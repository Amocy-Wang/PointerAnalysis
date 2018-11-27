import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.MoreObjects.ToStringHelper;

import soot.Local;
import soot.jimple.FieldRef;

public class Variable implements Comparable<Variable>{
	boolean isField = false;
	String var = null; 
	String field_var = null;
	FieldRef field;
	public Set<Integer> memorySet = new TreeSet<>();
	public Set<Variable> assignVariable = new TreeSet<>();
	public String toString() {
		String anString = "";
		if(var != null)
			anString += "variable:" + var.toString() + " ";
		else{
			if(!isField)
				anString += "variable:new ";
			else
				anString += field_var + "." + field.getField() + " "; 
		}
		for(Variable v:assignVariable) {
			//anString += v + " ";
		}
		return anString;
	}
	public int compareTo(Variable other) {
		return hashCode() - other.hashCode();
	}
	public void assign(Variable v, boolean isinScope, HashMap<String, Variable>variables, HashMap<String, Variable>memory_variables) {
		if(!isField && !v.isField) {
			if(!isinScope)
				memorySet.clear();
			if(!memorySet.containsAll(v.memorySet)) {
				memorySet.addAll(v.memorySet);
			}
		}
		if(v.isField && !isField) {
			Variable tmp = variables.get(v.field_var);
			if(!isinScope) {
				memorySet.clear();
			}
			for(Integer x:tmp.memorySet) {
				if(memory_variables.containsKey(x.toString() + v.field.getField().toString())) {
					Variable tmp1 = memory_variables.get(x.toString() + v.field.getField().toString());
					if(!memorySet.containsAll(tmp1.memorySet)) {
						memorySet.addAll(tmp1.memorySet);
					}
				}
				else {
					Variable tmp1 = new Variable();
					memory_variables.put(x.toString() + v.field.getField().toString(), tmp1);
				}
			}
		}
		if(!v.isField && isField) {
			Variable tmp = variables.get(field_var);
			if(tmp.memorySet.size() == 1 && !isinScope) {
				if(memory_variables.containsKey(tmp.memorySet.iterator().next() + field.getField().toString()))
					memory_variables.get(tmp.memorySet.iterator().next() + field.getField().toString()).memorySet.clear();
			}
			for(Integer x:tmp.memorySet) {
				if(memory_variables.containsKey(x.toString() + field.getField().toString())) {
					Variable tmp1 = memory_variables.get(x.toString() + field.getField().toString());
					if(!tmp1.memorySet.containsAll(v.memorySet)) {
						tmp1.memorySet.addAll(v.memorySet);
					}
				}
				else {
					Variable tmp1 = new Variable();
					tmp1.memorySet.addAll(v.memorySet);
					//System.out.println(v1.field.getField());
					memory_variables.put(x.toString() + field.getField().toString(), tmp1);
				}
			}
		}
		if(v.isField && isField) {
			Variable tmp = variables.get(v.field_var);
			Variable tmp1 = variables.get(field_var);
			if(tmp1.memorySet.size() == 1 && !isinScope) {
				if(memory_variables.containsKey(tmp1.memorySet.iterator().next() + field.getField().toString()))
					memory_variables.get(tmp1.memorySet.iterator().next() + field.getField().toString()).memorySet.clear();
			}
			for(Integer x1:tmp1.memorySet) {
				for(Integer x:tmp.memorySet) {
					String key1 = x1.toString() + field.getField().toString();
					String key = x.toString() + v.field.getField().toString();
					if(memory_variables.containsKey(key1) && memory_variables.containsKey(key)){
						Variable tmp2 = memory_variables.get(x1);
						Variable tmp3 = memory_variables.get(x);
						if(!tmp2.memorySet.containsAll(tmp3.memorySet)) {
							tmp2.memorySet.addAll(tmp3.memorySet);
						}
					}
					else if(!memory_variables.containsKey(key1) && memory_variables.containsKey(key)) {
						Variable tmpn = new Variable();
						Variable tmp2 = memory_variables.get(key);
						tmpn.memorySet.addAll(tmp2.memorySet);
						memory_variables.put(x1.toString() + field.getField().toString(), tmpn);
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
