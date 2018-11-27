import java.util.*;

import jas.Method;
import java_cup.runtime.virtual_parse_stack;
import soot.*;
import soot.jimple.AnyNewExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Switch;

public class Transformer extends SceneTransformer{
	int allocateID;
	HashMap<String, Integer> graphnum = new HashMap<>();
	HashMap<String, HashMap<Unit, Integer>> methodUnit = new HashMap<>();
	public Solver solver = new Solver();
	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) {
		 try{
			 SootMethod mainMethod = Scene.v().getMainMethod();
			 SolveMethod(mainMethod, null, null, null);
			 solver.solve();
		 }
		 catch(Exception e) {
			 e.printStackTrace();//System.out.println("error");
		 }
	}
	void gothroughGraph(HashMap<Unit, Integer> isinscope, UnitGraph graph, Unit unit, String method_Name, ArrayList<Unit> t) {
		t.add(unit);
		List<Unit> us = graph.getSuccsOf(unit);
		if(us.size() == 0) {
			Integer tmp = graphnum.get(method_Name);
			tmp += 1;
			graphnum.put(method_Name, tmp);
			for(Unit u:t) {
				if(isinscope.containsKey(u)){
					tmp = isinscope.get(u);
					tmp += 1;
					isinscope.put(u, tmp);
				}
				else {
					isinscope.put(u, 1);
				}
			}
			t.remove(t.size() - 1);
			return;
		}
		Iterator<Unit> i = us.iterator();
		while (i.hasNext()) {
			gothroughGraph(isinscope, graph, i.next(), method_Name, t);
		}
		t.remove(t.size() - 1);
	}
	void SolveMethod(SootMethod method, String lastMethodString, List<Value> args, Variable thisVar) {
		UnitGraph graph = new BriefUnitGraph(method.getActiveBody());
		if(!graphnum.containsKey(method.getSignature())) { 
			HashMap<Unit, Integer> isinscope = new HashMap<>();
			graphnum.put(method.getSignature(), new Integer(0));
			gothroughGraph(isinscope, graph, graph.getHeads().iterator().next(), method.getSignature(), new ArrayList<>());
			methodUnit.put(method.getSignature(), isinscope);
		}
		HashMap<Unit, Integer> isinscope = methodUnit.get(method.getSignature());
		for(Unit u:method.getActiveBody().getUnits()){
			System.out.println(u);
			//System.err.println(u.branches());
			System.out.println(isinscope.get(u));
			System.out.println(graphnum.get(method.getSignature()));
			SolveUnit(u, method.getSignature() + u.getJavaSourceStartLineNumber(), lastMethodString, args, thisVar, isinscope.get(u) != graphnum.get(method.getSignature()));
		}	
	}
	void SolveUnit(Unit unit, String method_Name, String lastMethodString, List<Value> args, Variable thisVar, boolean isinscope) {
		//System.out.println(unit.toString() + unit.getClass());
		try{
			if(unit instanceof IfStmt) {
				IfStmt ifStmt = (IfStmt)unit;
				System.out.println(ifStmt.getTarget());
				
			}
			if(unit instanceof IdentityStmt) {
				IdentityStmt identityStmt = (IdentityStmt)unit;
				Value lValue = identityStmt.getLeftOp();
				Value rValue = identityStmt.getRightOp();
				//右操作数为参数
				if(rValue instanceof ParameterRef) {
					//绑定引用关系
					ParameterRef pRef = (ParameterRef)rValue;
					if(args == null)
						return;
					Value tmpv = args.get(pRef.getIndex());
					Variable variable = null;
					//System.out.println(tmpv);
					if(tmpv instanceof Local)
						variable = solver.variables.get(lastMethodString + (Local)tmpv);
					if(tmpv instanceof FieldRef) 
						variable = solver.variable_fields.get(lastMethodString + (FieldRef)tmpv);
					Variable lVariable = null;
					if(lValue instanceof Local) {
						lVariable = solver.variables.get(method_Name + (Local)lValue);
					}
					if(lValue instanceof FieldRef) {
						lVariable = solver.variable_fields.get(method_Name + (FieldRef)lValue);
					}
					if(lVariable == null) {
						lVariable = new Variable();
						lVariable.var = method_Name + lValue;
						if(lValue instanceof Local)
							solver.variables.put(method_Name + lValue, lVariable);
						if(lValue instanceof FieldRef)
							solver.variable_fields.put(method_Name + lValue, lVariable);
					}
					lVariable.assignVariable.add(variable);
					lVariable.assign(variable, false, solver.variables, solver.memory_variables);
				}
				if(rValue instanceof ThisRef) {
					Variable lVariable = null;
					if(lValue instanceof Local) {
						lVariable = solver.variables.get(method_Name + (Local)lValue);
					}
					if(lValue instanceof FieldRef) {
						lVariable = solver.variable_fields.get(method_Name + (FieldRef)lValue);
					}
					if(lVariable == null) {
						System.out.println("-----" + lValue + lValue.getClass());
						lVariable = new Variable();
						lVariable.var = method_Name + lValue;
						if(lValue instanceof Local)
							solver.variables.put(method_Name + lValue, lVariable);
						if(lValue instanceof FieldRef)
							solver.variable_fields.put(method_Name + lValue, lVariable);
					}
					if(thisVar != null) {
						lVariable.assignVariable.add(thisVar);
						lVariable.assign(thisVar, false, solver.variables, solver.memory_variables);
					}
				}
				//左参数为this
			}
			else if(unit instanceof AssignStmt) {
				AssignStmt assignStmt = (AssignStmt)unit;
				Value lValue = assignStmt.getLeftOp();
				Value rValue = assignStmt.getRightOp();
				//新建对象
				//System.out.println(rValue.getClass());
				Variable rVariable = null;
				if(rValue instanceof AnyNewExpr) {
					//处理新建数组
					rVariable = new Variable();
					rVariable.memorySet.add(allocateID);
					solver.alloc_variables.put(new Integer(allocateID), rVariable);
					//solver.addVar((Local)rValue, allocateID);
					if(rValue instanceof NewArrayExpr) {}
					//添加新建变量标号
					allocateID = 0;
				}
				else if(rValue instanceof Local) {
					rVariable = solver.variables.get(method_Name + (Local)rValue);
				}
				else if(rValue instanceof FieldRef) {
					FieldRef fieldRef = (FieldRef)rValue;
					if(rValue instanceof InstanceFieldRef) {
						rVariable = solver.variable_fields.get(method_Name + fieldRef);
						if(rVariable == null) {
							System.out.println("-----" + rValue);
							rVariable = new Variable();
							rVariable.isField = true;
							rVariable.field = fieldRef;
							rVariable.field_var = method_Name + (Local)((InstanceFieldRef)rValue).getBase();
							solver.variable_fields.put(method_Name + fieldRef, rVariable);
						}
					}
				}
				else if(rValue instanceof ArrayRef) {
					//数组不做处理
				}
				Variable lVariable = null;
				if(rVariable != null) {
					//System.out.println(lValue.getClass());
					if(lValue instanceof Local) {
						//System.out.println(lValue);
						lVariable = solver.variables.get(method_Name + (Local)lValue);
						if(lVariable == null) {
							System.out.println("-----" + lValue);
							lVariable = new Variable();
							lVariable.var = method_Name + (Local)lValue;
							solver.addVar((Local)lValue, lVariable, method_Name);
						}
					}
					else if(lValue instanceof FieldRef) {
						FieldRef fieldRef = (FieldRef)lValue;
						lVariable = solver.variable_fields.get(method_Name + fieldRef);
						if(lVariable == null) {
							System.out.println("-----" + lValue);
							lVariable = new Variable();
							lVariable.isField = true;
							lVariable.field = fieldRef;
							lVariable.field_var = method_Name + (Local)((InstanceFieldRef)lValue).getBase();
							solver.variable_fields.put(method_Name + fieldRef, lVariable);
						}
					}
					else if(lValue instanceof ArrayRef) {
						//数组暂不处理
					}
					else {
						//其他值
					}
					lVariable.assignVariable.add(rVariable);
					lVariable.assign(rVariable, isinscope, solver.variables, solver.memory_variables);
				}
			}
			else if(unit instanceof InvokeStmt) {
				//System.out.println("---" + unit);
				InvokeExpr invokeExpr = ((InvokeStmt)unit).getInvokeExpr();
				if(invokeExpr != null) {
					if(invokeExpr instanceof InstanceInvokeExpr) {
						//System.out.println("invoke");
						SootMethod sootMethod = invokeExpr.getMethod();
						String methodName = sootMethod.getSignature();
						//if(methodName.equals("<java.lang.Object: void <init>()>"))
							//return;
						Variable variable = solver.variables.get(method_Name + ((InstanceInvokeExpr) invokeExpr).getBase());
						SolveMethod(sootMethod, method_Name, invokeExpr.getArgs(), variable);
						//暂时未完成
					}
					else {
						String methodName = invokeExpr.getMethod().getSignature();
						List<Value> invokeArgs = invokeExpr.getArgs();
						if(methodName.equals("<benchmark.internal.Benchmark: void alloc(int)>")) {
							int allocId = ((IntConstant) invokeArgs.get(0)).value;
                            allocateID = allocId;
						}
						if(methodName.equals("<benchmark.internal.Benchmark: void test(int,java.lang.Object)>")) {
							int id = ((IntConstant) invokeArgs.get(0)).value;
                            Object local = (Object) invokeArgs.get(1);
                            solver.queries.put(id, local);
                            solver.queries_methodname.put(id, method_Name);
						}
					}
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
