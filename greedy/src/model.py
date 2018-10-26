import numpy as np
import networkx as nx
import matplotlib.pyplot as plt
import time
import cplex
import random

class Resource:
    def __init__(self, size, prob, price=1):
        self.size = size
        self.prob = prob
        self.price = price

    def feasibleAction(self, G, edge_set):
        actions = set()
        for edge in edge_set:
            actions |= set(G.neighbors(edge[0]) + G.neighbors(edge[1]))
        return actions

class AttackerStrategy:
    def __init__(self, path):
        self.path = path

class DefenderStrategy:
    def __init__(self, coverage):
        self.coverage = coverage

class GameModel:
    def __init__(self, n=200, p=0.125, T=3, resource_list=None):
        self.n = n
        self.p = p
        self.G = nx.random_geometric_graph(self.n, self.p)
        self.T = T
        self.terminal_list = np.random.choice(self.n, self.T, replace=False)

        if resource_list is not None:
            self.resource_list = resource_list
        else:
            self.resource_list = [(Resource(1,1), 5)] # list[i] is a tuple of resource and number of this particular resource

        self.A = []
        self.D = []

    def updateAttackerStrategy(self, a):
        self.A.append(a)

    def updateDefenderStrategy(self, d):
        self.D.append(d)


class Solver:
    def __init__(self, model):
        self.model = model

