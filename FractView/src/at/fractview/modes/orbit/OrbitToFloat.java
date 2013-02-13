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

import at.fractview.modes.orbit.OrbitFactory.AbstractOrbit;

public interface OrbitToFloat {
	
	// TODO: Split into bailout and lake things and common things
	
	float value(OrbitFactory.AbstractOrbit orbit);

	public static enum Predefined implements OrbitToFloat {
		LengthSmooth {
			@Override
			public float value(AbstractOrbit orbit) {
				// Log as transfer
				float smooth = ((EscapeTime.Orbit) orbit).smooth();
	
				double d = orbit.length() + smooth;
	
				return (float) Math.log(d + 1); // Logarithmic transfer
			}
		},
		Length {
			@Override
			public float value(AbstractOrbit orbit) {
				return orbit.length();
			}
		},
		SumExp {
			@Override
			public float value(AbstractOrbit orbit) {
				double degree = 0;
	
				for(int i = 1; i < orbit.length(); i++) {
					degree += Math.exp(-orbit.get(i).absSqr() - 0.5 / orbit.get(i - 1).distSqr(orbit.get(i)));
				}
	
				return (float) Math.log(degree + 1);
			}
		},		
		SumDelta {		
			@Override
			public float value(AbstractOrbit orbit) {
				double sum = 0.;
	
				for(int i = 0; i < orbit.length() - 1; i++) {
					sum += Math.log(orbit.get(i + 1).distSqr(orbit.get(i)) + 1);
				}
	
				return (float) Math.log(sum + 1); // Logarithmic transfer
			}
		},
		LastArc {
			@Override
			public float value(AbstractOrbit orbit) {
				return (float) orbit.get(orbit.length() - 1).arc();
			}
		},
		Zero {
			@Override
			public float value(AbstractOrbit orbit) {
				return 0;
			}
		}/*
		LastRad {
			@Override
			public float value(AbstractOrbit orbit) {
				return (float) Math.log(orbit.get(orbit.length() - 1).abs() + 1);
			}
		},
		LastX {
			@Override
			public float value(AbstractOrbit orbit) {
				return (float)orbit.get(orbit.length() - 1).re();
			}
		},
		LastY {
			@Override
			public float value(AbstractOrbit orbit) {
				return (float) orbit.get(orbit.length() - 1).im();
			}
		},
		Curvature {
			int m = 2;
			
			float t(OrbitFactory.AbstractOrbit orbit, int i) {
				Cplx z = orbit.get(i);
				Cplx zp = orbit.get(i - 1);
				Cplx zpp = orbit.get(Math.max(0, i - 2));
	
				double d0 = new Cplx().sub(z, zp).arc();
				double d1 = new Cplx().sub(zp, zpp).arc();
	
				return (float) Math.cos(d0 - d1);
			}
			
			@Override
			public float value(AbstractOrbit orbit) {
				float sum = 0;
	
				for(int i = m + 1; i < orbit.length(); i++) {
					sum += t(orbit, i);				
				}
	
				float last = t(orbit, orbit.length());
	
				float d = ((EscapeTime.Orbit) orbit).smooth();
	
				return sum + last * d;
			}
		},
		TriangleInequality {
			int m = 1;
				
			float t(OrbitFactory.AbstractOrbit orbit, int i) {
				Cplx z = orbit.get(i);
				Cplx zp = orbit.get(i - 1);
	
				float zAbs = (float) z.abs();
				float zpAbs = (float) zp.abs();
	
				float cAbs = (float) orbit.c().abs();
	
				float m = Math.abs(zpAbs * zpAbs - cAbs);
				float M = Math.abs(zpAbs * zpAbs + cAbs);
	
				float d = (zAbs - m) / (M - m);
	
				// d should and will usually range between 0 and 1,
				// but for some fixed points it will increase or decrease
				// very quickly, so I normalize it
	
				return Math.min(Math.max(d, 0), 1);
			}
	
			@Override
			public float value(AbstractOrbit orbit) {
				float sum = 0;
	
				for(int i = m + 1; i < orbit.length(); i++) {
					sum += t(orbit, i);				
				}
	
				float last = t(orbit, orbit.length());
	
				float d = ((EscapeTime.Orbit) orbit).smooth();
	
				return sum + last * d;
			}
		}*/
	}
	;
}
