'''
Run the graph embedding methods on Karate graph and evaluate them on 
graph reconstruction and visualization. Please copy the 
gem/data/karate.edgelist to the working directory
'''
import matplotlib.pyplot as plt
from time import time

from gem.utils      import graph_util, plot_util
from gem.evaluation import visualize_embedding as viz
from gem.evaluation import evaluate_graph_reconstruction as gr

from gem.embedding.gf       import GraphFactorization
from gem.embedding.hope     import HOPE
from gem.embedding.lap      import LaplacianEigenmaps
from gem.embedding.lle      import LocallyLinearEmbedding
from gem.embedding.node2vec import node2vec
from gem.embedding.sdne     import SDNE

import networkx as nx

class GraphEmbedding:
    def __init__(self, G, d=20):
        self.embedding = node2vec(d=d, max_iter=1, walk_len=80, num_walks=10, con_size=10, ret_p=1, inout_p=1)
        self.Y, self.t = self.embedding.learn_embedding(graph=G, edge_f=None, is_weighted=True, no_python=True)
        self.alpha = 0.05 # learning rate

    def update(self, G):
        new_Y, new_t = self.embedding.learn_embedding(graph=G, edge_f=None, is_weighted=True, no_python=True)
        self.Y = (1 - self.alpha) * self.Y + self.alpha * new_Y
        self.t = (1 - self.alpha) * self.t + self.alpha * new_t


if __name__ == "__main__":
    n = 40
    p = 0.3
    G = nx.random_geometric_graph(n, p)
    graphEmbedding = GraphEmbedding(G)
