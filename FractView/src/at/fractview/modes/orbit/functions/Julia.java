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

import java.util.Iterator;

import at.fractview.math.Cplx;

// TODO: Include julia sets into GUI.
public class Julia implements AbstractFunction {
	public static final String JULIA_LABEL = "Julia-Point"; // TODO: Externalize
	
	private AbstractFunction function;
	
	private Cplx p;
	
	public Julia(AbstractFunction function, Cplx p) {
		if(function instanceof Julia) {
			throw new IllegalArgumentException("Cannot create julia-set of julia-set");
		}
		
		this.p = new Cplx(p);
		this.function = function;
	}
	
	@Override
	public Iterable<String> labels() {
		return new Iterable<String>() {

			@Override
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					
					Iterator<String> functionIterator = null;
					
					@Override
					public boolean hasNext() {
						return functionIterator == null || functionIterator.hasNext();
					}

					@Override
					public String next() {
						if(functionIterator == null) {
							functionIterator = function.labels().iterator();
							
							// Return Julia parameter
							return JULIA_LABEL;
						}
						
						return functionIterator.next();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	@Override
	public Cplx get(String s) {
		return function.get(s);
	}

	@Override
	public void set(String s, Cplx p) {
		if(s.equalsIgnoreCase(JULIA_LABEL)) {
			this.p.set(p);
		} else {
			function.set(s, p);
		}
	}

	@Override
	public int init(Cplx[] orbit, Cplx c) {
		int length = function.init(orbit, c);
		orbit[0].set(c);
		
		return length;
	}

	@Override
	public void step(Cplx[] zs, int n, Cplx c) {
		function.step(zs, n, p);
	}
}
