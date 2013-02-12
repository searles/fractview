package at.fractview.modes.orbit.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import at.fractview.math.Cplx;
import at.fractview.math.tree.Expr;
import at.fractview.math.tree.ExprCompiler;
import at.fractview.math.tree.Var;
import at.fractview.tools.Labelled;

/**
 * This class represents a specification of functions. 
 */
public class Specification {
	// Here we store expressions and parameters along with input string.
	// We will use these data to reproduce data for the input dialog.
	
	// The labels here are NOT descriptions of the data but the user input.
	private Labelled<Expr> function;
	private ArrayList<Labelled<Expr>> inits;
	private TreeMap<Var, Labelled<Cplx>> parameters; // labelled.label is NOT the name of the parameter, but the expression-string (eg. sqrt 5).
	
	public Specification(Labelled<Expr> function, List<Labelled<Expr>> inits, Map<Var, Labelled<Cplx>> parameters) {
		// Verify whether this is a valid specification
		if(function.get().maxIndexZ() != inits.size()) {
			throw new IllegalArgumentException("Number of inits is not correct");
		}
		
		for(int i = 0; i < inits.size(); i++) {
			if(inits.get(i).get().maxIndexZ() - 1 > i) { // - 1 because z(1) may use z(n - 1)
				throw new IllegalArgumentException("Init " + i + " requires z-value that only is defined later");
			}
		}
		
		// Fetch all parameters
		Set<Var> required = function.get().parameters(new TreeSet<Var>());

		for(Labelled<Expr> init : inits) {
			init.get().parameters(required);
		}
		
		// There are some predefined variables, we remove them here:
		required.removeAll(ExprCompiler.predefinedVars);
		
		if(!required.equals(parameters.keySet())) {
			throw new IllegalArgumentException("Some variables were not defined: " + required + " vs " + parameters.keySet());
		}
		
		this.function = function;
		this.inits = new ArrayList<Labelled<Expr>>(inits.size());
		this.parameters = new TreeMap<Var, Labelled<Cplx>>();
		
		this.inits.addAll(inits);
		this.parameters.putAll(parameters);
	}
	
	public Function create() {
		return Function.create(this);
	}
	
	public Labelled<Expr> function() {
		return function;
	}
	
	public int initsSize() {
		return inits.size();
	}
	
	public Labelled<Expr> init(int index) {
		return inits.get(index);
	}
	
	public Set<Var> parameters() {
		return parameters.keySet();
	}
	
	public Labelled<Cplx> parameter(Var v) {
		return parameters.get(v);
	}
}