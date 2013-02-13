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
package at.fractview.modes;

import at.fractview.math.Affine;
import at.fractview.math.Cplx;

public abstract class ScaleablePrefs implements Preferences {
	
	private Affine affine;

	public ScaleablePrefs(Affine affine) {
		this.affine = affine;
	}

	public float[] norm(float[] p, int w, int h) {
		int size = Math.min(w, h);
		
		p[0] = p[0] / (float) size + ((float) (size - w)) / (float) ((2 * size));
		p[1] = p[1] / (float) size + ((float) (size - h)) / (float) ((2 * size));
		
		return p;
	}

	public Cplx norm(float x, float y, int w, int h, Cplx c) {
		int size = Math.min(w, h);
		
		c.set(
				x / (float) size + ((float) (size - w)) / (float) ((2 * size)),
				y / (float) size + ((float) (size - h)) / (float) ((2 * size)));
		
		return c;
	}

	/*private Cplx inv(Cplx src, Cplx dest) {
		int size = Math.min(width, height);
		
		double x = src.re() - 0.5;
		x *= size;
		x += width / 2;
		
		double y = src.im() - 0.5;
		y *= size;
		y += height / 2;
		
		dest.set(x, y);
		
		return dest;
	}*/
	
	public Cplx map(float[] p, int w, int h, Cplx c) {
		norm(p, w, h);
		
		c.set(p[0], p[1]);
		return c.map(c, affine);
	}

	public Cplx map(float x, float y, int w, int h, Cplx c) {
		norm(x, y, w, h, c);
		
		return c.map(c, affine);
	}

	public ScaleablePrefs relativelyScaledInstance(float[] m) {
		// fields 6, 7, 8 are ignored since they are considered to be 0f, 0f, 1f
		double a = m[0];
		double b = m[1];
		double e = m[2];
		double c = m[3];
		double d = m[4];
		double f = m[5];
		
		Affine affine = Affine.create(a, b, e, c, d, f);
		
		affine.preConcat(this.affine);
		
		return newAffineInstance(affine);
	}

	/** Returns a copy of this preferences but with the scale set to the affine transformation given in the parameter
	 * @param affine
	 * @return
	 */
	public abstract ScaleablePrefs newAffineInstance(Affine affine);

	public Affine affine() {
		return this.affine;
	}
	
	/*public Cplx unmap(Cplx src, Cplx dest) {
		return inv(dest.invMap(src, affine), dest);
	}*/
}
