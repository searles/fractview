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

import at.fractview.math.Affine;
import at.fractview.math.Cplx;
import at.fractview.math.Spline;
import at.fractview.math.colors.Palette;
import at.fractview.modes.orbit.functions.Function;

public class EscapeTime extends OrbitFactory {

	public static enum Type { 
		Bailout , Epsilon , Lake 
	};
	
	private double bailout;
	private double epsilon;
	
	private Function function;
	
	private OrbitToFloat.Predefined bailoutDrawingMethod;
	private OrbitToFloat.Predefined lakeDrawingMethod;
	
	private Palette bailoutPalette;
	private Palette lakePalette;
	
	public EscapeTime(Affine affine, int maxIter, Function function, 
			double bailout, OrbitToFloat.Predefined bailoutDrawingMethod, Palette bailoutPalette,
			double epsilon, OrbitToFloat.Predefined lakeDrawingMethod, Palette lakePalette) {
		super(affine, maxIter);
		
		this.function = function;
		this.bailout = bailout;
		this.epsilon = epsilon;

		this.bailout = bailout;
		this.epsilon = epsilon;
		
		this.bailoutDrawingMethod = bailoutDrawingMethod;
		this.lakeDrawingMethod = lakeDrawingMethod;
		
		this.bailoutPalette = bailoutPalette;
		this.lakePalette = lakePalette;
	}
	
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

	public OrbitToFloat.Predefined bailoutDrawingMethod() {
		return bailoutDrawingMethod;
	}

	public Palette bailoutPalette() {
		return bailoutPalette;
	}

	public double epsilon() {
		return epsilon;
	}

	public OrbitToFloat.Predefined lakeDrawingMethod() {
		return lakeDrawingMethod;
	}

	public Palette lakePalette() {
		return lakePalette;
	}

	@Override
	public EscapeTime newAffineInstance(Affine affine) {
		return new EscapeTime(affine, this.maxIter(), this.function, 
				this.bailout, this.bailoutDrawingMethod, this.bailoutPalette,
				this.epsilon, this.lakeDrawingMethod, this.lakePalette);
	}
	
	@Override
	public EscapeTime newMaxIterInstance(int maxIter) {
		return new EscapeTime(this.affine(), maxIter, this.function, 
				this.bailout, this.bailoutDrawingMethod, this.bailoutPalette,
				this.epsilon, this.lakeDrawingMethod, this.lakePalette);
	}

	public EscapeTime newFunctionInstance(Function function) {
		return new EscapeTime(this.affine(), this.maxIter(), function, 
				this.bailout, this.bailoutDrawingMethod, this.bailoutPalette,
				this.epsilon, this.lakeDrawingMethod, this.lakePalette);
	}
	
	public EscapeTime newBailoutInstance(double bailout, OrbitToFloat.Predefined bailoutDrawingMethod, Palette bailoutPalette) {
		return new EscapeTime(this.affine(), this.maxIter(), function, 
				bailout, bailoutDrawingMethod, bailoutPalette,
				this.epsilon, this.lakeDrawingMethod, this.lakePalette);
	}

	public EscapeTime newLakeInstance(double epsilon, OrbitToFloat.Predefined lakeDrawingMethod, Palette lakePalette) {
		return new EscapeTime(this.affine(), this.maxIter(), function, 
				this.bailout, this.bailoutDrawingMethod, this.bailoutPalette,
				epsilon, lakeDrawingMethod, lakePalette);
	}

	public class Orbit extends AbstractOrbit {
		// For thread safety, one might think of making this class static
		// and adding an orbit. Especially interesting if you think of functions
		private Type type;
		
		protected void generate() {
			type = Type.Lake;
			
			for(length = function.init(orbit, c); length < maxIter() - 1; length++) {
				function.step(orbit, length - 1, c); // the parameter is the last calculated value

				Cplx z = orbit[length];
				
				double bailoutValue = z.absSqr();

				if(bailoutValue >= bailout * bailout) {
					type = Type.Bailout; // repelling point
					return;
				}

				if(length >= 1) {
					double epsilonValue = z.distSqr(orbit[length - 1]);
						
					if(epsilonValue < epsilon * epsilon) {
						type = Type.Epsilon; // constant point
						return;
					}
				}				
			}
		}
		
		public int color() {
			if(type == Type.Bailout) {
				return bailoutPalette.color(bailoutDrawingMethod.value(this));
			} else {
				// Lake
				return lakePalette.color(lakeDrawingMethod.value(this));
			}
		}
		
		public float smooth() {
			// Linear interpolation, smoothened by logarithm if possible
			double y = bailout();
			double y0 = length() > 0 ? get(length() - 1).abs() : 0;
			double y1 = get(length()).abs();
			
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
