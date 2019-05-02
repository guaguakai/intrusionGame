from models.Model import *
from solvers.CoreLP import CoreLP
from solvers.DefenderOracle.BRDefenderP import BRDefenderP
from solvers.DefenderOracle.GCNDefenderP import GCNDefenderP
# from solvers.DefenderOracle.NNDefenderP import NNDefenderP
from solvers.AttackerOracle.BRAttackerP import BRAttackerP
from solvers.Embedding.gcn import GCN
# from solvers.Embedding.n2v_embedding import GraphEmbedding

import networkx as nx
import numpy as np

import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim


class BasicGreedy:
    def __init__(self):
        print("initialize nn model")
        self.learning_rate = 1e-5

    def predict(self, features):
        shape = features.shape
        # return np.random.random(shape[0])
        return features[:,-1]


class NNModel(torch.nn.Module): # dummy nn model
    def __init__(self):
        super(NNModel, self).__init__()
        print("initialize nn model")
        self.linear1 = nn.Linear(5, 16)
        self.linear2 = nn.Linear(16, 10)
        self.linear3 = nn.Linear(10, 1)
        self.batch_size = 10

    def forward(self, x):
        h_relu = self.linear1(x).clamp(min=0)
        h_relu2 = self.linear2(h_relu).clamp(min=0)
        y_pred = self.linear3(h_relu2)
        return y_pred

    # def predict(self, features):
    #     return self.forward(torch.tensor(features).float())

def processMemory(gameModel, raw_replay_memory):
    # =================== processing memory and adding rewards ==================
    initial_defender_coverage = {}
    processed_replay_memory = []
    processed_replay_X = []
    processed_replay_y = []

    edges = list(gameModel.G.edges())
    attacker_paths = gameModel.attacker_strategy_set
    attacker_prob = gameModel.attacker_prob
    assert(len(attacker_paths) == len(attacker_prob))

    G = gameModel.G
    R = gameModel.R
    reward_list = gameModel.reward_list
    resource_list = gameModel.resource_list
    edge2index = gameModel.edge2index

    success_prob = np.ones(len(attacker_paths))
    for (resource, edge, feature, current_coverage) in raw_replay_memory:
        # --------------- deterministic payoff computation -------------------
        marginal_reward = 0
        for i in range(len(attacker_paths)):
            if edge in attacker_paths[i].getEdgeSet():
                marginal_reward += success_prob[i] * resource_list[resource].prob * attacker_prob[i] * reward_list[attacker_paths[i].getTarget()]
                success_prob[i] *= (1 - resource_list[resource].prob)

        processed_replay_memory.append((feature, marginal_reward))
        processed_replay_X.append(feature)
        processed_replay_y.append(marginal_reward)

    return processed_replay_memory, torch.stack(processed_replay_X), torch.reshape(torch.Tensor(processed_replay_y), (len(processed_replay_y), 1))


def playGame(n, p, R, resource_list, G, nn_model, loss_fn, embedding_model, optimizer, entire_replay_memory, total_count=50):
    gameModel = GameModel(n=n, p=p, G=G, R=R, resource_list=resource_list)
    def_prob, att_prob, obj = CoreLP(gameModel)
    gameModel.updateProbability(def_prob, att_prob)

    for count in range(total_count):
        old_obj = obj
        # print("defender probability:", def_prob)
        # print("attacker probability:", att_prob)

        cp_def_obj, cp_def_coverage, raw_replay_memory = GCNDefenderP(gameModel, embedding_model, nn_model)
        # cp_def_obj, cp_def_coverage, raw_replay_memory = NNDefenderP(gameModel, embedding_list, nn_model)

        # br_cp_def_obj, br_cp_def_coverage = BRDefenderP(gameModel)
        # print("greedy defender obj: {0}".format(cp_def_obj))
        # print("best response defender obj: {0}".format(br_cp_def_obj))

        cp_att_obj, cp_att_path = BRAttackerP(gameModel)
        # print("best response attacker obj: {0}".format(cp_att_obj))

        gameModel.updateDefenderStrategy(DefenderStrategy(cp_def_coverage, R=gameModel.R))
        gameModel.updateAttackerStrategy(AttackerStrategy(cp_att_path))

        def_prob, att_prob, obj = CoreLP(gameModel)
        gameModel.updateProbability(def_prob, att_prob)

        processed_replay_memory, processed_replay_X, processed_replay_y = processMemory(gameModel, raw_replay_memory)
        entire_replay_memory += processed_replay_memory

        processed_replay_X = torch.Tensor(processed_replay_X).float()
        processed_replay_y = torch.Tensor(processed_replay_y).float()

        y_pred = nn_model(processed_replay_X)
        loss = loss_fn(y_pred, processed_replay_y)
        loss.backward()
        optimizer.step()
        optimizer.zero_grad()

        if count % 1 == 0:
            print("iteration: {0}, objective value: {1}, def obj: {2}, att obj: {3}, loss: {4}".format(count, obj, cp_def_obj, cp_att_obj, loss))
        

if __name__ == "__main__":
    iterations = 1000
    learning_rate = 1e-4
    n = 20
    p = 0.3
    R = 3
    resource_list = [Resource(8,0.3), Resource(6,0.5), Resource(4,0.7)]
    G = nx.random_geometric_graph(n, p).to_directed()

    # embedding_list = [GraphEmbedding(G) for i in range(R)]
    entire_replay_memory = []

    nn_model = NNModel()
    embedding_model = GCN()
    loss_fn = torch.nn.MSELoss(reduction='sum')
    optimizer = torch.optim.Adam(list(nn_model.parameters()) + list(embedding_model.parameters()), lr=learning_rate)

    for i in range(iterations):
        playGame(n, p, R, resource_list, G, nn_model, loss_fn, embedding_model, optimizer, entire_replay_memory)
        # playGame(n, p, R, resource_list, G, nn_model, embedding_model, loss_fn, embedding_list, entire_replay_memory)


