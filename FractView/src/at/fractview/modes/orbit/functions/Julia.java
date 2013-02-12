package at.fractview.modes.orbit.functions;

import java.util.Iterator;

import at.fractview.math.Cplx;

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
