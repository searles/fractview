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
package at.fractview.math.tree;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import at.fractview.math.Cplx;

public class App extends Expr {
	
	private Op op;
	private Expr[] args;
	
	public App(Op op, Expr...args) {
		if(op.arity() != args.length) {
			throw new IllegalArgumentException("Arity and number of arguments do not match");
		}

		this.op = op;
		this.args = args;		
	}
	
	public Op op() {
		return op;
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
		return true;
	}

	@Override
	public boolean isApp(Op op) {
		return this.op == op;
	}

	@Override
	public Expr get(int i) {
		return args[i];
	}

	@Override
	public Cplx eval(Map<Var, Cplx> values) {
		Cplx[] cArgs = new Cplx[op.arity()];
		
		for(int i = 0; i < op.arity(); i++) {
			Cplx arg = args[i].eval(values);
			
			if(arg == null) return null;
			
			cArgs[i] = arg;
		}
		
		return op.eval(cArgs);
	}

	@Override
	public Expr diffZ() {
		return op.diffZ(args);
	}

	@Override
	public boolean containsZ() {
		for(Expr arg : args) {
			if(arg.containsZ()) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public int maxIndexZ() {
		int max = 0;
		
		for(Expr arg : args) {
			int i = arg.maxIndexZ();
			
			if(i > max) max = i;
		}
		
		return max;
	}
	
	@Override
	public Cplx findRoot(Var v, Map<Var, Cplx> values, double epsilon, int maxIter) {
		if(op == Op.MUL) {
			// Find root of all components that contain v
			for(int i = 0; i < args.length; i++) {
				if(args[i].containsZ()) {
					Cplx c = args[i].findRoot(v, values, epsilon, maxIter);
					
					if(c != null && !c.isNaN() && !c.isInfinite()) {
						return c;
					}
				}
			}
		}
		
		if(op == Op.DIV) {
			return args[0].findRoot(v, values, epsilon, maxIter);
		}
		
		return super.findRoot(v, values, epsilon, maxIter);
	}
	
	@Override
	public Set<Var> parameters(Set<Var> vars) {
		for(int i = 0; i < args.length; i++) {
			args[i].parameters(vars);
		}
		
		return vars;
	}
	
	protected int typeIndex() {
		return 0;
	}

	@Override
	public int cmp(Expr that) {
		App app = (App) that;
			
		// Use compare of enum
		int cmp = op.compareTo(app.op);
			
		if(cmp == 0) {
			for(int i = 0; i < op.arity(); i++) {
				cmp = args[i].compareTo(app.args[i]);
				// If we find a subtree that is not equal, bail out.
				if(cmp != 0) return cmp;
			}
		}
			
		return 0;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(args) + op.hashCode();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(op.toString());
		
		if(args.length > 0) {
			sb.append("(");
			
			for(int i = 0; i < args.length; i++) {
				if(i > 0) sb.append("; ");
				sb.append(args[i]);
			}
			
			sb.append(")");
		}
		
		return sb.toString();
	}
}
