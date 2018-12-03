import numpy as np

LargeConst = 1e5
Epsilon = 1e-5

def NNDefenderP(gameModel, embedding_list, nn_model):
    replay_memory = []

    edges = list(gameModel.G.edges())

    attacker_paths = gameModel.attacker_strategy_set
    attacker_prob = gameModel.attacker_prob
    assert(len(attacker_paths) == len(attacker_prob))

    G = gameModel.G
    R = gameModel.R
    reward_list = gameModel.reward_list
    resource_list = gameModel.resource_list
    edge2index = gameModel.edge2index

    defender_coverage = {}
    resource_usage = [[] for r in range(R)]
    while True:
        path_payoff = np.zeros(len(attacker_paths))
        # --------------- deterministic payoff computation -------------------
        for i in range(len(attacker_paths)):
            success_prob = 1
            for e in attacker_paths[i].getEdges():
                if e in defender_coverage:
                    for j in defender_coverage[e]:
                        success_prob *= (1 - resource_list[j].prob)

            path_payoff[i] = success_prob * reward_list[attacker_paths[i].getTarget()]
                    
        # --------------- stochastic payoff computation ----------------------
        # TODO
        # ======================== greedy selection ==========================
        # resource_reward_list = - LargeConst * np.ones(R)
        resource_reward_list = []
        feasible_action_list = []
        for r in range(R):
            G_copy = G.copy()
            if len(resource_usage[r]) == 0:
                feasible_actions = edges
            elif len(resource_usage[r]) >= resource_list[r].size:
                resource_reward_list.append(None)
                feasible_action_list.append(None) # WARNING: might have error
                continue
            else:
                feasible_actions = set()
                for e in resource_usage[r]:
                    (u, v) = e
                    if gameModel.directed:
                        feasible_actions |= set([(neighbor, u) for neighbor in G_copy.predecessors(u)])
                        feasible_actions |= set([(v, neighbor) for neighbor in G_copy.successors(v)])
                    else:
                        feasible_actions |= set([(neighbor, u) for neighbor in G_copy.neighbors(u)])
                        feasible_actions |= set([(v, neighbor) for neighbor in G_copy.neighbors(v)])
            feasible_actions = list(feasible_actions)
            feasible_action_size = len(feasible_actions)

            immediate_reward = np.zeros((len(edges), 1))
            for i in range(len(attacker_paths)):
                for e in attacker_paths[i].getEdges():
                    immediate_reward[edge2index[e]] += attacker_prob[i] * path_payoff[i] * resource_list[r].prob

            feasible_actions_immediate_reward = np.zeros((feasible_action_size, 1))
            for i in range(feasible_action_size):
                feasible_actions_immediate_reward[i] = immediate_reward[edge2index[feasible_actions[i]]]

            for e in edges:
                (u, v) = e
                G_copy[u][v]['weight'] = immediate_reward[edge2index[e]]

            # embedding_list[r].update(G_copy)

            embedding_vector_list = []
            graph_vec = np.sum(embedding_list[r].Y, axis=0) # sum over all nodes
            repeated_graph_vec = np.repeat(np.reshape(graph_vec, (1, len(graph_vec))), feasible_action_size, axis=0)
            feasible_actions_immediate_reward = np.reshape(feasible_actions_immediate_reward, (feasible_action_size, 1))
            for e in feasible_actions:
                (u, v) = e
                u_vec = embedding_list[r].Y[u]
                v_vec = embedding_list[r].Y[v]
                difference_vec = u_vec - v_vec
                embedding_vector_list.append(difference_vec)
            embedding_vector_list = np.array(embedding_vector_list)

            features = np.concatenate((repeated_graph_vec, embedding_vector_list, feasible_actions_immediate_reward), axis=1)
            predicted_reward = nn_model.predict(features) # feed forward, prediction
            # predicted_reward = nn_model(features) # feed forward, prediction

            tmp_max_index = np.argmax(predicted_reward)
            tmp_max_action = feasible_actions[tmp_max_index]
            feasible_action_list.append(tmp_max_action)
            resource_reward_list.append(predicted_reward[tmp_max_index])

        # print "resource reward list", resource_reward_list
        max_resource = np.argmax(resource_reward_list)
        max_reward = resource_reward_list[max_resource]
        max_action = feasible_action_list[max_resource]
        if max_action is None: # all resource are used => terminate
            print("--------------- terminate! ----------------")
            break
        else:
            replay_memory.append((max_resource, max_action, dict(defender_coverage)))
            if max_action in defender_coverage:
                defender_coverage[max_action] |= {max_resource}
            else:
                defender_coverage[max_action] = {max_resource}

            resource_usage[max_resource].append(max_action)

    # ========================= computing reward =======================
    defender_reward = 0
    for i in range(len(attacker_paths)):
        success_prob = 1
        for e in attacker_paths[i].getEdges():
            if e in defender_coverage:
                for j in defender_coverage[e]:
                    success_prob *= (1 - resource_list[j].prob)
        defender_reward += - success_prob * attacker_prob[i] * reward_list[attacker_paths[i].getTarget()]

    return defender_reward, defender_coverage, replay_memory





                



