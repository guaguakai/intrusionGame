from models.Model import *
from solvers.CoreLP import CoreLP
from solvers.DefenderOracle.BRDefenderP import BRDefenderP
from solvers.AttackerOracle.BRAttackerP import BRAttackerP

import numpy as np
import networkx as nx
import random
import time

if __name__ == "__main__":
    np.random.seed(1234)
    random.seed(1234)

    G = nx.DiGraph()
    G.add_edges_from([(0,1), (0,2), (0,3), (1,4), (2,4), (3,4), (4,5)])
    resource_list = [Resource(1,1), Resource(1,1)]
    gameModel = GameModel(n=6, T=2, S=1, R=2, G=G, resource_list=resource_list, source_list=[0], terminal_list=[4,5], terminal_payoff=[1,2])

    print(gameModel.attacker_strategy_set[0].path)
    print(gameModel.defender_strategy_set[0].coverage)
    print(gameModel.terminal_payoff)
    print(gameModel.payoff_matrix)
    # gameModel.drawGraph()
    defender_time = 0
    attacker_time = 0

    for count in range(10):
        def_prob, att_prob, obj = CoreLP(gameModel)

        print("iteration: {0}, objective value: {1}".format(count, obj))
        print("defender probability:", def_prob)
        print("attacker probability:", att_prob)

        gameModel.updateProbability(def_prob, att_prob)

        cp_def_obj, cp_def_coverage = BRDefenderP(gameModel)
        cp_att_obj, cp_att_path = BRAttackerP(gameModel)

        start = time.time()
        gameModel.updateDefenderStrategy(DefenderStrategy(cp_def_coverage, R=gameModel.R))
        end = time.time()
        defender_time += end-start

        start = time.time()
        gameModel.updateAttackerStrategy(AttackerStrategy(cp_att_path))
        end = time.time()
        attacker_time += end-start

        print("defender time:{0}, \t attacker time:{1}".format(defender_time, attacker_time))
        print("defender avg:{0}, \t attacker avg:{1}".format(defender_time/(count+1), attacker_time/(count+1)))

