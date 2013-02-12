package at.fractview.math.tree;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import android.util.Log;
import at.fractview.math.Cplx;

public abstract class Expr implements Comparable<Expr> {
	
	private static final String TAG = "Expr";
	
	static Expr create(Parser p, String id, List<Expr> args) {
		return create(p, id, args.toArray(new Expr[args.size()]));
	}
	
	static Expr create(Parser p, String id, Expr...args) {
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
			
			Expr fract = Op.DIV.app(args[0], args[0].diffZ());
			
			Expr RFract = Op.MUL.app(args[1], fract);
			Expr newton = Op.SUB.app(z, RFract);
			
			return Op.ADD.app(newton, args[2]);
		}
		
		if(id.equalsIgnoreCase("newton") && args.length == 1) {
			Var z = new Var("z");

			Expr fract = Op.DIV.app(args[0], args[0].diffZ());
			
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
	}
	
	static Expr create(String id) {
		if(id.equalsIgnoreCase("pi")) {
			return new Num(Math.PI);
		}

		if(id.equalsIgnoreCase("e")) {
			return new Num(Math.E);			
		}

		if(id.equalsIgnoreCase("i")) {
			return new Num(new Cplx(0, 1));
		}
		
		if(id.length() > 2 && id.substring(0, 2).equalsIgnoreCase("zn")) {
			// "zn2" corresponds to "z(n-2)".
			
			// Is the rest a number?
			Scanner sc = new Scanner(id.substring(2));
			
			if(sc.hasNextInt()) {
				int index = sc.nextInt();
				if(!sc.hasNext()) {
					return new Indexed.ZN(index);
				}
			}
		}
	
		return new Var(id);
	}

	public abstract boolean isNum();
	public abstract boolean isNum(double re, double im);
	
	public abstract boolean isApp();
	public abstract boolean isApp(Op op);
	
	public abstract Expr get(int i);
	
	public abstract Cplx eval(Map<Var, Cplx> values);

	public abstract int compareTo(Expr expr);
	
	public abstract Set<Var> parameters(Set<Var> vars);

	/**************** Methods for symbolic computation, independent of interpreter ******************/

	/**
	 * @param var Variable for which we are differentiating
	 * @return d.this / d.var, or null if the expression cannot be differentiated
	 */
	public abstract Expr diffZ();
	
	/**
	 * @param var
	 * @return true, if this tree contains the variable var.
	 */
	public abstract boolean containsZ();	
	
	public abstract int maxIndexZ();

	public abstract int hashCode();
	
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
	 */
	public Cplx findRoot(Var v, Map<Var, Cplx> values, double epsilon, int maxIter) {
		// if the variable does not occur in expression
		if(!this.containsZ()) {
			Log.v(TAG, this + " does not contain " + v);
			return null;
		}
		
		// if we cannot derive it
		Expr df = this.diffZ();
		
		if(df == null) {
			Log.v(TAG, "we did not get a derivation");
			return null;
		}
		
		// if there are some unknown variables
		Map<Var, Cplx> map = new TreeMap<Var, Cplx>();
		map.putAll(values);
		map.put(v, new Cplx(0, 0));
		
		if(this.eval(map) == null) {
			// If all variables are defined, the function returns a value (which might be Infinity or NaN).
			Log.v(TAG, "Well, there are some variable values for which I don't have any information");
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
		
		Log.v(TAG, "Maximal number of iterations reached without a result...");
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
					Log.v(TAG, x0 + " was a bad guess: f(x) returns Infinite or NaN");
					return null;
				}

				Cplx dfx0 = df.eval(map);
				
				// Calculate next newton value
				Cplx nextX0 = new Cplx().sub(x0, new Cplx().div(fx0, dfx0));
				
				if(nextX0.isInfinite() || nextX0.isNaN()) {
					Log.v(TAG, x0 + " was a bad guess: Next value would be Infinite or NaN.");
					return null;
				}

				// Otherwise check whether newton-method seem to converge
				distSqr = x0.distSqr(nextX0); // How much closer are we now?
				
				x0.set(nextX0); // Override in array and in map
			} while(distSqr < lastDistSqr); // we do this as long as we converge
			
			if(fx0.absSqr() < epsilon * epsilon) {
				// We found a solution.
				Log.v(TAG, "Found a solution: f(" + x0 + ") = " + fx0);
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
