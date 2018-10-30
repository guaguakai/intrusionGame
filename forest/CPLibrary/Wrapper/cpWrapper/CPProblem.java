package cpWrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lpWrapper.MIProblemCplex;
import ilog.concert.*;
import ilog.concert.cppimpl.IloRangeArray;
import ilog.cp.*;
import ilog.cplex.IloCplex;

public class CPProblem {
		
	public IloCP cp;
	protected static OBJECTIVE_TYPE objectiveType;

	public List<IloIntVar> variables;	//The Decision Variables
	protected HashMap<String,IloIntVar> varMap = new HashMap<String,IloIntVar>();
	protected HashMap<String,IloNumVar> numMap = new HashMap<String,IloNumVar>();
	
	
	protected List<IloConstraint> Constraint;
	protected List<IloNumExpr> constraints;
	protected List<Integer> constraintVal;
	
	protected List<IloNumExpr> constraintsLB;
	protected List<IloNumExpr> constraintsUP;
	
	public double runTime;
	
	//The Objective Function
	protected IloObjective objectiveFunction = null;
	
	public enum OBJECTIVE_TYPE {
		MIN, MAX
	}
	
	protected CPProblem(){

		cp = new IloCP();
		cp.setName("CPProblem");
		variables = new ArrayList<IloIntVar>();
		constraints = new ArrayList<IloNumExpr>();
		constraintsLB = new ArrayList<IloNumExpr>();
		constraintsUP = new ArrayList<IloNumExpr>();
		constraintVal = new ArrayList<Integer>();
		Constraint = new ArrayList<IloConstraint>();
		
		this.redirectOutput(null);
		
	}
	
	public void updateObjective(IloNumExpr objectiveFunctionExpr){
		
		try {
		if(objectiveFunction!=null){
			
				cp.remove(objectiveFunction);
		}
		
		switch (this.objectiveType) {
		case MAX:
			objectiveFunction = cp.addMaximize(objectiveFunctionExpr);
			break;
		case MIN:
			objectiveFunction = cp.addMinimize(objectiveFunctionExpr);
			break;
		default:
			throw new IllegalArgumentException("I don't know this type, kid!");
		}
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	public void addDecisionVar(String name, int LowerBound, int UpperBound){
		
		IloIntVar var;
		
		try {
			
			var = cp.intVar(LowerBound, UpperBound, name);
			this.variables.add(var);
			this.varMap.put(name, var);
			
		} catch (IloException e) {
			System.out.print("addDecisionVar error: ");
			e.printStackTrace();
		}
	}
	public void addNumDecisionVar(String name, int LowerBound, int UpperBound){
		
		IloNumVar var;
		
		try {
			
			var = cp.numVar(LowerBound, UpperBound, name);
			//this.variables.add(var);
			this.numMap.put(name, var);
			
		} catch (IloException e) {
			System.out.print("addDecisionVar error: ");
			e.printStackTrace();
		}
	}

	public void setDecisonVars(String[] strVar){
		for(int i =0;i<strVar.length;i++){
			this.addDecisionVar(strVar[i],0,1);
		}
	}
	
	public IloIntVar[] getDecisionVars(){
		IloIntVar[] vars = new IloIntVar[variables.size()];
		
		for(int i =0;i<variables.size();i++){
			vars[i] = variables.get(i);
		}
		
		return vars;
	}
			
	public IloNumVar getNumVar(String name){
			return numMap.get(name);
		
	}
	public IloIntVar getVar(String name){
		return varMap.get(name);
	}
	
	public void addBoundConstraint(IloNumExpr constraint, Integer lb, Integer up){
		
		try {
			
			IloConstraint cLB = cp.ge(constraint, lb);
			IloConstraint cUB = cp.le(constraint, up);
			cp.add(cLB);
			cp.add(cUB);
			constraints.add(constraint);
			constraintVal.add(lb);
			constraints.add(constraint);
			constraintVal.add(up);
			Constraint.add(cLB);
			Constraint.add(cUB);
		} catch (IloException e) {
		
			System.out.print("addConstraint error: ");
			e.printStackTrace();
		}
	}
	
	public void addUBoundConstraint(IloNumExpr constraint, Integer lb){
		
		try {
			
			IloConstraint cLB = cp.le(constraint, lb);
			cp.add(cLB);
			constraints.add(constraint);
			constraintVal.add(lb);
			Constraint.add(cLB);
		} catch (IloException e) {
		
			System.out.print("addConstraint error: ");
			e.printStackTrace();
		}
	}
	public void addLBoundConstraint(IloNumExpr constraint, Integer lb){
		
		try {
			
			IloConstraint cLB = cp.ge(constraint, lb);
			cp.add(cLB);
			constraints.add(constraint);
			constraintVal.add(lb);
			Constraint.add(cLB);
		} catch (IloException e) {
		
			System.out.print("addConstraint error: ");
			e.printStackTrace();
		}
	}
	public void addLEConstraint(IloNumExpr e1, IloNumExpr e2){
		try {
			IloConstraint c = cp.le(e1, e2);
			cp.add(c);
			constraintsLB.add(e1);
			constraintsUP.add(e2);
			Constraint.add(c);
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void addConstraint(IloNumExpr constraint, int num){

		try {
			IloConstraint c = cp.eq(constraint, num);
			cp.add(c);
			constraints.add(constraint);
			constraintVal.add(num);
			Constraint.add(c);
		} catch (IloException e) {
		
			System.out.print("addConstraint error: ");
			e.printStackTrace();
		}
	}
	public void addConstraint(IloNumExpr constraint, double num){

		try {
			IloConstraint c = cp.eq(constraint, num);
			cp.add(c);
			constraints.add(constraint);
			constraintVal.add((int)num*10);
			Constraint.add(c);
		} catch (IloException e) {
		
			System.out.print("addConstraint error: ");
			e.printStackTrace();
		}
	}
	public void addConstraint(IloNumExpr constraint, IloNumExpr constraint2){

		try {
			IloConstraint c = cp.eq(constraint, constraint2);
			cp.add(c);
			constraints.add(constraint2);
			constraintVal.add(0);
			Constraint.add(c);
		} catch (IloException e) {
		
			System.out.print("addConstraint error: ");
			e.printStackTrace();
		}
	}
	public void addConstraint(IloIntExpr constraint, int num){

		IloConstraint c = cp.eq(constraint, num);
		
		try {
			
			cp.add(c);
			constraints.add(constraint);
			constraintVal.add(num);
			Constraint.add(c);
			
		} catch (IloException e) {
			System.out.print("addConstraint error: ");
			e.printStackTrace();
		}
	}
	
	
	//SUMMATION METHODS
	
	public IloNumExpr sumVars(IloNumExpr exp1, IloNumExpr exp2){
		try {
			
			return cp.sum(exp1, exp2);
			
		} catch (IloException e) {
			System.out.print("sumVarArray error: ");
			e.printStackTrace();
		}
		return null;
	}
	
	public IloIntExpr sumVarArray(IloIntVar[] varstosum){
		try {
			
			return cp.sum(varstosum);
			
		} catch (IloException e) {
			System.out.print("sumVarArray error: ");
			e.printStackTrace();
		}
		return null;
	}
	
//	public IloNumExpr sumVarArray(List<IloIntVar> varstosum){
//		
//		IloIntVar[] arrayVars = new IloIntVar[varstosum.size()];
//		
//		for(int i=0;i<varstosum.size();i++){
//			arrayVars[i]= varstosum.get(i);
//		}
//		
//		return sumVarArray(arrayVars);
//	}
	
	public IloNumExpr sumVarArray(List<IloNumExpr> varstosum){
		
		IloNumExpr[] arrayVars = new IloNumExpr[varstosum.size()];
		
		for(int i=0;i<varstosum.size();i++){
			arrayVars[i]= varstosum.get(i);
		}
		
		try {
			return cp.sum(arrayVars);
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public IloNumExpr sumVarArray(IloNumVar[] varstosum){
		try {
			
			return cp.sum(varstosum);
			
		} catch (IloException e) {
			System.out.print("sumVarArray error: ");
			e.printStackTrace();
		}
		return null;
	}
	
	public IloNumExpr sumExprArray(IloNumExpr[] varstosum){
		try {
			
			return cp.sum(varstosum);
			
		} catch (IloException e) {
			System.out.print("sumExprArray error: ");
			e.printStackTrace();
		}
		return null;
	}
	public IloNumExpr[] subArray( IloNumExpr[] varstosum){
		IloNumExpr[] result = new IloNumExpr[varstosum.length];
		try {
			for(int i=0;i<varstosum.length;i++){
				result[i] = cp.sum(1, cp.prod(-1, varstosum[i]));
			}
			return result;
		} catch (IloException e) {
			System.out.print("scalProdSumArray error: ");
			e.printStackTrace();
		}
		
		return null;
	}
	/** Dot product of 2 arrays
	 * 
	 * @param coeffs
	 * @param varstosum
	 * @return sum of (coeffs*varstosum)
	 */
	public IloNumExpr scalProdSumArray(double[] coeffs, IloNumExpr[] varstosum){
		try {
			return cp.prod(coeffs, varstosum);
		} catch (IloException e) {
			System.out.print("scalProdSumArray error: ");
			e.printStackTrace();
		}
		
		return null;
	}
	public IloNumExpr scalProdSumArray(double[] coeffs, IloIntVar[] varstosum){
		try {
			return cp.prod(coeffs, varstosum);
			
		} catch (IloException e) {
			System.out.print("scalProdSumArray error: ");
			e.printStackTrace();
		}
		
		return null;
	}
	public IloNumExpr scalProdSumArray(List<Double> coeffs, List<IloIntVar> varstosum){
		double[] arrayCoeffs = new double[coeffs.size()];
		
		for(int i=0;i<coeffs.size();i++){
			arrayCoeffs[i]= coeffs.get(i);
		}
		
		IloIntVar[] arrayVars = new IloIntVar[varstosum.size()];
		
		for(int i=0;i<varstosum.size();i++){
			arrayVars[i]= varstosum.get(i);
		}
		
		return scalProdSumArray(arrayCoeffs, arrayVars);
	}
	
	
	//MULTIPLICATION METHODS
	
	
	/**Takes the product of a variable and a coefficient 
	 * 
	 * @param exp
	 * @param coeff
	 * @return exp*coeff
	 */
	public IloNumExpr product(IloNumExpr exp, double coeff){
		IloNumExpr product = null;
		
		try {
			product = cp.prod(coeff,exp);
	
		} catch (IloException e) {
			System.out.print("product error: ");
			e.printStackTrace();
		}
		return product;
	}
	public IloNumExpr product(IloNumExpr exp, IloNumExpr exp2){
		IloNumExpr product = null;
		
		try {
			product = cp.prod(exp,exp2);
	
		} catch (IloException e) {
			System.out.print("product error: ");
			e.printStackTrace();
		}
		return product;
	}
	/**Multiplies each index of two arrays together and then multiplies the full array 
	 * 
	 * @param varstoprod
	 * @param coeffs
	 * @return returns product of the array vars*coeffs
	 */
	public IloNumExpr productScaleArray(IloNumExpr[] varstoprod, double[] coeffs){
		IloNumExpr product = null;
		
		try {
				product = cp.prod(coeffs[0],varstoprod[0]);
		
			for(int i=1;i<varstoprod.length;i++){
				product = cp.prod(product,cp.prod(coeffs[i],varstoprod[i]));
			}
		} catch (IloException e) {
			System.out.print("prodScaleArray error: ");
			e.printStackTrace();
		}
		
		
		return product;
	}
	
	/** Takes an array input and multiplies all variables in the array together
	 * 
	 * @param varstoprod
	 * @return returns an expression equal to the product of all variables in the array
	 */
	
	public IloNumExpr productExprArray(List<IloNumExpr> varstoprod){
		
		IloNumExpr[] vars = varstoprod.toArray(new IloNumExpr[varstoprod.size()]);
		
		IloNumExpr product = vars[0];
		
		for(int i=1;i<vars.length;i++){
			try {
				product = cp.prod(product,vars[i]);
			} catch (IloException e) {
				System.out.print("productArray error: ");
				e.printStackTrace();
			}
		}
		return product;
	}
	
	public IloNumExpr productArray(IloNumExpr[] varstoprod){
		IloNumExpr product = varstoprod[0];
		
		for(int i=1;i<varstoprod.length;i++){
			try {
				product = cp.prod(product,varstoprod[i]);
			} catch (IloException e) {
				System.out.print("productArray error: ");
				e.printStackTrace();
			}
		}
		return product;
	}
	
	public IloNumExpr productArray(List<IloIntVar> vars) {
		
		IloIntVar[] varstoprod = vars.toArray(new IloIntVar[vars.size()]);
		IloNumExpr product = varstoprod[0];
		
		for(int i=1;i<varstoprod.length;i++){
			try {
				product = cp.prod(product,varstoprod[i]);
			} catch (IloException e) {
				System.out.print("productArray error: ");
				e.printStackTrace();
			}
		}
		return product;
	}
	
	//EXPONENTIATION METHODS
	
	/**
	 * raises an array of coefficients to the power of variables
	 * @param coeffs
	 * @param varstoexp
	 * @return array of coeffs^varstoexp
	 */
	public IloNumExpr power(IloNumExpr exp, int coeff){
		
		
		return cp.power(exp, coeff);
	}
	public IloNumExpr[] exponentArray(List<Double> coeffs, List<IloIntVar> varstoexp){
		
		Double[] coeff = coeffs.toArray(new Double[coeffs.size()]);
		IloIntVar[] vars = varstoexp.toArray(new IloIntVar[varstoexp.size()]);
		
		IloNumExpr[] v = new IloNumExpr[coeff.length];
		
		for(int i=0;i<coeff.length;i++){
			v[i] = cp.power(coeff[i], vars[i]);
		}
		
		return v;
	}
	public IloNumExpr[] exponentArray(double[] coeffs, IloIntVar[] varstoexp){
		
		IloNumExpr[] v = new IloNumExpr[coeffs.length];
		
		for(int i=0;i<coeffs.length;i++){
			v[i] = cp.power(coeffs[i], varstoexp[i]);
		}
		
		return v;
	}
	
	public boolean solve(){
		
		try {
			double start=System.currentTimeMillis();
			boolean solved = cp.solve();
			this.runTime = System.currentTimeMillis() - start;
			return solved;
		} catch (IloException e) {
			System.out.print("exponentArray error: ");
			e.printStackTrace();
		}
		return false;
	}
	public void redirectOutput(OutputStream stream) {
		cp.setOut(stream);
		cp.setWarning(stream);
	}
	public void writeProb(String file){
		PrintWriter writer;
		try {
			writer = new PrintWriter(file);
		
			writer.println("ObjectiveFunction: " + this.objectiveFunction + "\n");
			
			writer.println("Constraints");
			
			int i=0;
			
			for(IloConstraint expr : Constraint){
				writer.println(expr);
				writer.println();
				i++;
			}
			
			writer.close();
		
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void writeSol(String fileName) {
		try {
			cp.solution().toString();
		} catch (IloException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	public void end(){
		
		try {
			this.objectiveFunction.clearExpr();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		this.variables.clear();
		this.varMap.clear();
		this.constraints.clear();
		this.constraintVal.clear();
		
		this.cp.end();
	}

	protected void initialize() {
		if (cp != null) {
			cp.end();
		}
		cp = new IloCP();
		
		this.redirectOutput(null);
	}
	
}
