from __future__ import division
from __future__ import print_function

import time
import argparse
import numpy as np

import torch
import torch.nn.functional as F
import torch.optim as optim

import networkx as nx
from torch_geometric.nn import GCNConv

class GCN(torch.nn.Module):
    def __init__(self):
        super(GCN, self).__init__()
        self.num_features = 7 # TODO
        self.intermediate_size = 16
        self.num_classes = 5 # TODO

        self.conv1 = GCNConv(self.num_features, self.intermediate_size, cached=True)
        self.conv2 = GCNConv(self.intermediate_size, self.num_classes, cached=True)
        # self.conv1 = ChebConv(data.num_features, 16, K=2)
        # self.conv2 = ChebConv(16, data.num_features, K=2)

    def forward(self, original_x, edge_index):
        # x, edge_index = data.x, data.edge_index
        x = F.relu(self.conv1(original_x, edge_index))
        x = F.dropout(x, training=self.training)
        x = self.conv2(x, edge_index)
        return F.log_softmax(x, dim=1)
