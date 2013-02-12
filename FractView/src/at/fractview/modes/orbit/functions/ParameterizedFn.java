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

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import at.fractview.math.Cplx;

public abstract class ParameterizedFn implements AbstractFunction {
	
	private Map<String, Integer> parameterMap; // map from label to index in parameters-array	
	protected Cplx[] parameters;

	protected ParameterizedFn(List<String> labels) {
		// Executables have been created, now we need to take care of parameters:
		this.parameterMap = new TreeMap<String, Integer>();
		this.parameters = new Cplx[labels.size()];

		// Iterate through all labels
		for(ListIterator<String> i = labels.listIterator(); i.hasNext();) {
			int index = i.nextIndex();
			String p = i.next();
			
			this.parameters[index] = new Cplx(); // Create an entry in array
			this.parameterMap.put(p, index); // and put it into map.
		}
	}
	
	protected ParameterizedFn(String...labels) {
		this(Arrays.asList(labels));
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
	
	
	
}
