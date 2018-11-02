  // --------------------------------------------------------------------------
  // File: examples/src/QPex1.java
  // Version 8.1
  // --------------------------------------------------------------------------
  //  Copyright (C) 2001-2002 by ILOG.
  //  All Rights Reserved.
  //  Permission is expressly granted to use this example in the
  //  course of developing applications that use ILOG products.
  // --------------------------------------------------------------------------
  //
  // QPex1.java - Entering and optimizing a QP problem
  
  import ilog.concert.*;
  import ilog.cplex.*;
  
  
  public class test {
     public static void main(String[] args) {
        try {
           IloCplex cplex = new IloCplex();
           IloLPMatrix lp = populateByRow(cplex);
  
           if ( cplex.solve() ) {
              double[] x     = cplex.getValues(lp);
              double[] dj    = cplex.getReducedCosts(lp);
              double[] pi    = cplex.getDuals(lp);
              double[] slack = cplex.getSlacks(lp);
  
              System.out.println("Solution status = " + cplex.getStatus());
              System.out.println("Solution value  = " + cplex.getObjValue());
  
              int ncols = lp.getNcols();
              for (int j = 0; j < ncols; ++j) {
                 System.out.println("Column: " + j +
                                    " Value = " + x[j] +
                                    " Reduced cost = " + dj[j]);
              }
  
              int nrows = lp.getNrows();
              for (int i = 0; i < nrows; ++i) {
                 System.out.println("Row   : " + i +
                                    " Slack = " + slack[i] +
                                    " Pi = " + pi[i]);
              }
  
              cplex.exportModel("qpex1.lp");
           }
           cplex.end();
        }
        catch (IloException e) {
           System.err.println("Concert exception '" + e + "' caught");
        }
     }
  
     static IloLPMatrix populateByRow(IloMPModeler model) throws IloException {
        IloLPMatrix lp = model.addLPMatrix();
  
        double[]    lb = {0.0, 0.0, 0.0};
        double[]    ub = {40.0, Double.MAX_VALUE, Double.MAX_VALUE};
        IloNumVar[] x  = model.numVarArray(model.columnArray(lp, 3), lb, ub);
  
        double[]   lhs = {-Double.MAX_VALUE, -Double.MAX_VALUE};
        double[]   rhs = {20.0, 30.0};
        double[][] val = { {-1.0,  1.0,  1.0},
                           { 1.0, -3.0,  1.0} };
        int[][]    ind = { {0, 1, 2},
                           {0, 1, 2} };
        lp.addRows(lhs, rhs, ind, val);
  
        // Q = 0.5 ( 33*x0*x0 + 22*x1*x1 + 11*x2*x2 - 12*x0*x1 - 23*x1*x2 )
        IloNumExpr x00 = model.prod( 33, model.square(x[0]));
        IloNumExpr x11 = model.prod( 22, model.square(x[1]));
        IloNumExpr x22 = model.prod( 11, model.square(x[2]));
        IloNumExpr x01 = model.prod(-12, model.prod(x[0], x[1]));
        IloNumExpr x12 = model.prod(-23, model.prod(x[1], x[2]));
        IloNumExpr Q   = model.prod(0.5, model.sum(x00, x11, x22, x01, x12));
  
        double[] objvals = {1.0, 2.0, 3.0};
        model.add(model.maximize(model.diff(model.scalProd(x, objvals), Q)));
  
        return (lp);
     }
  }
