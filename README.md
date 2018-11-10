### Introduction

Please enter the directory **greedy/src**. Then simply run **python sanityCheck.py** or **python doubleOracle.py**.
You can record the running time of both oracles in **doubleOracle.py**.
You can customize the input of the **gameModel** instance. Please refer to **model/Model.py** for more detail about the constructor of class **gameModel**.

### Required Libraries
* networkx
* numpy
* cplex

Notice that even if you have installed the cplex on your computer, you probably haven't configured the **cpoptimize**.
You may need to create a symbolic link of the binary files of both cplex and cpoptimize in your cplex folder.
E.g. **ln your/cplex/folder/bin/version/cplex /usr/bin/**

By default, the docplex library will run its cloud service. But you can specify that you want to run on your local machine. Please refer to **https://github.com/IBMDecisionOptimization/docplex-examples** for more details.
