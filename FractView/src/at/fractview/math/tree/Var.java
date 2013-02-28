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

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import at.fractview.math.Cplx;

public class Var extends Expr {

	private String id;
	
	// TODO: What about negative indices?
	
	@SuppressWarnings("unused")
	private Var() {} // For GSon

	public Var(String id) {
		this.id = id;
	}
	
	public boolean is(String id) {
		return this.id.equalsIgnoreCase(id);
	}
	
	public String id() {
		return id;
	}
	
	public Var prefix() {
		int i;		
		for(i = id.length(); i > 0 && Character.isDigit(id.charAt(i - 1)); i--);
		
		return i < id.length() ? new Var(id.substring(0, i)) : this;
	}

	public Integer index() {
		int i;		
		for(i = id.length(); i > 0 && Character.isDigit(id.charAt(i - 1)); i--);
		
		if(i < id.length()) {
			try {
				return Integer.parseInt(id.substring(i));
			} catch(NumberFormatException e) {
				// Ignore - invalid number.
			}
		}
		
		return null;
	}

	@Override
	public Cplx eval(Cplx dest, Map<Var, Cplx> values) {
		return dest.set(values.get(this));
	}

	@Override
	public Expr derive(String v) {
		if(is(v)) {
			return new Num(1);
		} else if(prefix().is(v)) {
			// Cannot derive things like z[0].
			return null;
		} else {
			return new Num(0);
		}
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
	public boolean contains(String v) {
		return this.is(v);
	}
	
	@Override
	public int maxIndex(String v) {
		if(prefix().is(v)) {
			Integer index = index();
			
			if(index == null) index = 0;
			
			return index + 1;
		} else {
			return 0;
		}
	}

	@Override
	public Set<Var> parameters(Set<Var> vars) {
		vars.add(this);
		return vars;
	}

	protected int typeIndex() {
		return 2;
	}

	@Override
	protected int cmp(Expr that) {
		Var v = (Var) that;
		return id.compareTo(v.id); // They are not equal, so we dont care about case.
	}
	
	public int hashCode() {
		return id.toLowerCase(Locale.US).hashCode();
	}
	
	public String toString() {
		return id();
	}
}