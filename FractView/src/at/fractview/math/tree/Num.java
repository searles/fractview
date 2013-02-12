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

import java.util.Map;
import java.util.Set;

import at.fractview.math.Cplx;

public class Num extends Expr {
	
	private Cplx c;

	public Num(Cplx c) {
		this.c = c;
	}
	
	public Num(double d) {
		this(new Cplx(d, 0.));
	}

	public boolean isInt() {
		return isDouble() && c.re() == (int) Math.floor(c.re());
	}
	
	public boolean isInt(int n) {
		return isInt() && c.re() == n;
	}
	
	public int intValue() {
		return (int) Math.floor(c.re());
	}
	
	public boolean isDouble() {
		return c.im() == 0.;
	}
	
	public double doubleValue() {
		return c.re();
	}
	
	public Cplx value(Map<String, Cplx> values) {
		return c;
	}

	@Override
	public Cplx eval(Map<Var, Cplx> values) {
		return new Cplx(c);
	}

	@Override
	public Expr diffZ() {
		return new Num(0);
	}

	@Override
	public boolean containsZ() {
		return false;
	}

	@Override
	public boolean isNum() {
		return true;
	}

	@Override
	public boolean isNum(double re, double im) {
		return c.re() == re && c.im() == im;
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
		throw new IllegalArgumentException("No such argument");
	}
	
	@Override
	public int maxIndexZ() {
		return 0;
	}
	
	@Override
	public Set<Var> parameters(Set<Var> vars) {
		return vars;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public int compareTo(Expr that) {
		// Number is the smallest
		if(that instanceof Num) {
			Num n = (Num)  that;
			// First sort by real value, then by imaginary value
			int r = Double.compare(c.re(), n.c.re());
			
			if(r == 0) {
				return Double.compare(c.im(), n.c.im());
			}
			
			return r;
		}

		// this < that
		return -1;
	}
	
	public String toString() {
		return isInt() ? Integer.toString(intValue()) : isDouble() ? Double.toString(doubleValue()) : c.toString();
	}
}