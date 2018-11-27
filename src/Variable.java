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
}
