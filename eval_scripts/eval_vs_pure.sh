cd /private/home/dennissoemers/Polygames

# Print info about branch and rev we're using
echo "Using branch:"
git symbolic-ref --short HEAD
echo "Using rev:"
git rev-parse HEAD


# Configuration of models
eval_model_name="$1"
eval_model="$2"
game_name="$3"
echo Game Name:${game_name}
echo Eval Model:${eval_model}
echo Cuda Visible Devices = $CUDA_VISIBLE_DEVICES

# Configuration of MCTSes
num_actor_eval=1					# Number of threads running MCTS for eval
num_rollouts_eval=40				# Number of rollouts per actor/thread for eval MCTS
num_actor_opponent=1				# Number of threads running MCTS for opponent
num_rollouts_opponent=800			# Number of rollouts per actor/thread for opponent MCTS

# Configuration of Evaluation process
num_game_eval=300					# Total number of evaluation games
num_parallel_games_eval=150			# Number of eval games to be played in parallel
seed_eval=${SLURM_JOB_ID}

# Auxiliary stuff
export FSLURM_JOB_ID=${SLURM_JOB_ID}
mkdir -p /checkpoint/${USER}/polygames_evals

python -u -m pypolygames eval \
    --checkpoint="${eval_model}" \
    --device_eval "cuda:0" \
    --num_game_eval=${num_game_eval} \
	--num_actor_eval=${num_actor_eval} \
	--num_rollouts_eval=${num_rollouts_eval} \
	--num_actor_opponent=${num_actor_opponent} \
	--num_rollouts_opponent=${num_rollouts_opponent} \
	--num_parallel_games_eval=${num_parallel_games_eval} 2>&1 |cat > /checkpoint/${USER}/polygames_evals/${game_name}_${eval_model_name}_vs_pure_${FSLURM_JOB_ID}.log
