# Towards a General Transfer Approach for Policy-Value Networks

This repository contains supporting scripts used for the paper [*"Towards a General Transfer Approach for Policy-Value Networks"*](https://openreview.net/forum?id=vJcTm2v9Ku). The scripts in this repository are only fairly small scripts, defining experiments that were run and providing links between [Ludii](https://github.com/ludeme/ludii) and [Polygames](https://github.com/facebookarchive/Polygames). 

## Dependency: Ludii (general game playing engine)

The [Ludii](https://github.com/ludeme/ludii) repository contains:
- Game description files (defining the rules of all games used in this paper, interpreted by Ludii's engine)
- Code to generate tensors from Ludii's internal state action action representations, return rewards, and any other code that is required by the Python-based deep learning code. More specifically, this code is implemented in the [LudiiGameWrapper.java](https://github.com/Ludeme/Ludii/blob/master/AI/src/utils/LudiiGameWrapper.java) and [LudiiStateWrapper.java](https://github.com/Ludeme/Ludii/blob/master/AI/src/utils/LudiiStateWrapper.java) files.
- Code to identify which channels should map to each other for any given source-target pairing. This is implemented in the `moveTensorSourceChannels()` and `stateTensorSourceChannels()` methods of [LudiiGameWrapper.java](https://github.com/Ludeme/Ludii/blob/master/AI/src/utils/LudiiGameWrapper.java).

## Dependency: Polygames (serch & deep learning code)

The [Polygames](https://github.com/facebookarchive/Polygames) repository contains:
- All deep learning, neural networks, and MCTS code.
- [JNI code for interfacing](https://github.com/facebookarchive/Polygames/tree/eb5390e57cc38e5287bf6dcfb420308a5995d194/src/games/ludii) between Ludii's Java code (general game playing engine), and Polygames' C++ and Python code for search and learning.
- [Script for converting trained models](https://github.com/facebookarchive/Polygames/blob/eb5390e57cc38e5287bf6dcfb420308a5995d194/pypolygames/convert.py) based on identified relations between channels.

## Citing Information

Please use the following `.bib` entry to cite our paper:

	@article{Soemers_2023_Transfer,
            author      = "D. J. N. J. Soemers and V. Mella and {\'E.} Piette and M. Stephenson and C. Browne and O. Teytaud",
            journal     = "Transactions on Machine Learning Research",
            title       = "Towards a General Transfer Approach for Policy-Value Networks",
            year        = "2023",
            url         = "\url{https://openreview.net/forum?id=vJcTm2v9Ku}"
    }

## See Also

See our other paper on [*"Deep Learning for General Game Playing with Ludii and Polygames"*](https://content.iospress.com/articles/icga-journal/icg220197) for more information on the bridge between Ludii and Polygames.
