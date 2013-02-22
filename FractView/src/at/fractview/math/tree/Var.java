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
	private int index;
	
	// TODO: What about negative indices?
	
	public Var(String id, int index) {
		this.id = id;
		this.index = index;
	}
	
	public Var(String id) {
		this(id, 0);
	}
	
	public boolean is(String id) {
		return index == 0 && isIndexed(id);
	}
	
	public boolean isIndexed(String id) {
		return this.id.equalsIgnoreCase(id);
	}

	@Override
	public Cplx eval(Cplx dest, Map<Var, Cplx> values) {
		return dest.set(values.get(this));
	}

	@Override
	public Expr derive(String v) {
		// Exclude zr and zi
		if(is(v)) {
			return index != 0 ? null : new Num(1);
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
		return isIndexed(v) ? index + 1 : 0;
	}

	public int index() {
		return index;
	}

	public String indexedId() {
		return id + index;
	}

	public String id() {
		return id;
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
			
		if(this.is(v.id)) return this.index - v.index;
		else return id.compareTo(v.id); // They are not equal, so we dont care about case.
	}
	
	public int hashCode() {
		return id.toLowerCase(Locale.US).hashCode() + index;
	}
	
	public String toString() {
		return index == 0 ? id() : indexedId();
	}
}