import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import fj.data.vector.V2;
import java_cup.reduce_action;
import polyglot.visit.FlowGraph;

import java.util.Set;
import java.util.TreeSet;
import soot.*;
import soot.jimple.AnyNewExpr;
import soot.jimple.FieldRef;

public class Solver {
	public HashMap<Integer, Object> queries = new HashMap<>();
	public HashMap<Integer, String> queries_methodname = new HashMap<>();
	public HashMap<Integer, Variable> alloc_variables = new HashMap<>(); 
	public HashMap<String, Variable> variables = new HashMap<>();
	public HashMap<String, Variable> variable_fields = new HashMap<>();
	public HashMap<String, Variable> memory_variables = new HashMap<>();
	public List<Integer> allID = new ArrayList<>();
	public List<Integer> queryID = new ArrayList<>();
	public int cnt;
	public void addQuery(int id, Local local) {
		queries.put(new Integer(id), local);
		return;
	}
	public void addVar(Local local, int allocID, String methodName) {
		Variable variable = variables.get(local);
		if(variable == null) {
			variable = new Variable();
			variable.memorySet.add(new Integer(allocID));
		}
		else {
			variable.memorySet.add(new Integer(allocID));
		}
		//System.out.println(allocID + variable.toString());
		variables.put(methodName + local, variable);
	}
	public void addVar(Local local, Variable variable, String methodName) {
		Variable variable2 = variables.get(local);
		if(variable2 != null) {
			return;
		}
		else {
			variables.put(methodName + local, variable);
		}
		//System.out.println(local.toString() + variable.toString());
	}
	public void solve() {
		for(Entry<Integer, Object> e:queries.entrySet()) {
			System.out.println(e.getKey() + " " + e.getValue());
			//System.out.println(e.getValue());
		}
		System.out.println("*******************************");
		for(Entry<String, Variable> e:variables.entrySet()) {
			System.out.println(e.getValue());
			//System.out.println(e.getValue());
		}
		System.out.println("*******************************");
		for(Entry<String, Variable> e:variable_fields.entrySet()) {
			System.out.println(e.getValue());
			//System.out.println(e.getValue());
		}
		System.out.println("*******************************");
		for(Entry<Integer, Variable> e:alloc_variables.entrySet()) {
			System.out.println(e.getKey() + " " + e.getValue());
			//System.out.println(e.getValue());
		}
		/*for(Entry<Integer, Variable> e:alloc_variables.entrySet()) {
			for(Entry<String, Variable> e1:variables.entrySet()) {
				if(e1.getValue().assignVariable.contains(e.getValue()))
					e1.getValue().memorySet.add(e.getKey());
			}
			for(Entry<String, Variable> e1:variable_fields.entrySet()) {
				if(e1.getValue().assignVariable.contains(e.getValue()))
					e1.getValue().memorySet.add(e.getKey());
			}
		}*/
		/*Set<Variable> whole = new TreeSet<Variable>();
		whole.addAll(variables.values());
		whole.addAll(variable_fields.values());
		boolean flag = true;
		while (flag) {
			flag = false;
			for(Variable v1:whole) {
				for(Variable v:v1.assignVariable) {
					if(!v.isField && !v1.isField) {
						if(!v1.memorySet.containsAll(v.memorySet)) {
							flag = true;
							v1.memorySet.addAll(v.memorySet);
						}
					}
					if(v.isField && !v1.isField) {
						Variable tmp = variables.get(v.field_var);
						for(Integer x:tmp.memorySet) {
							if(memory_variables.containsKey(x.toString() + v.field.getField().toString())) {
								Variable tmp1 = memory_variables.get(x.toString() + v.field.getField().toString());
								if(!v1.memorySet.containsAll(tmp1.memorySet)) {
									flag = true;
									v1.memorySet.addAll(tmp1.memorySet);
								}
							}
							else {
								Variable tmp1 = new Variable();
								memory_variables.put(x.toString() + v.field.getField().toString(), tmp1);
							}
						}
					}
					if(!v.isField && v1.isField) {
						//System.out.println("----" + v1.field_var);
						Variable tmp = variables.get(v1.field_var);
						for(Integer x:tmp.memorySet) {
							if(memory_variables.containsKey(x.toString() + v1.field.getField().toString())) {
								Variable tmp1 = memory_variables.get(x.toString() + v1.field.getField().toString());
								if(!tmp1.memorySet.containsAll(v.memorySet)) {
									flag = true;
									tmp1.memorySet.addAll(v.memorySet);
								}
							}
							else {
								flag = true;
								Variable tmp1 = new Variable();
								tmp1.memorySet.addAll(v.memorySet);
								//System.out.println(v1.field.getField());
								memory_variables.put(x.toString() + v1.field.getField().toString(), tmp1);
							}
						}
					}
					if(v.isField && v1.isField) {
						Variable tmp = variables.get(v.field_var);
						Variable tmp1 = variables.get(v1.field_var);
						for(Integer x1:tmp1.memorySet) {
							for(Integer x:tmp.memorySet) {
								String key1 = x1.toString() + v1.field.getField().toString();
								String key = x.toString() + v.field.getField().toString();
								if(memory_variables.containsKey(key1) && memory_variables.containsKey(key)){
									Variable tmp2 = memory_variables.get(x1);
									Variable tmp3 = memory_variables.get(x);
									if(!tmp2.memorySet.containsAll(tmp3.memorySet)) {
										flag = true;
										tmp2.memorySet.addAll(tmp3.memorySet);
									}
								}
								else if(!memory_variables.containsKey(key1) && memory_variables.containsKey(key)) {
									flag = true;
									Variable tmpn = new Variable();
									Variable tmp2 = memory_variables.get(key);
									tmpn.memorySet.addAll(tmp2.memorySet);
									memory_variables.put(x1.toString() + v1.field.getField().toString(), tmpn);
								}
								else if(memory_variables.containsKey(key1) && !memory_variables.containsKey(key)) {
									Variable tmpn = new Variable();
									memory_variables.put(x.toString() + v.field.getField().toString(), tmpn);
								}
								else {
									Variable tmpn1 = new Variable();
									Variable tmpn2 = new Variable();
									memory_variables.put(key, tmpn2);
									memory_variables.put(key1, tmpn1);
								}
							}
						}
					}
				}
			}
		}*/
		try {
			FileWriter fileWriter = new FileWriter(new File("result.txt"));
			for(Entry<Integer, Object> e:queries.entrySet()) {
				System.out.print(e.getKey() + ":");
				fileWriter.write(e.getKey() + ":");
				if(e.getValue() instanceof Local) {
					Variable tmp = variables.get(queries_methodname.get(e.getKey()) + (Local)e.getValue());
					for(Integer x:tmp.memorySet) {
						System.out.print(x + " ");
						fileWriter.write(x + " ");
					}
				}
				if(e.getValue() instanceof FieldRef) {
					Variable variable = variable_fields.get(queries_methodname.get(e.getKey()) + (FieldRef)e.getValue());
					for(Integer x:variable.memorySet) {
						Variable tmp = memory_variables.get(x.toString() + variable.field.getField().toString());
						for(Integer y:tmp.memorySet) {
							System.out.println(y + " ");
							fileWriter.write(y + " ");
						}
					}
				}
				fileWriter.write("\n");
				System.out.println();
			}	
			fileWriter.flush();
			fileWriter.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		for(Entry<String, Variable> e:variables.entrySet()) {
			System.out.print(e.getKey() + " ");
			for(Integer x:e.getValue().memorySet) {
				System.out.print(x + " ");
			}
			System.out.println();
			//System.out.println(e.getValue());
		}
		for(Entry<String, Variable>e:memory_variables.entrySet()) {
			System.out.print(e.getKey());
			for(Integer x:e.getValue().memorySet) {
				System.out.print(x + " ");
			}
			System.out.println();
		}
	}
	public void solve_e() {
	    allID.add(0);
		for(int i = 0; i < cnt ; i ++){
		    System.out.print(queryID.get(i) + ":");
		    for(int elm:allID){
				System.out.print(elm + " ");
			}
			System.out.print("\n");
		}
	}
}
