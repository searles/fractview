/*
 * This file is part of FractView.
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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import at.fractview.math.Cplx;
import at.fractview.math.tree.Executable;
import at.fractview.math.tree.ExprCompiler;
import at.fractview.math.tree.Var;

public class ExecutableFunction implements AbstractFunction {

	// private static final String TAG = "Function";
	
	private Executable function;
	private Executable[] inits;

	private Map<String, Integer> parameterMap; // map from label to index in parameters-array	
	private Cplx[] parameters;

	
	public static ExecutableFunction create(Function spec) {
		// We need to create an order of parameters and constants, therefore we collect all parameters in a list
		List<Var> parameterLabels = new LinkedList<Var>();
		List<Cplx> constants = new LinkedList<Cplx>(); // These will be used in executables.
		
		// Now generate all instruction lists, and fill parameters/constants
		List<Integer> instructionsFn = ExprCompiler.generateInstructionList(spec.function().get(), constants, parameterLabels);
		
		// Now the initalizations
		List<List<Integer>> instructionsInits = new ArrayList<List<Integer>>(spec.initsSize());

		for(int i = 0; i < spec.initsSize(); i++) {
			instructionsInits.add(ExprCompiler.generateInstructionList(spec.init(i).get(), constants, parameterLabels));
		}
		
		List<String> parameterStrings = new LinkedList<String>();
		
		for(Var v : parameterLabels) {
			parameterStrings.add(v.id());
		}

		ExecutableFunction function = new ExecutableFunction(spec, instructionsFn, instructionsInits, constants, parameterStrings);

		// Set values of parameters in the executable function we just created
		for(Var v : parameterLabels) {
			function.set(v.id(), spec.parameter(v).get());
		}
		
		return function;
	}
	
	@SuppressWarnings("unused")
	private ExecutableFunction() {} // For GSon
	
	/** This constructor takes a specification of an object and compiles it into a group of Executable that are used
	 * to calculate values faaast.
	 */
	public ExecutableFunction(Function spec, 
			List<Integer> instructionsFn, 
			List<List<Integer>> instructionsInits, 
			List<Cplx> constants, 
			List<String> parameterLabels) {
		
		// First parameters
		this.parameterMap = new TreeMap<String, Integer>();
		this.parameters = new Cplx[parameterLabels.size()];

		// Iterate through all labels
		for(ListIterator<String> i = parameterLabels.listIterator(); i.hasNext();) {
			int index = i.nextIndex();
			String p = i.next();
			
			this.parameters[index] = new Cplx(); // Create an entry in array
			this.parameterMap.put(p, index); // and put it into map.
		}
		
		// Now create executables
		Cplx[] constantsArray = constants.toArray(new Cplx[constants.size()]);

		// Since the constants are not subject to modification we can store it directly inside the executables
		this.function = new Executable(instructionsFn, constantsArray);
		
		this.inits = new Executable[instructionsInits.size()];

		for(int i = 0; i < this.inits.length; i++) {
			this.inits[i] = new Executable(instructionsInits.get(i), constantsArray);
		}
	}

	public int init(Cplx[] orbit, Cplx c) {
		for(int i = 0; i < inits.length; i++) {
			inits[i].execute(c, orbit, i - 1, parameters, orbit[i]);
		}
		
		return inits.length;
	}
	
	@Override
	public void step(Cplx[] zs, int n, Cplx c) {
		function.execute(c, zs, n, parameters, zs[n + 1]);
	}
	
	@Override
	public Iterable<String> labels() {
		return parameterMap.keySet();
	}

	@Override
	public Cplx get(String label) {
		return parameters[parameterMap.get(label)];
	}

	@Override
	public void set(String label, Cplx c) {
		parameters[parameterMap.get(label)].set(c);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		for(int i = 0; i < inits.length; i++) {
			sb.append("=== Init " + i + " ===\n");
			sb.append(inits[i]);
			sb.append("\n");
		}

		sb.append("\n=== Function ===\n");
		sb.append(function);
		sb.append("\n");
		
		sb.append("\n");
		
		return sb.toString();
	}
}
