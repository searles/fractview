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
package at.fractview.modes.orbit;

import android.graphics.Bitmap;
import at.fractview.math.Affine;
import at.fractview.math.Cplx;
import at.fractview.modes.RasterTask;
import at.fractview.modes.RasterTask.Environment;
import at.fractview.modes.ScaleablePrefs;

public abstract class OrbitFactory extends ScaleablePrefs implements RasterTask.Rasterable {

	private int maxIter;
	
	public OrbitFactory(Affine affine, int maxIter) {
		super(affine);
		this.maxIter = maxIter;
	}

	public abstract OrbitFactory newMaxIterInstance(int maxIter);
	
	public int maxIter() {
		return maxIter;
	}
	
	public RasterTask calculateInBackground(Bitmap bitmap) {
		RasterTask task = new RasterTask(this);
		task.start(bitmap);
		return task;
	}
	
	
	@Override
	public RasterTask.Environment createEnvironment() {
		return new Environment() {
			AbstractOrbit orbit = createOrbit();
			
			@Override
			public int color(float x, float y, int w, int h) {
				orbit.generate(x, y, w, h);
				return orbit.color();
				/*Cplx c = new Cplx();
				map(p, w, h, c);
				
				Cplx z = new Cplx(0., 0.);
				
				int i;
				for(i = 1; i < maxLength; i++) {
					z.set(Math.abs(z.re()), z.im());
					z.add(z.sqr(z), c);
					
					if(z.absSqr() > 16) {
						break;
					}			
				}
				
				if(i < 0 || i == maxLength) {
					return 0xff000000;
				} else {
					return 0xffff0000;
				}*/
			}
		};
	}

	public abstract AbstractOrbit createOrbit();
	
	public abstract class AbstractOrbit {

		protected int length;
		protected Cplx c;
		protected Cplx[] orbit;
		
		public AbstractOrbit() {
			this.length = 0;
			this.orbit = new Cplx[maxIter];
			
			for(int i = 0; i < OrbitFactory.this.maxIter; i++) {
				orbit[i] = new Cplx();
			}
			
			this.c = new Cplx();
		}
		
		public OrbitFactory factory() {
			return OrbitFactory.this;
		}

		public void generate(float x, float y, int w, int h) {
			map(x, y, w, h, c);
			generate();
		}
		
		/**
		 * Creates the orbit, using this.c as start point
		 */
		protected abstract void generate();
		
		/**
		 * @return The color value of the current orbit
		 */
		public abstract int color();
		
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
