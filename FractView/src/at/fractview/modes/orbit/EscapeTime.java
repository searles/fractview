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
import at.fractview.modes.orbit.functions.Function;

public class EscapeTime extends OrbitFactory {

	public static enum Type { 
		Bailout , Epsilon , Lake 
	};
	
	private double bailout;
	private double epsilon;
	
	private Function function;
	
	private Colorization bailoutColorization;
	private Colorization lakeColorization;
	
	public EscapeTime(Affine affine, int maxLength, Function function, 
			double bailout, Colorization bailoutColorization,
			double epsilon, Colorization lakeColorization) {
		super(affine, maxLength);
		
		this.function = function;
		this.bailout = bailout;
		this.epsilon = epsilon;
		
		this.bailoutColorization = bailoutColorization;
		this.lakeColorization = lakeColorization;
	}
	
	public EscapeTime(Affine affine, int maxLength, Function function, 
			double bailout, Colorization bailoutColorization,
			double epsilon, Colorization lakeColorization, Cplx juliaParameter) {
		super(affine, maxLength);
		
		this.function = function;
		this.bailout = bailout;
		this.epsilon = epsilon;
		
		this.bailoutColorization = bailoutColorization;
		this.lakeColorization = lakeColorization;
	}
	
	@Override
	public Orbit createOrbit() {
		return new Orbit();
	}
	
	public void setFunction(Function function) {
		this.function = function;
	}
	
	public Function function() {
		return function;
	}

	public void setBailoutColorization(Colorization colorization) {
		this.bailoutColorization = colorization;
	}
	
	public Colorization bailoutColorization() {
		return bailoutColorization;
	}
	
	public void setLakeColorization(Colorization colorization) {
		this.lakeColorization = colorization;
	}
	
	public Colorization lakeColorization() {
		return lakeColorization;
	}
	
	public double bailout() {
		return bailout;
	}
	
	@Override
	public EscapeTime scaledInstance(Affine affine) {
		return new EscapeTime(affine, this.maxLength(), this.function, 
				this.bailout, this.bailoutColorization,
				this.epsilon, this.lakeColorization);
	}
	
	public class Orbit extends AbstractOrbit {
		// For thread safety, one might think of making this class static
		// and adding an orbit. Especially interesting if you think of functions
		private Type type;
		
		protected void generate() {
			type = Type.Lake;
			
			for(length = function.init(orbit, c); length < maxLength() - 1; length++) {
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
				return bailoutColorization.color(this);
			} else {
				return lakeColorization.color(this);
			}
		}
		
		public float smooth() {
			double y = 2 * Math.log(bailout()); // times two because we use absSqr

			double y0 = length() > 0 ? Math.log(get(length() - 1).absSqr()) : 0;
			double y1 = Math.log(get(length()).absSqr());

			if(y0 > 0 && y > 0 && y1 > 0) {
				// Double transfer if all values permit it
				y = Math.log(y);
				y0 = Math.log(y0);
				y1 = Math.log(y1);
			}

			return Spline.Lin.x((float) y, (float) y0, (float) y1);
		}
	}
}
