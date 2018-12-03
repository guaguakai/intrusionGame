from models.Model import *
from solvers.CoreLP import CoreLP
from solvers.DefenderOracle.BRDefenderP import BRDefenderP
from solvers.AttackerOracle.BRAttackerP import BRAttackerP

def main():
    gameModel = GameModel(n=20)
    print(gameModel.attacker_strategy_set[0].path)
    print(gameModel.defender_strategy_set[0].coverage)
    print(gameModel.terminal_payoff)
    print(gameModel.payoff_matrix)
    # gameModel.drawGraph()

    for count in range(100):
        def_prob, att_prob, obj = CoreLP(gameModel)

        print("iteration: {0}, objective value: {1}".format(count, obj))
        print("defender probability:", def_prob)
        print("attacker probability:", att_prob)

        gameModel.updateProbability(def_prob, att_prob)

        cp_def_obj, cp_def_coverage = BRDefenderP(gameModel)
        cp_att_obj, cp_att_path = BRAttackerP(gameModel)

        gameModel.updateDefenderStrategy(DefenderStrategy(cp_def_coverage, R=gameModel.R))
        gameModel.updateAttackerStrategy(AttackerStrategy(cp_att_path))

if __name__ == "__main__":
    main()
    print "TODO"
