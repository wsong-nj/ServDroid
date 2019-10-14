If you use the open-source code for ServDroid, please cite our paper: 

Wei Song, Jing Zhang, Jeff Huang. ServDroid: Detecting Service Usage Inefficiencies in Android Applications. FSE2019.


The main branch uses InfoflowCFG as the basis for the analysis. However, in September 2019, we found that InfoflowCFG is not complete in some cases which may affect the analysis results. To this end, we also provide another branch that is not based on InfoflowCFG (we implement inter-procedural dominator/postdominator analysis by ourselves based on Scene in Soot).
