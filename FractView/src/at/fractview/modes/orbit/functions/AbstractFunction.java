package at.fractview.modes.orbit.functions;

import at.fractview.math.Cplx;
import at.fractview.modes.PointParameters;

public interface AbstractFunction extends PointParameters {
	
	/** Initializes the orbit, i.e., fills the first values so that step can succeed (in detail, puts the z(0), z(1), ... values)
	 * @param orbit
	 * @param c
	 * @return The number of values that were written in the orbit.
	 */
	int init(Cplx[] orbit, Cplx c);

	/** Calculates one step of the zs-sequence, in detail after execution z[n+1] will contain the appropriate value.
	 * @param zs
	 * @param n
	 * @param c
	 */
	void step(Cplx[] zs, int n, Cplx c);	
}