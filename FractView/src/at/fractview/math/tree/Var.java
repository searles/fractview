package at.fractview.math.tree;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import at.fractview.math.Cplx;

public class Var extends Expr {

	private String id;
	
	public Var(String id) {
		this.id = id;
	}
	
	public boolean is(String id) {
		return this.id.equalsIgnoreCase(id);
	}

	@Override
	public Cplx eval(Map<Var, Cplx> values) {
		return values.get(this);
	}

	@Override
	public Expr diffZ() {
		// Exclude zr and zi
		if(is("zr") || is("zi")) return null;
		return new Num(is("z") ? 1 : 0); 
	}
	
	@Override
	public boolean isNum() {
		return false;
	}

	@Override
	public boolean isNum(double re, double im) {
		return false;
	}

	@Override
	public boolean isApp() {
		return false;
	}

	@Override
	public boolean isApp(Op op) {
		return false;
	}

	@Override
	public Expr get(int i) {
		throw new IllegalArgumentException("No such element");
	}

	@Override
	public boolean containsZ() {
		return this.is("z") || this.is("zr") || this.is("zi");
	}
	
	@Override
	public int maxIndexZ() {
		return is("z") ? 1 : 0;
	}
	
	public String id() {
		return id;
	}
	
	@Override
	public Set<Var> parameters(Set<Var> vars) {
		vars.add(this);
		return vars;
	}

	@Override
	public int compareTo(Expr that) {
		if(that instanceof Num) return 1;
		
		if(that instanceof Var) {
			Var v = (Var) that;
			
			if(this.is(v.id)) return 0;
			else return id.compareTo(v.id); // They are not equal, so we dont care about case.
		}
		
		// in all other cases var is smaller
		return -1;
	}
	
	public int hashCode() {
		return id.toLowerCase(Locale.US).hashCode();
	}
	
	public String toString() {
		return id;
	}
}