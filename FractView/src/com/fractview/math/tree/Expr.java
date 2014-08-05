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
package com.fractview.math.tree;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.fractview.math.Cplx;

public abstract class Expr implements Comparable<Expr> {
	
	@SuppressWarnings("unused")
	private static final String TAG = "Expr";
	
	/*static Expr create(Parser p, String id, List<Expr> args) {
		return create(p, id, args.toArray(new Expr[args.size()]));
	}
	

	
	static Expr create(Parser p, String id, Expr...args) {
		for(Expr arg : args) {
			if(arg == null) return null;
		}
		
		if(args.length == 0) {
			return create(id);
		}
		
		if(args.length == 1 || args.length == 2) {
			try {
				// TODO: This is not perfect, I know, but there is no
				// other way known to me to find out whether 
				// sym is in the enum or not.
				Op op = Enum.valueOf(Op.class, id.toUpperCase(Locale.US));
				
				if(op.arity() == args.length) {
					return op.app(args);
				}
			} catch(IllegalArgumentException e) {
				// Okay, not a known unary symbol.
			}
		}

		if(id.equalsIgnoreCase("horner")) {
			Expr z = new Var("z");
			
			Expr result = new Num(1);

			for(Expr arg : args) {
				Expr pre = Op.MUL.app(result, z);
				result = Op.ADD.app(pre, arg);
			}
			
			return result;
		}
		
		if(id.equalsIgnoreCase("nova") && args.length == 3) {
			Var z = new Var("z");
			
			Expr da = args[0].diffZ();

			if(da == null) return null;

			Expr fract = Op.DIV.app(args[0], da);
			
			Expr RFract = Op.MUL.app(args[1], fract);
			Expr newton = Op.SUB.app(z, RFract);
			
			return Op.ADD.app(newton, args[2]);
		}
		
		if(id.equalsIgnoreCase("newton") && args.length == 1) {
			Var z = new Var("z");

			Expr da = args[0].diffZ();
			
			if(da == null) return null;
			
			Expr fract = Op.DIV.app(args[0], da);
			
			return Op.SUB.app(z, fract);
		}
		
		if(args.length == 1) {
			// "z" is a special case
			if(id.equalsIgnoreCase("z")) {
				Expr arg = args[0];
				
				// TODO: This is a bit a hack, but I think this is the most userfriendly way 
				// to deal with such things...
				if(arg.isApp(Op.ADD)) {
					// is it -index + n?
					Expr nNegIndex = arg.get(0);
					Expr nVar = arg.get(1);

					if(nVar instanceof Var && ((Var) nVar).is("n")) {
						// we might have a winner
						if(nNegIndex.isNum()) {
							Num nIndex = (Num) nNegIndex;
							
							if(nIndex.isInt()) {
								int index = -nIndex.intValue(); // negate!
								
								if(index >= 0) {
									// z(n-1)
									return new Indexed.ZN(index);
								}
							}
						}
					}
				}
			}

			return Op.MUL.app(Expr.create(id), args[0]);
		}
		
		// And now some predefined fractal types
		if(args.length == 0) {
			if(id.equalsIgnoreCase("mandelbrot")) {
				return Op.ADD.app(Op.SQR.app(new Var("z")), new Var("c"));
			}
			
			// TODO: Create table with predefined fractals
		}
		
		p.reportError("Unknown symbol: " + id);
		// Unknown symbol - return null
		return null;
	}*/
	
	static Op createOp(String id) {
		try {
			return Enum.valueOf(Op.class, id.toUpperCase(Locale.US));
		} catch(IllegalArgumentException e) {
			// Okay, not a known unary symbol.
			return null;
		}
	}
	
	static Expr createVar(String id) {
		return new Var(id);
	}
	
	public abstract boolean isNum();
	public abstract boolean isNum(double re, double im);
	
	public abstract boolean isApp();
	public abstract boolean isApp(Op op);
	
	public abstract Expr get(int i);
	
	public abstract Cplx eval(Cplx dest, Map<Var, Cplx> values);

	public abstract Set<Var> parameters(Set<Var> vars);

	/**************** Methods for symbolic computation, independent of interpreter ******************/

	/**
	 * @param v TODO
	 * @param var Variable for which we are differentiating
	 * @return d.this / d.var, or null if the expression cannot be differentiated
	 */
	public abstract Expr derive(String v);
	
	/**
	 * @param v TODO
	 * @param var
	 * @return true, if this tree contains the variable var.
	 */
	public abstract boolean contains(String v);	
	
	public abstract int maxIndex(String v);

	public abstract int hashCode();
	
	protected abstract int typeIndex();
	
	protected abstract int cmp(Expr that);
	
	@Override
	public final int compareTo(Expr that) {
		int delta = this.typeIndex() - that.typeIndex();
		
		if(delta != 0) {
			return delta;
		} else {
			return cmp(that);
		}
	}
	
	public boolean equals(Object that) {
		if(this == that) {
			return true;
		}
		
		if(that instanceof Expr) {
			// null is not an instance of Expr
			return this.compareTo((Expr) that) == 0;
		}
		
		return false;
	}
	
	/** Attempts to find a root using newton's method. In order to be less sensitive to
	 * bad initial guesses, an error value is introduced if the newton value does not converge
	 * @param x
	 * @param x0 Initial value. This value will be modified!
	 * @param values
	 * @param epsilon
	 * @return
	 *
	public Cplx findRoot(Var v, Map<Var, Cplx> values, double epsilon, int maxIter) {
		// if the variable does not occur in expression
		if(!this.containsZ()) {
			Log.d(TAG, this + " does not contain " + v);
			return null;
		}
		
		// if we cannot derive it
		Expr df = this.diffZ();
		
		if(df == null) {
			Log.d(TAG, "we did not get a derivation");
			return null;
		}
		
		// if there are some unknown variables
		Map<Var, Cplx> map = new TreeMap<Var, Cplx>();
		map.putAll(values);
		map.put(v, new Cplx(0, 0));
		
		if(this.eval(map) == null) {
			// If all variables are defined, the function returns a value (which might be Infinity or NaN).
			Log.d(TAG, "Well, there are some variable values for which I don't have any information");
			return null;
		}
		
		Cplx[] pop = new Cplx[8];
		
		pop[0] = new Cplx(0, 0);
		pop[1] = new Cplx(1, 0);
		pop[2] = new Cplx(-1, 0);
		pop[3] = new Cplx(0, 1);
		pop[4] = new Cplx(0, -1);
		
		// And some random values
		Random rnd = new Random();
		for(int i = 5; i < pop.length; i++) {
			pop[i] = new Cplx(rnd.nextGaussian(), rnd.nextGaussian());
		}
		
		// Okay, we can derive it and we know all values, so try to find a root, starting from v0
		for(Cplx x0 : pop) {
			map.put(v, x0);
			Cplx c = newtonApproximate(this, df, x0, map, epsilon, maxIter);
			
			if(c != null) {
				return c;
			}
		}
		
		Log.d(TAG, "Maximal number of iterations reached without a result...");
		// We did not find a root in time...
		return null;
	}
	
	private static Cplx newtonApproximate(Expr f, Expr df, Cplx x0, Map<Var, Cplx> map, double epsilon, int maxIter) {
		for(int i = 0; i < maxIter; i++) {
			double lastDistSqr;
			double distSqr = Double.POSITIVE_INFINITY;

			Cplx fx0;
			
			do {
				lastDistSqr = distSqr;
				
				fx0 = f.eval(map);
				
				if(fx0.isInfinite() || fx0.isNaN()) {
					// Okay, that was a bad guess...
					Log.d(TAG, x0 + " was a bad guess: f(x) returns Infinite or NaN");
					return null;
				}

				Cplx dfx0 = df.eval(map);
				
				// Calculate next newton value
				Cplx nextX0 = new Cplx().sub(x0, new Cplx().div(fx0, dfx0));
				
				if(nextX0.isInfinite() || nextX0.isNaN()) {
					Log.d(TAG, x0 + " was a bad guess: Next value would be Infinite or NaN.");
					return null;
				}

				// Otherwise check whether newton-method seem to converge
				distSqr = x0.distSqr(nextX0); // How much closer are we now?
				
				x0.set(nextX0); // Override in array and in map
			} while(distSqr < lastDistSqr); // we do this as long as we converge
			
			if(fx0.absSqr() < epsilon * epsilon) {
				// We found a solution.
				Log.d(TAG, "Found a solution: f(" + x0 + ") = " + fx0);
				return x0;
			}
		}
		
		return null;
	}
	
	/*public static class Log {
		public static void v(String s, String t) {
			System.out.println(t);
		}
	}*/
}
