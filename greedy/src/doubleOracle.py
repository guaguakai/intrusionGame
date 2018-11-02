from models.Model import *
from solvers.CoreLP import coreLP

if __name__ == "__main__":
    gameModel = GameModel(n=20)
    print(gameModel.attacker_strategy_set[0].path)
    print(gameModel.defender_strategy_set[0].coverage)
    print(gameModel.terminal_payoff)
    print(gameModel.payoff_matrix)
    # gameModel.drawGraph()

    prob, obj = coreLP(gameModel)
    print(prob, obj)
