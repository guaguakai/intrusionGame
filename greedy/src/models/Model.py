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
        self.path = path # a list of edges, or in other words, it needs to be a path initiated from source to one of the terminals

    def getEdges(self):
        return [(self.path[i], self.path[i+1]) for i in range(len(self.path)-1)]

class AttackerMixedStrategy:
    def __init__(self, attacker_strategy_list, prob):
        # attacker_strategy_list is a list of AttackerStrategy
        # prob is a list of non-negative number with summation 1
        assert(len(attacker_strategy_list) == len(prob))
        assert(sum(prob) == 1)

        self.attacker_strategy_list = attacker_strategy_list
        self.prob = prob
        self.length = len(prob)

class DefenderStrategy:
    def __init__(self, coverage):
        self.coverage = coverage
        # a dictionary: edge -> recourse id that covers this edge
        # This should the resource order

class DefenderMixedStrategy:
    def __init__(self, defender_strategy_list, prob):
        # defender_strategy_list is a list of DefenderStrategy
        # prob is a list of non-negative number with summation 1
        assert(len(defender_strategy_list) == len(prob))
        assert(sum(prob) == 1)

        self.defender_strategy_list = defender_strategy_list
        self.prob = prob
        self.length = len(prob)

class GameModel:
    def __init__(self, n=200, p=0.3, T=3, S=1, resource_list=None):
        self.n = n
        self.p = p
        self.G = nx.random_geometric_graph(self.n, self.p)
        self.T = T
        self.S = S

        random_targets = np.random.choice(self.n, self.T + self.S, replace=False)
        self.terminal_list = random_targets[:self.T]
        self.node2terminalId = {}
        for i in range(len(self.terminal_list)):
            self.node2terminalId[self.terminal_list[i]] = i

        self.terminal_payoff = np.random.random(self.T) * 10
        self.source_list = random_targets[-self.S:]

        if resource_list is not None:
            self.resource_list = resource_list
        else:
            self.resource_list = [(Resource(1,0.5))] * 5 # list[i] is a tuple of resource (coverage, prob) and number of this particular resource

        self.defender_strategy_size = 5
        self.defender_strategy_set = [self.randomDefenderStrategy() for i in range(5)]
        # self.defender_mixed_strategy = DefenderMixedStrategy(self.defender_strategy_set, prob=[1])
        
        # self.attacker_strategy_set = [self.randomAttackerStrategy()]
        self.attacker_strategy_size = self.T
        self.attacker_strategy_set = [self.shortestPathAttackerStrategy(self.source_list[0], self.terminal_list[j]) for j in range(self.T)] # Testing... TODO
        # self.attacker_mixed_strategy = AttackerMixedStrategy(self.attacker_strategy_set, prob=[1]) # Testing... TODO

        self.payoff_matrix = self.computePayoffMatrix(self.defender_strategy_set, self.attacker_strategy_set)
        # self.payoff_matrix = np.array([[self.pureStrategyPayoff(self.defender_strategy_set[0], self.attacker_strategy_set[0])]])

    def pureStrategyPayoff(self, d, a):
        resource_coverage = set()
        for e in a.getEdges():
            if e in d.coverage:
                resource_coverage |= d.coverage[e]

        success_prob = 1 # probability of successfully attacking
        for i in resource_coverage:
            success_prob *= (1 - self.resource_list[i].prob)

        payoff_value = - success_prob * self.terminal_payoff[self.node2terminalId[a.path[-1]]]
        # TODO: to decide how to store the terminal payoff, whether to use node2terminalId: ideally not, but just easy to debug
        return payoff_value

    def mixedStrategyPayoff(self, recompute_payoff_matrix=False, defender_mixed_strategy=None, attacker_mixed_strategy=None):
        if not recompute_payoff_matrix:
            assert(self.payoff_matrix.shape == (defender_mixed_strategy.length, attacker_mixed_strategy.length))
            payoff_value = np.dot(defender_mixed_strategy.prob, np.dot(self.payoff_matrix, attacker_mixed_strategy.prob))
            return payoff_value
        else:
            print("TODO...")
            # TODO...

    def computePayoffMatrix(self, defender_strategy_set, attacker_strategy_set):
        defender_strategy_size = len(defender_strategy_set)
        attacker_strategy_size = len(attacker_strategy_set)
        payoff_matrix = np.zeros((defender_strategy_size, attacker_strategy_size))
        for i in range(defender_strategy_size):
            for j in range(attacker_strategy_size):
                payoff_matrix[i][j] = self.pureStrategyPayoff(defender_strategy_set[i], attacker_strategy_set[j])

        return payoff_matrix

    def randomAttackerStrategy(self):
        print("TODO...")
        # TODO...

    def randomDefenderStrategy(self):
        coverage = {}
        for i in range(len(self.resource_list)):
            n1 = np.random.randint(self.n)
            # n2 = G.neighbors(n1)[np.random.randint(len(G.neighbors(n1)))]
            n2 = list(self.G.neighbors(n1))[np.random.randint(len(list(self.G.neighbors(n1))))]
            if (n1, n2) in coverage:
                coverage[(n1, n2)] |= {i}
            if (n2, n1) in coverage:
                coverage[(n2, n1)] |= {i}
                
            coverage[(n1, n2)] = {i}
            coverage[(n2, n1)] = {i}

        return DefenderStrategy(coverage)
        print("TODO...")
        # TODO...

    def shortestPathAttackerStrategy(self, source, terminal):
        return AttackerStrategy(path=nx.shortest_path(self.G, source, terminal))

    def updateAttackerStrategy(self, a):
        self.attacker_strategy_set.append(a)
        self.attacker_strategy_size += 1
        self.payoff_matrix = np.concatenate((self.payoff_matrix, np.reshape([self.pureStrategyPayoff(d, a) for d in self.defender_strategy_set], (self.defender_strategy_size, 1))), axis=1)

    def updateDefenderStrategy(self, d):
        self.defender_strategy_set.append(d)
        self.defender_strategy_size += 1
        self.payoff_matrix = np.concatenate((self.payoff_matrix, np.reshape([self.pureStrategyPayoff(d, a) for a in self.attacker_strategy_set], (1, self.attacker_strategy_size))), axis=0)

    def drawGraph(self):
        color_map = ["green"]*self.n
        pos = {}
        for i in range(self.n):
            pos[i] = tuple(np.random.random(2))
        for i in range(self.S):
            color_map[self.source_list[i]] = "blue"
            pos[self.source_list[i]] = (0, i * 1.0 / len(self.source_list))
        for i in range(self.T):
            color_map[self.terminal_list[i]] = "red"
            pos[self.terminal_list[i]] = (1, i * 1.0 / len(self.terminal_list))
        nx.draw(self.G, node_color=color_map, with_labels=True, pos=pos)
        plt.show()

if __name__ == "__main__":
    gameModel = GameModel(n=20)
    print(gameModel.attacker_strategy_set[0].path)
    print(gameModel.defender_strategy_set[0].coverage)
    print(gameModel.terminal_payoff)
    print(gameModel.payoff_matrix)
    # gameModel.drawGraph()
