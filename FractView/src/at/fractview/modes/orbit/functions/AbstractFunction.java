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