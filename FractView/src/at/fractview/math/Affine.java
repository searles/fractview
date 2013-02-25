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
package at.fractview.math;


public class Affine {
	
	public static Affine scalation(double sx, double sy) {
		return new Affine(new double[]{
				sx, 0, 0, 
				0, sy, 0});
	}
	
	public static Affine rotation(double arc) {
		double c = Math.cos(arc);
		double s = Math.sin(arc);
		
		return new Affine(new double[]{
				c, -s, 0, 
				s, c, 0});
	}
	
	public static Affine translation(double tx, double ty) {
		return new Affine(new double[]{
				1, 0, tx, 
				0, 1, ty});
	}
	
	public static Affine create(double...m) {
		// TODO: Copy of array!
		return new Affine(m);
	}
	
	private double[] m;
	private double det;
	
	private Affine(double...m) {
		this.m = m;
		update();		
	}
	
	public Affine concat(Affine a0, Affine a1) {
		double m0 = a0.m[0] * a1.m[0] + a0.m[1] * a1.m[3];
		double m1 = a0.m[0] * a1.m[1] + a0.m[1] * a1.m[4];
		double m2 = a0.m[0] * a1.m[2] + a0.m[1] * a1.m[5] + a0.m[2];
		double m3 = a0.m[3] * a1.m[0] + a0.m[4] * a1.m[3];
		double m4 = a0.m[3] * a1.m[1] + a0.m[4] * a1.m[4];
		double m5 = a0.m[3] * a1.m[2] + a0.m[4] * a1.m[5] + a0.m[5];
		
		m[0] = m0;
		m[1] = m1;
		m[2] = m2;
		m[3] = m3;
		m[4] = m4;
		m[5] = m5;
		
		update();
		
		return this;
	}
	
	private void update() {
		this.det = m[0] * m[4] - m[1] * m[3];
	}
	
	public Affine preConcat(Affine a) {
		return this.concat(a, this);
	}

	public Affine postConcat(Affine a) {
		return this.concat(this, a);
	}
	
	public double[] get() {
		return new double[]{m[0], m[1], m[2], m[3], m[4], m[5]};
	}
	
	/*public Cplx map(Cplx src, Cplx dest) {
		dest.set(
				m[0] * src.re() + m[1] * src.im() + m[2],
				m[3] * src.re() + m[4] * src.im() + m[5]
		);
		
		return dest;
	}*/
	
	public Cplx invmap(double x, double y, Cplx dest) {
		x -= m[2];
		y -= m[5];
		
		dest.set(
				(m[0] * x + m[3] * y) / det,
				(m[1] * x + m[4] * y) / det
		);
		
		return dest;
	}

	public Cplx map(double x, double y, Cplx dest) {
		dest.set(
				m[0] * x + m[1] * y + m[2],
				m[3] * x + m[4] * y + m[5]
		);
		
		return dest;
	}
}
