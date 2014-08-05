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
package com.fractview.modes.orbit;

import com.fractview.math.Affine;
import com.fractview.math.Cplx;
import com.fractview.modes.ScaleablePrefs;

public abstract class AbstractOrbitPrefs extends ScaleablePrefs {

	public static final int LENGTH_MASK = 0x00ffffff;
	public static final int MAX_LENGTH = 0x01000000; // Maximum orbit length. 

	private int maxIter;

	protected AbstractOrbitPrefs() {} // For GSon
	
	public AbstractOrbitPrefs(Affine affine, int maxIter) {
		super(affine);
		this.maxIter = Math.min(maxIter, MAX_LENGTH);
	}

	public abstract AbstractOrbitPrefs newMaxIterInstance(int maxIter);
	
	public int maxIter() {
		return maxIter;
	}
	

	/** This method is useful if the gui wants to show the orbit on mouseover
	 * @return
	 */
	public abstract AbstractOrbit createOrbit();
	
	public abstract class AbstractOrbit {

		protected int length;
		protected Cplx c;
		protected Cplx[] orbit;
		
		public AbstractOrbit() {
			this.length = 0;
			this.orbit = new Cplx[maxIter];
			
			for(int i = 0; i < AbstractOrbitPrefs.this.maxIter; i++) {
				orbit[i] = new Cplx();
			}
			
			this.c = new Cplx();
		}
		
		public AbstractOrbitPrefs factory() {
			return AbstractOrbitPrefs.this;
		}

		public void generate(int x, int y, int w, int h) {
			map(x, y, w, h, c);
			generate();
		}
		
		/**
		 * Creates the orbit, using this.c as start point
		 */
		protected abstract void generate();
		
		/**
		 * @param i
		 * @return The ith point of the orbit
		 */
		public Cplx get(int i) {
			return orbit[i];
		}

		public int length() {
			return length;
		}
		
		public Cplx c() {
			return c;
		}
		
		public double absSqr(int i) {
			return orbit[i].absSqr();
		}
		
		public double distSqr(int i) {
			return orbit[i-1].distSqr(orbit[i]);
		}
	}
}
