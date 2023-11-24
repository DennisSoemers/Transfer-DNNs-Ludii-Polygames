'''
Script to convert all the final checkpoints of Ludii models we trained for various board sizes
to all other board sizes of the same game. This script reinitialises all the weights for
self.v and self.pi_logit.

@author Dennis Soemers
'''

import subprocess
from pathlib import Path


def quote(string):
    """
    Returns given string, with quotes around it
    """
    return "\"" + string + "\""


def best_checkpoint(checkpoint_dir):
    """
    Gives us the best checkpoint for given directory of checkpoints
    """
    # Find the server dir for this checkpoint dir
    server_dirs = list(Path(checkpoint_dir).glob("server-*"))
    
    if len(server_dirs) > 1:
        print("WARNING: Found more than 1 server dir")
    elif len(server_dirs) == 0:
        print("WARNING: Found 0 server dirs")
        
    # Find the job log in the server dir
    job_logs = list(server_dirs[0].glob("Job-*.log"))
    
    if len(job_logs) > 1:
        print("WARNING: Found more than 1 Job log")
    elif len(job_logs) == 0:
        print("WARNING: Found 0 Job logs")
        
    # Find last line in Job log that says "Top 10:"
    #last_top_10_line = None
    #job_log_lines = list(open(job_logs[0], 'r'))
    #for i in range(len(job_log_lines) - 1, -1, -1):
    #    if job_log_lines[i].rstrip() == "Top 10:":
    #        last_top_10_line = i
    #        break
    #        
    #if last_top_10_line is None:
    #    return None
    #        
    #best_epoch = job_log_lines[last_top_10_line + 1].split()[-1].strip()
    
    best_epoch = "dev"
    
    # Find the best checkpoint
    checkpoints = list(server_dirs[0].glob("checkpoint_*.pt"))
    if best_epoch == "dev":
        # Just find the highest epoch number
        max_epoch = -1
        best_checkpoint = None
        for checkpoint in checkpoints:
            epoch = int(checkpoint.name[len("checkpoint_"):len(checkpoint.name)-len(".pt")])
            if epoch > max_epoch:
                max_epoch = epoch
                best_checkpoint = checkpoint.name
                
        if best_checkpoint is None:
            return None
                
        return checkpoint_dir + "/" + server_dirs[0].name + "/" + best_checkpoint
    elif best_epoch == "init":
        # Take the lowest epoch number
        min_epoch = 10000000
        best_checkpoint = None
        for checkpoint in checkpoints:
            epoch = int(checkpoint.name[len("checkpoint_"):len(checkpoint.name)-len(".pt")])
            if epoch < min_epoch:
                min_epoch = epoch
                best_checkpoint = checkpoint.name
                
        return checkpoint_dir + "/" + server_dirs[0].name + "/" + best_checkpoint
    else:
        # Find matching epoch
        return checkpoint_dir + "/" + server_dirs[0].name + "/" + "checkpoint_" + best_epoch[1:] + ".pt"
    
    
def convert(src_checkpoint, target_options, out_file):
    args = [
        'python',
        '-u',
        '-m',
        'pypolygames',
        'convert',
        '--init_checkpoint',
        src_checkpoint,
        '--out',
        out_file,
        '--skip',
        'v.weight',
        'v.bias',
        'pi_logit.weight',
        'pi_logit.bias',
        '--game_options',
    ]
    
    if type(target_options) is list:
        args.extend(target_options)
    else:
        args.append(target_options)
    
    subprocess.check_call(args)


if __name__ == '__main__':
    broken_line_data = {
        "CheckpointDirs": [
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiBrokenLine.lud/LineSize3",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiBrokenLine.lud/LineSize4",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiBrokenLine.lud/LineSize5",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiBrokenLine.lud/LineSize6",
        ],
        "Options": [
            ["Line Size/3", "Board Size/5x5", "Board/hex"],
            ["Line Size/4", "Board Size/5x5", "Board/hex"],
            ["Line Size/5", "Board Size/9x9", "Board/Square"],
            ["Line Size/6", "Board Size/9x9", "Board/Square"],
        ],
        "OptionNames": [
            "LineSize3Hex",
            "LineSize4Hex",
            "LineSize5Square",
            "LineSize6Square",
        ],
        "Game": "LudiiBroken Line.lud"
    }
    
    diagonal_hex_data = {
        "CheckpointDirs": [
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiDiagonalHex.lud/7x7",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiDiagonalHex.lud/9x9",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiDiagonalHex.lud/11x11",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiDiagonalHex.lud/13x13",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiDiagonalHex.lud/19x19",
        ],
        "Options": [
            "Board Size/7x7",
            "Board Size/9x9",
            "Board Size/11x11",
            "Board Size/13x13",
            "Board Size/19x19",
        ],
        "OptionNames": [
            "7x7",
            "9x9",
            "11x11",
            "13x13",
            "19x19",
        ],
        "Game": "LudiiDiagonal Hex.lud"
    }

    gomoku_data = {
        "CheckpointDirs": [
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiGomoku.lud/9x9",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiGomoku.lud/13x13",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiGomoku.lud/15x15",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiGomoku.lud/19x19",
        ],
        "Options": [
            "Board Size/9x9",
            "Board Size/13x13",
            "Board Size/15x15",
            "Board Size/19x19",
        ],
        "OptionNames": [
            "9x9",
            "13x13",
            "15x15",
            "19x19",
        ],
        "Game": "LudiiGomoku.lud"
    }
    
    hex_data = {
        "CheckpointDirs": [
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiHex.lud/7x7",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiHex.lud/9x9",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiHex.lud/11x11",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiHex.lud/11x11Misere",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiHex.lud/13x13",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiHex.lud/19x19",
        ],
        "Options": [
            "Board Size/7x7",
            "Board Size/9x9",
            "Board Size/11x11",
            ["Board Size/11x11", "End Rules/Misere"],
            "Board Size/13x13",
            "Board Size/19x19",
        ],
        "OptionNames": [
            "7x7",
            "9x9",
            "11x11",
            "11x11Misere",
            "13x13",
            "19x19",
        ],
        "Game": "LudiiHex.lud"
    }
    
    pentalath_data = {
        "CheckpointDirs": [
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiPentalath.lud/HexHexBoard",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiPentalath.lud/HalfHexHexBoard",
        ],
        "Options": [
            "Board/HexHexBoard",
            "Board/HalfHexHexBoard",
        ],
        "OptionNames": [
            "HexHexBoard",
            "HalfHexHexBoard",
        ],
        "Game": "LudiiPentalath.lud"
    }
    
    yavalath_data = {
        "CheckpointDirs": [
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiYavalath.lud/3x3",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiYavalath.lud/4x4",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiYavalath.lud/5x5",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiYavalath.lud/6x6",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiYavalath.lud/7x7",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiYavalath.lud/8x8",
        ],
        "Options": [
            "Board Size/3x3",
            "Board Size/4x4",
            "Board Size/5x5",
            "Board Size/6x6",
            "Board Size/7x7",
            "Board Size/8x8",
        ],
        "OptionNames": [
            "3x3",
            "4x4",
            "5x5",
            "6x6",
            "7x7",
            "8x8",
        ],
        "Game": "LudiiYavalath.lud"
    }
    
    breakthrough_data = {
        "CheckpointDirs": [
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiBreakthrough.lud/Square6",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiBreakthrough.lud/Square8",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiBreakthrough.lud/Square10",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiBreakthrough.lud/Hexagon4",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiBreakthrough.lud/Hexagon6",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiBreakthrough.lud/Hexagon8",
        ],
        "Options": [
            ["Board Size/6x6", "Board/Square"],
            ["Board Size/8x8", "Board/Square"],
            ["Board Size/10x10", "Board/Square"],
            ["Board Size/4x4", "Board/Hexagon"],
            ["Board Size/6x6", "Board/Hexagon"],
            ["Board Size/8x8", "Board/Hexagon"],
        ],
        "OptionNames": [
            "Square6",
            "Square8",
            "Square10",
            "Hexagon4",
            "Hexagon6",
            "Hexagon8",
        ],
        "Game": "LudiiBreakthrough.lud"
    }
    
    hexentafl_data = {
        "CheckpointDirs": [
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiHeXentafl.lud/BoardSize4",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiHeXentafl.lud/BoardSize5",
        ],
        "Options": [
            "Board Size/4x4",
            "Board Size/5x5",
        ],
        "OptionNames": [
            "4x4",
            "5x5",
        ],
        "Game": "LudiiHeXentafl.lud"
    }
    
    konane_data = {
        "CheckpointDirs": [
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiKonane.lud/BoardSize6",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiKonane.lud/BoardSize8",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiKonane.lud/BoardSize10",
            "/checkpoint/dennissoemers/polygames_checkpoints/LudiiKonane.lud/BoardSize12",
        ],
        "Options": [
            "Board Size/6x6",
            "Board Size/8x8",
            "Board Size/10x10",
            "Board Size/12x12",
        ],
        "OptionNames": [
            "6x6",
            "8x8",
            "10x10",
            "12x12",
        ],
        "Game": "LudiiKonane.lud"
    }
    
    all_game_datas = [
        broken_line_data, 
        diagonal_hex_data, 
        gomoku_data, 
        hex_data,
        pentalath_data, 
        yavalath_data,
        breakthrough_data,
        hexentafl_data,
        konane_data,
    ]
    
    for game_data in all_game_datas:
        print("Processing game: ", game_data["Game"])
        num_options = len(game_data["Options"])
        
        for i in range(num_options):
            src_checkpoint = best_checkpoint(checkpoint_dir=game_data["CheckpointDirs"][i])
            print("src_checkpoint = ", src_checkpoint)
            
            if src_checkpoint is None:
                continue
        
            for j in range(num_options):
                if i == j:
                    continue
                
                out_file = "/".join([
                    "/checkpoint/dennissoemers/converted_reinit",
                    game_data["Game"],
                    game_data["OptionNames"][i] + "_to_" + game_data["OptionNames"][j] + ".pt.gz"
                ])
                
                convert(src_checkpoint=src_checkpoint, 
                        target_options=game_data["Options"][j],
                        out_file=out_file)
