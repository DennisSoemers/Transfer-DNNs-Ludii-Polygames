#!/bin/bash
echo "$# parameters"
echo "$@"

cd ../Polygames

# Print info about branch and rev we're using
echo "Using branch:"
git symbolic-ref --short HEAD
echo "Using rev:"
git rev-parse HEAD

# Configuration of game
game_name="$1"
checkpoint_dir="$2"
game_options=( "${@:3}" )
echo Gamename:${game_name}
echo Game options:${game_options}

# Stuff for distributed training with PyTorch
export MASTER_PORT=7001

# Configuration of Neural Network
model_name="ResConvConvLogitPoolModelV2"
nn_size=2                       # Number of out channels per input channel for Conv2d layers
nb_nets=10                      # Number of subnets (different models have different configurations as "subnets")
nb_layers_per_net=6
random_features=0               # Number of random features the input includes
history=0                       # Number of last steps whose representation is added in the featurization

# Configuration of MCTS
num_rollouts=400  # tunes the number of simulations in the clients, per move.

# Configuration of Training Process
max_time=72000                  # Maximum time allowed for a run (in seconds)
saving_period=6                 # Number of epochs between two consecutive checkpoints
num_game=2                      # Number of game-running threads (2 gives good GPU usage, >2 almost never any benefit, ignored by server)
epoch_len=256                   # Number of train batches per epoch
batch_size=128                  # Number of training examples in a mini-batch (train batch) (ignored by clients)
sync_period=32                  # Number of epochs between two consecutive sync between the model and the assembler
train_channel_timeout_ms=1000   # Timeout (in milliseconds) to wait for actors to produce trajectories
train_channel_num_slots=10000   # Number of slots in train channel used to send trajectories
replay_capacity=100000          # Nb of act_batches the replay buffer can contain
replay_warmup=9000              # Nb of act_batches the replay buffer needs to buffer before the training can start

# Auxiliary stuff
export FSLURM_JOB_ID=${SLURM_JOB_ID}

# Launch our clients (7 of them, for 8 GPUs on a node)
# Every client runs self-play games and sends results to server
for i in $(seq 0 6); do
    c=$(expr 1 + $i % 7)
    echo "Client $i on cuda:$c"
    export CUDA_VISIBLE_DEVICES=$c
	
	checkpoint_dir_client="${checkpoint_dir}/client-$i"
	mkdir -p "${checkpoint_dir_client}"
    
    RANK=0 WORLD_SIZE=1 MASTER_ADDR=127.0.0.1 MASTER_PORT=701${c} \
        TORCH_USE_RTLD_GLOBAL=YES \
        python -u -m pypolygames train \
        --max_time=${max_time} \
        --saving_period=${saving_period} \
        --num_game ${num_game} \
        --epoch_len ${epoch_len} \
        --batchsize ${batch_size} \
        --sync_period ${sync_period} \
        --num_rollouts ${num_rollouts} \
        --train_channel_timeout_ms ${train_channel_timeout_ms} \
        --train_channel_num_slots ${train_channel_num_slots} \
        --replay_capacity ${replay_capacity} \
        --replay_warmup ${replay_warmup} \
        --do_not_save_replay_buffer \
        --checkpoint_dir "${checkpoint_dir_client}" \
        --random_features ${random_features} \
        --history ${history} \
        --game_name "${game_name}" \
		--game_options "${game_options[@]}" \
        --model_name ${model_name} \
        --bn \
        --nnsize ${nn_size} \
        --nb_layers_per_net ${nb_layers_per_net} \
        --nb_nets ${nb_nets} \
        --tournament_mode=true \
        --bsfinder_max_bs=400 \
        --connect tcp://127.0.0.1:5611 2>&1 | cat > "${checkpoint_dir_client}/Job-${FSLURM_JOB_ID}.log"  &
done

mkdir -p "${checkpoint_dir}/server-${FSLURM_JOB_ID}"

# Launch server, for training
RANK=0 WORLD_SIZE=1 MASTER_ADDR=127.0.0.1 MASTER_PORT=$MASTER_PORT \
    TORCH_USE_RTLD_GLOBAL=YES \
    python -u -m pypolygames train \
    --max_time=${max_time} \
    --saving_period=${saving_period} \
    --num_game ${num_game} \
    --epoch_len ${epoch_len} \
    --batchsize ${batch_size} \
    --sync_period ${sync_period} \
    --num_rollouts ${num_rollouts} \
    --train_channel_timeout_ms ${train_channel_timeout_ms} \
    --train_channel_num_slots ${train_channel_num_slots} \
    --replay_capacity ${replay_capacity} \
    --replay_warmup ${replay_warmup} \
    --do_not_save_replay_buffer \
    --checkpoint_dir "${checkpoint_dir}/server-${FSLURM_JOB_ID}" \
    --random_features ${random_features} \
    --history ${history} \
    --game_name "${game_name}" \
	--game_options "${game_options[@]}" \
    --model_name ${model_name} \
    --bn \
    --nnsize ${nn_size} \
    --nb_layers_per_net ${nb_layers_per_net} \
    --nb_nets ${nb_nets} \
    --tournament_mode=true \
    --bsfinder_max_bs=400 \
    --listen tcp://0.0.0.0:5611 2>&1 | cat > "${checkpoint_dir}/server-${FSLURM_JOB_ID}/Job-${FSLURM_JOB_ID}.log"
