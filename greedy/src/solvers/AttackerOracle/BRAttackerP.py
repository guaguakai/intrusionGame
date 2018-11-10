from docplex.cp.model import CpoModel
from docplex.cp.parameters import CpoParameters
import numpy as np
import networkx as nx

params = CpoParameters(LogVerbosity="Quiet")

def BRAttackerP(gameModel):
    source = gameModel.source_list[0]
    edges = list(gameModel.G.edges())
    nodes = list(gameModel.G.nodes())

    defender_coverage = gameModel.defender_strategy_set
    defender_prob = gameModel.defender_prob
    assert(len(defender_coverage) == len(defender_prob))

    resource_list = gameModel.resource_list
    edge2index = gameModel.edge2index

    solution_list = []
    # =================== attacker CP ==================
    for j in range(len(gameModel.terminal_list)):
        # ---------------- tarminal j ------------------
        target = gameModel.terminal_list[j]
        cpo = CpoModel()
        cpo.set_parameters(params)

        edge_variables = cpo.binary_var_list(gameModel.m, "gamma")
        cpo.add(sum([edge_variables[edge2index[source_out]] for source_out in gameModel.G.out_edges(source)]) == 1)
        cpo.add(sum([edge_variables[edge2index[target_in]] for target_in in gameModel.G.in_edges(target)]) == 1)
        for node in nodes:
            if node == source or node == target:
                continue
            else:
                cpo.add(sum([edge_variables[edge2index[in_edge]] for in_edge in gameModel.G.in_edges(node)]) == sum([edge_variables[edge2index[out_edge]] for out_edge in gameModel.G.out_edges(node)])) # incoming flow == outgoing flow

        # -------------- computing the objective value -------------
        objective_value = 0
        for i in range(len(defender_coverage)):
            success_prob = 1
            for e_i in range(len(defender_coverage[i].resource_usage)):
                covering_prob = gameModel.resource_list[e_i].prob # covering prob
                success_prob *= (1 - edge_variables[e_i] * covering_prob)
            objective_value += success_prob * defender_prob[i] * gameModel.terminal_payoff[j]

        cpo.add(cpo.maximize(objective_value))
        cpo_solution = cpo.solve()
        if cpo_solution:
            edge_usage = [edges[i] for i in range(gameModel.m) if cpo_solution[edge_variables[i]] == 1]
            obj = cpo_solution.get_objective_values()

            solution_list.append((obj, edge_usage, target))
        else:
            print("no solution")

    best_solution = max(solution_list, key=lambda x: x[0])
    best_target = best_solution[2]
    new_obj = best_solution[0]
    new_G = nx.DiGraph(best_solution[1])
    new_path = nx.shortest_path(new_G, source, best_target)
    return new_obj, new_path





