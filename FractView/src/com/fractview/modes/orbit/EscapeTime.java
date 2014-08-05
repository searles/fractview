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

import java.text.DecimalFormat;

import com.fractview.math.Affine;
import com.fractview.math.Cplx;
import com.fractview.math.Spline;
import com.fractview.math.colors.Palette;
import com.fractview.modes.AbstractImgCache;
import com.fractview.modes.orbit.colorization.CommonOrbitToFloat;
import com.fractview.modes.orbit.colorization.OrbitTransfer;
import com.fractview.modes.orbit.functions.ExecutableFunction;
import com.fractview.modes.orbit.functions.Function;

public class EscapeTime extends AbstractOrbitPrefs {

	// We combine number of iterations with type in the cache.
	public static final int LENGTH_MASK = 0x00ffffff;
	public static final int TYPE_MASK = 0xff000000;
	
	public static final int BAILOUT_TYPE = 0x01000000;
	public static final int LAKE_TYPE = 0x02000000;
	
	private double bailout;
	private double epsilon;
	
	private Function function;
	private ExecutableFunction internalFunction;
	
	private CommonOrbitToFloat bailoutMethod;
	private CommonOrbitToFloat lakeMethod;
	
	private OrbitTransfer bailoutTransfer;
	private OrbitTransfer lakeTransfer;
	
	private Palette bailoutPalette;
	private Palette lakePalette;
	
	@SuppressWarnings("unused")
	private EscapeTime() {} // For GSon
	
	public EscapeTime(Affine affine, int maxIter, Function function, 
			double bailout, CommonOrbitToFloat bailoutMethod, OrbitTransfer bailoutTransfer, Palette bailoutPalette,
			double epsilon, CommonOrbitToFloat lakeMethod, OrbitTransfer lakeTransfer, Palette lakePalette) {
		super(affine, maxIter);
		
		this.function = function;
		this.internalFunction = function.create();
		this.bailout = bailout;
		this.epsilon = epsilon;

		this.bailout = bailout;
		this.epsilon = epsilon;
		
		this.bailoutMethod = bailoutMethod;
		this.lakeMethod = lakeMethod;
		
		this.bailoutTransfer = bailoutTransfer;
		this.lakeTransfer = lakeTransfer;
		
		this.bailoutPalette = bailoutPalette;
		this.lakePalette = lakePalette;	}	

	@Override
	public Orbit createOrbit() {
		return new Orbit();
	}
	
	public Function function() {
		return function;
	}
	
	public double bailout() {
		return bailout;
	}

	public CommonOrbitToFloat bailoutMethod() {
		return bailoutMethod;
	}

	public OrbitTransfer bailoutTransfer() {
		return bailoutTransfer;
	}

	public Palette bailoutPalette() {
		return bailoutPalette;
	}

	public double epsilon() {
		return epsilon;
	}

	public CommonOrbitToFloat lakeMethod() {
		return lakeMethod;
	}

	public OrbitTransfer lakeTransfer() {
		return lakeTransfer;
	}

	public Palette lakePalette() {
		return lakePalette;
	}
	
	/*public int color(int type, float value) {
		if(type == BAILOUT_TYPE) {
			return bailoutPalette.color(value);
		} else {
			// Lake
			return lakePalette.color(value);
		}
	}*/
	
	@Override
	public AbstractImgCache createImgCache(int width, int height) {
		return new EscapeTimeCache(this, width, height);
	}

	@Override
	public EscapeTime newAffineInstance(Affine affine) {
		return new EscapeTime(affine, this.maxIter(), this.function, 
				this.bailout, this.bailoutMethod, this.bailoutTransfer, this.bailoutPalette,
				this.epsilon, this.lakeMethod, this.lakeTransfer, this.lakePalette);
	}
	
	@Override
	public AbstractOrbitPrefs newMaxIterInstance(int maxIter) {
		return new EscapeTime(this.affine(), maxIter, this.function, 
				this.bailout, this.bailoutMethod, this.bailoutTransfer, this.bailoutPalette,
				this.epsilon, this.lakeMethod, this.lakeTransfer, this.lakePalette);
	}
	
	public String toString() {
		Cplx center = affine().center();
		DecimalFormat df = new DecimalFormat("0.00###");
		
		return "(" + function.toDescription() + ") at " + df.format(center.re()) + ", " + df.format(center.im());
	}
	
	public class Orbit extends AbstractOrbit {
		// Also store type + value here...
		private int type;
		float value;
		
		protected void generate() {
			type = LAKE_TYPE;
			
			for(length = internalFunction.init(orbit, c); length < maxIter(); length++) {
				internalFunction.step(orbit, length - 1, c); // the parameter is the last calculated value

				Cplx z = orbit[length];
				
				double bailoutValue = z.absSqr();

				if(bailoutValue >= bailout * bailout) {
					type = BAILOUT_TYPE; // repelling point
					value = bailoutMethod.value(this);
					return;
				}

				if(length >= 1) {
					double epsilonValue = z.distSqr(orbit[length - 1]);
						
					if(epsilonValue < epsilon * epsilon) {
						// constant point, use lake-parameters.
						value = lakeMethod.value(this);
						return;
					}
				}				
			}

			// Set value of lake.
			value = lakeMethod.value(this);
		}
		
		// These two values will be cached.
		public float value() {
			return value;
		}
		
		public int type() {
			return type;
		}

		// TODO: move the next method somewhere else...
		// TODO: Find some better solution for out-of-bounds
		public float smooth() {
			// Linear interpolation, smoothened by logarithm if possible
			double y = bailout();
			double y0 = length() > 0 ? get(length() - 1).abs() : 0;
			// If this is not bailout then this is the work-around.
			double y1 = length() == maxIter() ? y0 : get(length()).abs();
			
			for(int i = 0; i < 2 && y0 > 1 && y > 1 && y1 > 1; i++) {
				// Double transfer if all values permit it
				y = Math.log(y);
				y0 = Math.log(y0);
				y1 = Math.log(y1);
			}

			return Spline.Lin.x((float) y, (float) y0, (float) y1);
		}
	}
}
