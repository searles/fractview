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
