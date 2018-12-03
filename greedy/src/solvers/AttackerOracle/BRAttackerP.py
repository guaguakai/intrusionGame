from docplex.cp.model import CpoModel
import numpy as np
import networkx as nx

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
        cpo.add_parameters(LogVerbosity="Quiet")

        if gameModel.directed:
            edge_variables = cpo.binary_var_list(gameModel.m, "gamma")
            cpo.add(sum([edge_variables[edge2index[source_out]] for source_out in gameModel.G.out_edges(source)]) - sum([edge_variables[edge2index[source_in]] for source_in in gameModel.G.in_edges(source)]) == 1)
            cpo.add(sum([edge_variables[edge2index[target_out]] for target_out in gameModel.G.out_edges(target)]) - sum([edge_variables[edge2index[target_in]] for target_in in gameModel.G.in_edges(target)]) == -1)
            for node in nodes:
                if node == source or node == target:
                    continue
                else:
                    cpo.add(sum([edge_variables[edge2index[in_edge]] for in_edge in gameModel.G.in_edges(node)]) == sum([edge_variables[edge2index[out_edge]] for out_edge in gameModel.G.out_edges(node)])) # incoming flow == outgoing flow

        else: # undirected # TODO
            edge_variables = cpo.binary_var_list(gameModel.m * 2, "gamma")
            cpo.add(sum([edge_variables[edge2index[(source, neighbor)]] for neighbor in gameModel.G.neighbors(source)]) == 1)
            cpo.add(sum([edge_variables[edge2index[(neighbor, target)]] for neighbor in gameModel.G.neighbors(target)]) == 1)
            for edge in edges:
                cpo.add(edge_variables[edge2index[edge]] + edge_variables[edge2index[edge] + gameModel.m] <= 1)
                (u, v) = edge
                if u == source or u == target or v == source or v == target:
                    continue
                else:
                    print(edge)
                    cpo.add(sum([edge_variables[edge2index[(neighbor, u)] + gameModel.m] for neighbor in gameModel.G.neighbors(u)]) == sum([edge_variables[edge2index[(v, neighbor)]] for neighbor in gameModel.G.neighbors(v)])) # incoming flow == outgoing flow
            

        # -------------- computing the objective value -------------
        objective_value = 0
        for i in range(len(defender_coverage)):
            success_prob = 1
            for r in range(gameModel.R):
                covering_prob = gameModel.resource_list[r].prob # covering prob
                # print defender_coverage[i].resource_usage
                for e_i in range(len(defender_coverage[i].resource_usage[r])):
                    e = defender_coverage[i].resource_usage[r][e_i]
                    if gameModel.directed:
                        success_prob *= (1 - edge_variables[edge2index[e]] * covering_prob)
                    else:
                        success_prob *= (1 - (edge_variables[edge2index[e]] + edge_variables[edge2index[e] + gameModel.m]) * covering_prob)
            objective_value += success_prob * defender_prob[i] * gameModel.terminal_payoff[j]

        cpo.add(cpo.maximize(objective_value))
        cpo.export_model("attacker.cpo")
        cpo_solution = cpo.solve()
        if cpo_solution:
            if gameModel.directed:
                edge_usage = [edges[i] for i in range(gameModel.m) if cpo_solution[edge_variables[i]] != 0]
            else:
                edge_usage = [edges[i] for i in range(gameModel.m) if cpo_solution[edge_variables[i]] != cpo_solution[edge_variables[i+gameModel.m]]]
            obj = cpo_solution.get_objective_values()

            solution_list.append((obj, edge_usage, target, cpo))
        else:
            print("no solution")

    best_solution = max(solution_list, key=lambda x: x[0])
    best_target = best_solution[2]
    best_cpo = best_solution[3]
    best_cpo.export_model("attacker.cpo")
    new_obj = best_solution[0]
    new_G = nx.DiGraph(best_solution[1])
    # print("edges:", new_G.edges)
    new_path = nx.shortest_path(new_G, source, best_target)
    return new_obj, new_path





