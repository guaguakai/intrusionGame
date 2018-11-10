import cplex
import numpy as np

def CoreLP(gameModel):
    payoff_matrix = gameModel.payoff_matrix
    (d_size, a_size) = payoff_matrix.shape # defender strategy size and attacker strategy size

    cpx = cplex.Cplex()

    # cpx.parameters.mip.limits.treememory.set(8192)
    # cpx.parameters.mip.strategy.file.set(3)
    # cpx.set_log_stream(None)
    # cpx.set_error_stream(None)
    # cpx.set_warning_stream(None)
    # cpx.set_results_stream(None)

    cpx.objective.set_sense(cpx.objective.sense.maximize) # maximizing defender utility
    objective_coef = [0] * d_size + [1]
    cpx.variables.add(obj=objective_coef,
                      lb=[0.0]*d_size + [-cplex.infinity],
                      ub=[1.0]*d_size + [cplex.infinity],
                      names=["x{0}".format(x) for x in range(d_size)] + ["U"])

    cpx.linear_constraints.add(lin_expr=[[range(d_size), [1]*d_size]], senses=["E"], rhs=[1.0], names=["Prob"])
    for j in range(a_size):
        cpx.linear_constraints.add(lin_expr=[[list(range(d_size)) + [d_size], list(payoff_matrix[:,j]) + [-1]]], senses=["G"], rhs=[0.0], names=["c{0}".format(j)])

    cpx.solve()
    cpx.write("LP/1110.lp")
    obj = cpx.solution.get_objective_value()
    variables = cpx.solution.get_values()

    def_prob = np.array(variables[:d_size])
    att_prob = -np.array(cpx.solution.get_dual_values(["c{0}".format(j) for j in range(a_size)])) # the inequality direction is reverse, so we need a negation
    att_prob /= sum(att_prob)
    return def_prob, att_prob, obj

