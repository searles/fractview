/*
: * This file is part of FractView.
 *
 * FractView is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FractView is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FractView.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.fractview.modes.orbit.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import at.fractview.math.Cplx;
import at.fractview.math.tree.Expr;
import at.fractview.math.tree.ExprCompiler;
import at.fractview.math.tree.Var;
import at.fractview.tools.Labelled;

/**
 * This class represents a specification of functions. 
 */
public class Function {
	// Here we store expressions and parameters along with input string.
	// We will use these data to reproduce data for the input dialog.
	
	// The labels here are NOT descriptions of the data but the user input.
	private Labelled<Expr> function;
	private ArrayList<Labelled<Expr>> inits;
	
	// TODO: Gson failed me on the tree set, so I split it up
	private ArrayList<Var> parameterNames;
	private ArrayList<Labelled<Cplx>> parameters;
	//private TreeMap<Var, Labelled<Cplx>> parameters; // labelled.label is NOT the name of the parameter, but the expression-string (eg. sqrt 5).
	
	@SuppressWarnings("unused")
	private Function() {} // For GSon
	
	public Function(Labelled<Expr> function, List<Labelled<Expr>> inits, Map<Var, Labelled<Cplx>> parameters) {
		// Verify whether this is a valid specification
		if(function.get().maxIndex("z") != inits.size()) {
			throw new IllegalArgumentException("Number of inits is not correct");
		}
		
		for(int i = 0; i < inits.size(); i++) {
			if(inits.get(i).get().maxIndex("z") - 1 > i) { // - 1 because z(1) may use z(n - 1)
				throw new IllegalArgumentException("Init " + i + " requires z-value that only is defined later");
			}
		}
		
		// Fetch all parameters
		Set<Var> required = function.get().parameters(new TreeSet<Var>());

		for(Labelled<Expr> init : inits) {
			init.get().parameters(required);
		}
		
		ExprCompiler.removePredefinedVars(required);
		
		if(!required.equals(parameters.keySet())) {
			throw new IllegalArgumentException("Some variables were not defined: " + required + " vs " + parameters.keySet());
		}
		
		this.function = function;

		this.inits = new ArrayList<Labelled<Expr>>(inits.size());
		this.inits.addAll(inits);

		//this.parameters = new TreeMap<Var, Labelled<Cplx>>();
		
		this.parameterNames = new ArrayList<Var>();
		this.parameters = new ArrayList<Labelled<Cplx>>();
		
		for(Map.Entry<Var, Labelled<Cplx>> entry : parameters.entrySet()) {
			this.parameterNames.add(entry.getKey());
			this.parameters.add(entry.getValue());
		}
		
		//this.parameters.putAll(parameters);
	}
	
	public ExecutableFunction create() {
		return ExecutableFunction.create(this);
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
	
	public List<Var> parameters() {
		return parameterNames;
	}
	
	public Labelled<Cplx> parameter(Var v) {
		return parameters.get(parameterNames.indexOf(v));
	}

	public String toDescription() {
		return function.label();
	}
}