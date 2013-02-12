package at.fractview.modes;

import at.fractview.math.Cplx;

public interface PointParameters {
	Iterable<String> labels();
	Cplx get(String label);
	void set(String label, Cplx c);
}
