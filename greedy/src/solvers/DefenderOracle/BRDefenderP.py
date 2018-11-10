from docplex.cp.model import CpoModel
from docplex.cp.parameters import CpoParameters
import numpy as np

params = CpoParameters(LogVerbosity="Quiet")

def BRDefenderP(gameModel):
    cpo = CpoModel()
    #cpo.add_parameters(LogVerbosity="Quiet")
    cpo.set_parameters(params)

    edges = list(gameModel.G.edges())

    attacker_path = gameModel.attacker_strategy_set
    attacker_prob = gameModel.attacker_prob
    assert(len(attacker_path) == len(attacker_prob))

    resource_list = gameModel.resource_list
    edge2index = gameModel.edge2index

    edge_variables = []
    node_variables = []
    for i in range(len(resource_list)):
        tmp_edge_variable_list = cpo.binary_var_list(gameModel.m, "lambda_{0}".format(i)) # the edge variables corresponding to resource i
        edge_variables.append(tmp_edge_variable_list)
        
        tmp_node_variable_list = cpo.binary_var_list(gameModel.n, "lambda_node_{0}".format(i)) # the node variables corresponding to resource i
        node_variables.append(tmp_node_variable_list)

        # ---------------- connected edge constraint ----------------
        for e in range(gameModel.m):
            (ns, nt) = edges[e]
            if gameModel.resource_list[i].size > 1: # only need connectivity constraints when the resource coverage size is more than 1
                ns_incoming_sum = cpo.sum([edge_variables[i][edge2index[in_edge]] for in_edge in gameModel.G.in_edges(ns)])
                nt_outgoing_sum = cpo.sum([edge_variables[i][edge2index[out_edge]] for out_edge in gameModel.G.out_edges(nt)])
                cpo.add(edge_variables[i][e] <= ns_incoming_sum + nt_outgoing_sum)

            # --------------------- node constraint ---------------------
            cpo.add(edge_variables[i][e] <= node_variables[i][ns])
            cpo.add(edge_variables[i][e] <= node_variables[i][nt])

        cpo.add(sum(edge_variables[i]) == gameModel.resource_list[i].size)
        cpo.add(sum(node_variables[i]) == gameModel.resource_list[i].size + 1)

    # ==================== compute the objective value =====================
    objective_value = 0
    for j in range(len(attacker_path)):
        success_prob = 1 # attacker success probability
        for edge in attacker_path[j].getEdges():
            e = edge2index[edge] # edge index
            for i in range(len(resource_list)):
                success_prob *= (1 - edge_variables[i][e] * resource_list[i].prob)

        objective_value += - success_prob * attacker_prob[j] * gameModel.reward_list[attacker_path[j].path[-1]] # defender payoff, therefore negation of the attacker payoff
    
    cpo.add(cpo.maximize(objective_value))
    cpo.export_model("defender.cpo")
    cpo_solution = cpo.solve()

    if cpo_solution:
        coverage = {}
        for i in range(len(resource_list)):
            for e in range(gameModel.m):
                (ns, nt) = edges[e]
                if cpo_solution[edge_variables[i][e]] == 1:
                    if (ns, nt) in coverage:
                        coverage[(ns, nt)] |= {i}
                    else:
                        coverage[(ns, nt)] = {i}
        obj = cpo_solution.get_objective_values()
        return obj, coverage

    else:
        print("defender solution not found!")

