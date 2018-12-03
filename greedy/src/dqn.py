from models.Model import *
from solvers.CoreLP import CoreLP
from solvers.DefenderOracle.BRDefenderP import BRDefenderP
from solvers.DefenderOracle.NNDefenderP import NNDefenderP
from solvers.AttackerOracle.BRAttackerP import BRAttackerP
from solvers.Embedding.n2v_embedding import GraphEmbedding

import networkx as nx
import numpy as np


class NN_Model: # dummy nn model
    def __init__(self):
        print("initialize nn model")
    def predict(self, features):
        shape = features.shape
        # return np.random.random(shape[0])
        return features[:,-1]


def PlayGame(n, p, R, resource_list, G, nn_model, embedding_list, total_count=10):
    gameModel = GameModel(n=n, p=p, G=G, R=R, resource_list=resource_list)
    def_prob, att_prob, obj = CoreLP(gameModel)
    gameModel.updateProbability(def_prob, att_prob)
    replay_memory = []

    for count in range(100):
        old_obj = obj
        print("iteration: {0}, objective value: {1}".format(count, obj))
        # print("defender probability:", def_prob)
        # print("attacker probability:", att_prob)

        cp_def_obj, cp_def_coverage, raw_replay_memory = NNDefenderP(gameModel, embedding_list, nn_model)
        br_cp_def_obj, br_cp_def_coverage = BRDefenderP(gameModel)
        print("greedy defender obj: {0}".format(cp_def_obj))
        print("best response defender obj: {0}".format(br_cp_def_obj))

        cp_att_obj, cp_att_path = BRAttackerP(gameModel)

        gameModel.updateDefenderStrategy(DefenderStrategy(cp_def_coverage, R=gameModel.R))
        gameModel.updateAttackerStrategy(AttackerStrategy(cp_att_path))

        def_prob, att_prob, obj = CoreLP(gameModel)
        gameModel.updateProbability(def_prob, att_prob)

if __name__ == "__main__":
    iterations = 1
    n = 20
    p = 0.3
    R = 3
    resource_list = [Resource(4,0.3), Resource(3,0.5), Resource(2,0.7)]
    G = nx.random_geometric_graph(n, p).to_directed()

    embedding_list = [GraphEmbedding(G) for i in range(R)]
    nn_model = NN_Model()

    for i in range(iterations):
        PlayGame(n, p, R, resource_list, G, nn_model, embedding_list)
