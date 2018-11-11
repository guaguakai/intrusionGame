from models.Model import *
from solvers.CoreLP import CoreLP
from solvers.DefenderOracle.BRDefenderP import BRDefenderP
from solvers.AttackerOracle.BRAttackerP import BRAttackerP
import numpy as np
import random

if __name__ == "__main__":
    # np.random.seed(1234)
    # random.seed(123456)
    
    # grid_sizes = [50]
    np.random.seed(1234)
    random.seed(123456)
    grid_sizes = [200]

    avg_attacker_times = []
    avg_defender_times = []

    for grid_size in grid_sizes:
        # vary parameters
        gameModel = GameModel(n=grid_size, p=0.3, T=5, S=1, R=3, G=None, resource_list=None, source_list=None, terminal_list=None, terminal_payoff=None)
        print(gameModel.attacker_strategy_set[0].path)
        print(gameModel.defender_strategy_set[0].coverage)
        print(gameModel.terminal_payoff)
        print(gameModel.payoff_matrix)
        # gameModel.drawGraph()

        defender_time = 0
        attacker_time = 0

        for count in range(100):
            def_prob, att_prob, obj = CoreLP(gameModel)

            print("iteration: {0}, objective value: {1}".format(count, obj))
            print("defender probability:", def_prob)
            print("attacker probability:", att_prob)

            gameModel.updateProbability(def_prob, att_prob)


            start = time.time()
            cp_def_obj, cp_def_coverage = BRDefenderP(gameModel)
            end = time.time()
            defender_time += end - start

            start = time.time()
            cp_att_obj, cp_att_path = BRAttackerP(gameModel)
            end = time.time()
            attacker_time += end - start

            gameModel.updateDefenderStrategy(DefenderStrategy(cp_def_coverage, R=gameModel.R))
            gameModel.updateAttackerStrategy(AttackerStrategy(cp_att_path))

            print("defender time:{0}, \t attacker time:{1}".format(defender_time, attacker_time))
            print("defender avg:{0}, \t attacker avg:{1}".format(defender_time/(count+1), attacker_time/(count+1)))
            avg_attacker_times.append(attacker_time/(count+1))
            avg_defender_times.append(defender_time/(count+1))

    print("==========finished experiment==========")
    for i, grid_size in enumerate(sorted(grid_sizes)):
        print("grid size:{}, \t defender avg:{}, \t attacker avg:{}".format(grid_size, avg_defender_times[i], avg_attacker_times[i]))
        print("finished experiment")


