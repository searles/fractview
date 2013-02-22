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
package at.fractview.modes.orbit.colorization;

import at.fractview.math.colors.Palette;
import at.fractview.math.colors.Palette2D;
import at.fractview.modes.orbit.OrbitFactory;
import at.fractview.modes.orbit.OrbitFactory.AbstractOrbit;

/** This interface represents functions from an orbit into a color
 * @author searles
 *
 */
public interface GeneralColorization {
	
	int color(OrbitFactory.AbstractOrbit orbit);
	
	/** This class contains a transfer function of an orbit to a
	 * double value that is then used to obtain a color value from a palette
	 * @author searles
	 *
	 */
	public static class LinearDouble implements GeneralColorization {
		
		private OrbitToFloat trans;
		private Palette palette;
		
		public LinearDouble(OrbitToFloat trans, Palette palette) {
			this.trans = trans;
			this.palette = palette;
		}

		@Override
		public int color(AbstractOrbit orbit) {
			return palette.color((float) trans.value(orbit));
		}

		public Palette palette() {
			return palette;
		}

		public Object orbitToDouble() {
			return trans;
		}
	}
	
	public static class TwoDimensional implements GeneralColorization {
		private OrbitToFloat trans0;
		private OrbitToFloat trans1;
		private Palette2D palette;
		
		public TwoDimensional(OrbitToFloat trans0, OrbitToFloat trans1, Palette2D palette) {
			this.trans0 = trans0;
			this.trans1 = trans1;
			this.palette = palette;
		}

		@Override
		public int color(AbstractOrbit orbit) {
			return palette.color((float) trans0.value(orbit), (float) trans1.value(orbit));
		}
	}

	

	public static class Constant implements GeneralColorization {
		private int color;
		
		public Constant(int color) {
			this.color = color;
		}

		@Override
		public int color(AbstractOrbit orbit) {
			return color;
		}
	}
	

	// === Orbit Traps - this will be the next thing. ===
	
	/*public static abstract class OrbitTrapColorization implements Colorization {
		protected Palette palette;
		protected OrbitTrap trap;
		
		public OrbitTrapColorization(OrbitTrap trap, Palette palette) {
			this.trap = trap;
			this.palette = palette;
		}
	}
	
	public static class LastOrbitTrap extends OrbitTrapColorization {

		public LastOrbitTrap(OrbitTrap trap, Palette palette) {
			super(trap, palette);
		}

		@Override
		public int argb(AbstractOrbit orbit) {
			for(int i = orbit.length() - 1; i >= 0; i--) {
				Cplx c = orbit.get(i);
				if(trap.isInside(c)) {
					return trap.color(c, palette.argb(i));
				}
			}

			return 0;
		}
	}
	
	public static class TopOrbitTrap extends OrbitTrapColorization {

		public TopOrbitTrap(OrbitTrap trap, Palette palette) {
			super(trap, palette);
		}

		@Override
		public int argb(AbstractOrbit orbit) {
			for(int i = 1; i < orbit.length(); i++) {
				Cplx c = orbit.get(i);
				if(trap.isInside(c)) {
					return trap.color(c, palette.argb(i));
				}
			}

			return 0;
		}
	}
	
	public static class CombinedOrbitTrap extends OrbitTrapColorization {

		/**
		 * If true then earlier points in orbit have higher impact
		 *
		private boolean fromLast;
		
		public CombinedOrbitTrap(OrbitTrap trap, Palette palette, boolean fromLast) {
			super(trap, palette);
			this.fromLast = fromLast;
		}
		
		@Override
		public int argb(AbstractOrbit orbit) {
			float lab0[] = new float[]{0, 0, 0};
			
			for(int i = 0; i < orbit.length(); i++) {
				int j = fromLast ? orbit.length() - i - 1 : i;
				
				Cplx c = orbit.get(j);
				
				if(trap.isInside(c)) {
					int color = trap.color(c, palette.argb(j));
					double d = trap.dist(c);
					
					float[] lab = ColorConverter.IntToLab(color);
											
					for(int  k = 0; k < 3; k++)  lab0[k] = (float) (lab0[k] * (1 - d) + lab[k] * d);
				}
			}
			
			return ColorConverter.LabToInt(lab0);
		}
	}*/


}