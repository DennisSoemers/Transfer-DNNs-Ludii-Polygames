import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import main.StringRoutines;
import utils.LudiiGameWrapper;

/**
 * Script with main method to convert Polygames models between 
 * different Ludii games for zero-shot evaluations.
 * 
 * On FAIR cluster, compile (from local_scripts as working directory) using:
 * 	javac -cp /private/home/dennissoemers/Polygames/ludii/Ludii.jar ./ConvertLudiiModelsCrossGameZeroShot.java
 * 
 * From the Polygames dir, run using:
 * 	java -cp /private/home/dennissoemers/Polygames/ludii/Ludii.jar:/private/home/dennissoemers/local_scripts ConvertLudiiModelsCrossGameZeroShot
 *
 * @author Dennis Soemers
 */
public class ConvertLudiiModelsCrossGameZeroShot
{
	
	/**
	 * @param s
	 * @return Given String s, with quotes around it.
	 */
	private static final String quote(final String s)
	{
		return "\"" + s + "\"";
	}
	
	/**
	 * @param checkpointDirPath
	 * @return Filepath for best checkpoint found in given directory of checkpoints,
	 * 	or null if no checkpoint found.
	 */
	private static final String bestCheckpoint(final String checkpointDirPath)
	{
		final File checkpointDir = new File(checkpointDirPath);
		
		for (final File f : checkpointDir.listFiles())
		{
			if (f.isDirectory() && f.getName().startsWith("server-"))
			{
				// f is the server-$JOBID dir
				File lastCheckpoint = null;
				int lastEpoch = -1;
				
				for (final File file : f.listFiles())
				{
					final String fileName = file.getName();
					if (file.isFile() && fileName.startsWith("checkpoint_") && fileName.endsWith(".pt"))
					{
						final int epoch = Integer.parseInt(fileName
								.replaceAll(Pattern.quote("checkpoint_"), "")
								.replaceAll(Pattern.quote(".pt"), ""));
						
						if (epoch > lastEpoch)
						{
							lastEpoch = epoch;
							lastCheckpoint = file;
						}
					}
				}
				
				if (lastCheckpoint == null)
					return null;
				
				return lastCheckpoint.getAbsolutePath();
			}
		}
		
		return null;
	}
	
	/**
	 * Runs the convert command locally
	 * @param sourceCheckpoint
	 * @param targetGame
	 * @param targetOptions
	 * @param outFile
	 * @param moveSourceChannels
	 * @param stateSourceChannels
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	private static void convert
	(
		final String sourceCheckpoint, 
		final String targetGame,
		final String[] targetOptions, 
		final String outFile,
		final int[] moveSourceChannels,
		final int[] stateSourceChannels
	) throws IOException, InterruptedException
	{
		final List<String> args = new ArrayList<String>(Arrays.asList(
				"python",
				"-u",
				"-m",
				"pypolygames",
				"convert",
				"--init_checkpoint",
				sourceCheckpoint,
				"--out",
				outFile,
				"--zero_shot=true",
				"--auto_tune_nnsize",
				"--game_name",
				targetGame,
				"--game_options"
		));
		
		if (targetOptions.length > 0)
			args.addAll(Arrays.asList(targetOptions));
		
		args.add("--move_source_channels");
		for (final int c : moveSourceChannels)
			args.add("" + c);
		
		args.add("--state_source_channels");
		for (final int c : stateSourceChannels)
			args.add("" + c);
		
		final ProcessBuilder pb = new ProcessBuilder(args);
		pb.redirectOutput(Redirect.INHERIT);
		pb.redirectError(Redirect.INHERIT);
		final Process p = pb.start();
		p.waitFor();
	}
	
	/**
	 * Wrapper class around data for a single game
	 *
	 * @author Dennis Soemers
	 */
	private static final class GameData
	{
		public final String[] checkpointDirs;
		public final String[][] options;
		public final String[] optionNames;
		public final String game;
		
		public GameData
		(
			final String[] checkpointDirs, 
			final String[][] options, 
			final String[] optionNames, 
			final String game
		)
		{
			this.checkpointDirs = checkpointDirs;
			this.options = options;
			this.optionNames = optionNames;
			this.game = game;
		}
	}
	
	public static void main(final String[] args) throws IOException, InterruptedException
	{
		// Line Completion games
		final GameData brokenLineData = new GameData(
				new String[]{
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiBrokenLine.lud/LineSize3",
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiBrokenLine.lud/LineSize4",
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiBrokenLine.lud/LineSize5",
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiBrokenLine.lud/LineSize6",
				},
				new String[][]{
					{"Line Size/3", "Board Size/5x5", "Board/hex"},
					{"Line Size/4", "Board Size/5x5", "Board/hex"},
					{"Line Size/5", "Board Size/9x9", "Board/Square"},
					{"Line Size/6", "Board Size/9x9", "Board/Square"},
				},
				new String[]{
						"LineSize3Hex",
						"LineSize4Hex",
						"LineSize5Square",
						"LineSize6Square",
				},
				"LudiiBroken Line.lud"
				);
		
		final GameData connect6Data = new GameData(
				new String[]{
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiConnect6.lud/Default",
				},
				new String[][]{
					{},
				},
				new String[]{
						"",
				},
				"LudiiConnect6.lud"
				);
		
		final GameData daiHasamiShogiData = new GameData(
				new String[]{
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiDaiHasamiShogi.lud/Default",
				},
				new String[][]{
					{},
				},
				new String[]{
						"",
				},
				"LudiiDai Hasami Shogi.lud"
				);
		
		final GameData gomokuData = new GameData(
				new String[]{
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiGomoku.lud/9x9",
						//"/checkpoint/dennissoemers/polygames_checkpoints/LudiiGomoku.lud/13x13",
						//"/checkpoint/dennissoemers/polygames_checkpoints/LudiiGomoku.lud/15x15",
						//"/checkpoint/dennissoemers/polygames_checkpoints/LudiiGomoku.lud/19x19",
				},
				new String[][]{
					{"Board Size/9x9"},
					//{"Board Size/13x13"},
					//{"Board Size/15x15"},
					//{"Board Size/19x19"},
				},
				new String[]{
						"9x9",
						//"13x13",
						//"15x15",
						//"19x19",
				},
				"LudiiGomoku.lud"
				);
		
		final GameData pentalathData = new GameData(
				new String[]{
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiPentalath.lud/HexHexBoard",
						//"/checkpoint/dennissoemers/polygames_checkpoints/LudiiPentalath.lud/HalfHexHexBoard",
				},
				new String[][]{
					{"Board/HexHexBoard"},
					//{"Board/HalfHexHexBoard"},
				},
				new String[]{
						"HexHexBoard",
						//"HalfHexHexBoard",
				},
				"LudiiPentalath.lud"
				);
		
		final GameData squavaData = new GameData(
				new String[]{
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiSquava.lud/Default",
				},
				new String[][]{
					{},
				},
				new String[]{
						"",
				},
				"LudiiSquava.lud"
				);
		
		final GameData yavalathData = new GameData(
				new String[]{
						//"/checkpoint/dennissoemers/polygames_checkpoints/LudiiYavalath.lud/3x3",
						//"/checkpoint/dennissoemers/polygames_checkpoints/LudiiYavalath.lud/4x4",
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiYavalath.lud/5x5",
						//"/checkpoint/dennissoemers/polygames_checkpoints/LudiiYavalath.lud/6x6",
						//"/checkpoint/dennissoemers/polygames_checkpoints/LudiiYavalath.lud/7x7",
						//"/checkpoint/dennissoemers/polygames_checkpoints/LudiiYavalath.lud/8x8",
				},
				new String[][]{
					//{"Board Size/3x3"},
					//{"Board Size/4x4"},
					{"Board Size/5x5"},
					//{"Board Size/6x6"},
					//{"Board Size/7x7"},
					//{"Board Size/8x8"},
				},
				new String[]{
						//"3x3",
						//"4x4",
						"5x5",
						//"6x6",
						//"7x7",
						//"8x8",
				},
				"LudiiYavalath.lud"
				);
		
		// Hex-like games
		final GameData diagonalHexData = new GameData(
				new String[]{
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiDiagonalHex.lud/7x7",
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiDiagonalHex.lud/9x9",
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiDiagonalHex.lud/11x11",
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiDiagonalHex.lud/13x13",
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiDiagonalHex.lud/19x19",
				},
				new String[][]{
					{"Board Size/7x7"},
					{"Board Size/9x9"},
					{"Board Size/11x11"},
					{"Board Size/13x13"},
					{"Board Size/19x19"},
				},
				new String[]{
						"7x7",
						"9x9",
						"11x11",
						"13x13",
						"19x19",
				},
				"LudiiDiagonal Hex.lud"
				);
		
		final GameData hexData = new GameData(
				new String[]{
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiHex.lud/7x7",
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiHex.lud/9x9",
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiHex.lud/11x11",
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiHex.lud/11x11Misere",
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiHex.lud/13x13",
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiHex.lud/19x19",
				},
				new String[][]{
					{"Board Size/7x7"},
					{"Board Size/9x9"},
					{"Board Size/11x11"},
					{"Board Size/11x11", "End Rules/Misere"},
					{"Board Size/13x13"},
					{"Board Size/19x19"},
				},
				new String[]{
						"7x7",
						"9x9",
						"11x11",
						"11x11Misere",
						"13x13",
						"19x19",
				},
				"LudiiHex.lud"
				);
		
		// Shogi variants
		final GameData hasamiShogi = new GameData(
				new String[]{
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiHasamiShogi.lud/Default",
				},
				new String[][]{
					{},
				},
				new String[]{
						"",
				},
				"LudiiHasami Shogi.lud"
				);
		
		final GameData kyotoShogi = new GameData(
				new String[]{
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiKyotoShogi.lud/Default",
				},
				new String[][]{
					{},
				},
				new String[]{
						"",
				},
				"LudiiKyoto Shogi.lud"
				);
		
		final GameData minishogi = new GameData(
				new String[]{
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiMinishogi.lud/Default",
				},
				new String[][]{
					{},
				},
				new String[]{
						"",
				},
				"LudiiMinishogi.lud"
				);
		
		final GameData shogi = new GameData(
				new String[]{
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiShogi.lud/Default",
				},
				new String[][]{
					{},
				},
				new String[]{
						"",
				},
				"LudiiShogi.lud"
				);
		
		final GameData tobiShogi = new GameData(
				new String[]{
						"/checkpoint/dennissoemers/polygames_checkpoints/LudiiTobiShogi.lud/Default",
				},
				new String[][]{
					{},
				},
				new String[]{
						"",
				},
				"LudiiTobi Shogi.lud"
				);
		
		final List<List<GameData>> gameCombinations = new ArrayList<List<GameData>>();
		
		// Line completion games
		final List<GameData> lineCompletionGames = 
				Arrays.asList(connect6Data, daiHasamiShogiData, gomokuData, pentalathData, squavaData, yavalathData);
		gameCombinations.add(lineCompletionGames);
		
		// Shogi variants
		final List<GameData> shogiVariants = 
				Arrays.asList(hasamiShogi, kyotoShogi, minishogi, shogi, tobiShogi);
		gameCombinations.add(shogiVariants);
		
		// All possible cross-game conversions among line-completion games, and among Shogi variants
		for (final List<GameData> gameCombination : gameCombinations)
		{
			for (final GameData sourceGame : gameCombination)
			{
				final int numSourceOptions = sourceGame.options.length;
				
				for (int i = 0; i < numSourceOptions; ++i)
				{
					final LudiiGameWrapper sourceWrapper = LudiiGameWrapper.construct(
							sourceGame.game.substring("Ludii".length()), 
							sourceGame.options[i]);
					final String srcCheckpoint = bestCheckpoint(sourceGame.checkpointDirs[i]);
					
					if (srcCheckpoint == null)
						continue;
					
					for (final GameData targetGame : gameCombination)
					{
						if (sourceGame == targetGame)
							continue;
						
						final int numTargetOptions = targetGame.options.length;
						
						for (int j = 0; j < numTargetOptions; ++j)
						{
							final String outFile = StringRoutines.join("/", 
									"/checkpoint/dennissoemers/crossgame/zeroshot",
									sourceGame.game + sourceGame.optionNames[i] + "_to_" + targetGame.game + targetGame.optionNames[j] + ".pt.gz");
							
							final LudiiGameWrapper targetWrapper = LudiiGameWrapper.construct(
									targetGame.game.substring("Ludii".length()), 
									targetGame.options[j]);
							
							final int[] moveSourceChannels = targetWrapper.moveTensorSourceChannels(sourceWrapper);
							final int[] stateSourceChannels = targetWrapper.stateTensorSourceChannels(sourceWrapper);
							
							System.out.println("");
							System.out.println("Source: " + sourceGame.game + " (" + Arrays.toString(sourceGame.options[i]) + ")");
							System.out.println("Target: " + targetGame.game + " (" + Arrays.toString(targetGame.options[j]) + ")");
							
							for (int c = 0; c < moveSourceChannels.length; ++c)
							{
								if (moveSourceChannels[c] != c)
									System.out.println("Transferring move channel " + moveSourceChannels[c] + " --> " + c);
							}
							
							for (int c = 0; c < stateSourceChannels.length; ++c)
							{
								if (stateSourceChannels[c] != c)
									System.out.println("Transferring state channel " + stateSourceChannels[c] + " --> " + c);
							}
							
							convert(srcCheckpoint, targetGame.game, targetGame.options[j], outFile, moveSourceChannels, stateSourceChannels);
						}
					}
				}
			}
		}
		
		// Convert all versions of Broken Line to each of the different line-completion games
		GameData sourceGame = brokenLineData;
		int numSourceOptions = sourceGame.options.length;
				
		for (int i = 0; i < numSourceOptions; ++i)
		{
			final LudiiGameWrapper sourceWrapper = LudiiGameWrapper.construct(
					sourceGame.game.substring("Ludii".length()), 
					sourceGame.options[i]);
			final String srcCheckpoint = bestCheckpoint(sourceGame.checkpointDirs[i]);

			if (srcCheckpoint == null)
				continue;

			for (final GameData targetGame : lineCompletionGames)
			{
				final int numTargetOptions = targetGame.options.length;

				for (int j = 0; j < numTargetOptions; ++j)
				{
					final String outFile = StringRoutines.join("/", 
							"/checkpoint/dennissoemers/crossgame/zeroshot",
							sourceGame.game + sourceGame.optionNames[i] + "_to_" + targetGame.game + targetGame.optionNames[j] + ".pt.gz");

					final LudiiGameWrapper targetWrapper = LudiiGameWrapper.construct(
							targetGame.game.substring("Ludii".length()), 
							targetGame.options[j]);

					final int[] moveSourceChannels = targetWrapper.moveTensorSourceChannels(sourceWrapper);
					final int[] stateSourceChannels = targetWrapper.stateTensorSourceChannels(sourceWrapper);
					
					System.out.println("");
					System.out.println("Source: " + sourceGame.game + " (" + Arrays.toString(sourceGame.options[i]) + ")");
					System.out.println("Target: " + targetGame.game + " (" + Arrays.toString(targetGame.options[j]) + ")");

					for (int c = 0; c < moveSourceChannels.length; ++c)
					{
						if (moveSourceChannels[c] != c)
							System.out.println("Transferring move channel " + moveSourceChannels[c] + " --> " + c);
					}

					for (int c = 0; c < stateSourceChannels.length; ++c)
					{
						if (stateSourceChannels[c] != c)
							System.out.println("Transferring state channel " + stateSourceChannels[c] + " --> " + c);
					}

					convert(srcCheckpoint, targetGame.game, targetGame.options[j], outFile, moveSourceChannels, stateSourceChannels);
				}
			}
		}
		
		// Convert all versions of Diagonal Hex to all versions of Hex
		sourceGame = diagonalHexData;
		numSourceOptions = sourceGame.options.length;
				
		for (int i = 0; i < numSourceOptions; ++i)
		{
			final LudiiGameWrapper sourceWrapper = LudiiGameWrapper.construct(
					sourceGame.game.substring("Ludii".length()), 
					sourceGame.options[i]);
			final String srcCheckpoint = bestCheckpoint(sourceGame.checkpointDirs[i]);

			if (srcCheckpoint == null)
				continue;
			
			final GameData targetGame = hexData;
			final int numTargetOptions = targetGame.options.length;

			for (int j = 0; j < numTargetOptions; ++j)
			{
				final String outFile = StringRoutines.join("/", 
						"/checkpoint/dennissoemers/crossgame/zeroshot",
						sourceGame.game + sourceGame.optionNames[i] + "_to_" + targetGame.game + targetGame.optionNames[j] + ".pt.gz");

				final LudiiGameWrapper targetWrapper = LudiiGameWrapper.construct(
						targetGame.game.substring("Ludii".length()), 
						targetGame.options[j]);

				final int[] moveSourceChannels = targetWrapper.moveTensorSourceChannels(sourceWrapper);
				final int[] stateSourceChannels = targetWrapper.stateTensorSourceChannels(sourceWrapper);

				System.out.println("");
				System.out.println("Source: " + sourceGame.game + " (" + Arrays.toString(sourceGame.options[i]) + ")");
				System.out.println("Target: " + targetGame.game + " (" + Arrays.toString(targetGame.options[j]) + ")");

				for (int c = 0; c < moveSourceChannels.length; ++c)
				{
					if (moveSourceChannels[c] != c)
						System.out.println("Transferring move channel " + moveSourceChannels[c] + " --> " + c);
				}

				for (int c = 0; c < stateSourceChannels.length; ++c)
				{
					if (stateSourceChannels[c] != c)
						System.out.println("Transferring state channel " + stateSourceChannels[c] + " --> " + c);
				}

				convert(srcCheckpoint, targetGame.game, targetGame.options[j], outFile, moveSourceChannels, stateSourceChannels);
			}
		}
	}

}