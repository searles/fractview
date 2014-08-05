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
package com.fractview.math.colors;

import com.fractview.math.Spline;

public class Palette2D {
	// TODO Keep colors and all parameters of constructor.
	
	private boolean cyclic; // y-cyclic
	private Palette[] ps;	

	public Palette2D(float[][][] colors, boolean xCyclic, boolean yCyclic) {
		this.cyclic = yCyclic;
		
		ps = new Palette[colors.length];
		
		for(int i = 0; i < colors.length; i++) {
			ps[i] = new Palette(colors[i], xCyclic);
		}
	}		

	private int clamp(int index) {
		if(cyclic) {
			index = index % ps.length;
			
			if(index < 0) {
				index = (index + ps.length);
			}
			
			return index;
		} else {
			return index < 0 ? 0 : index >= ps.length ? ps.length - 1 : index;
		}
	}
	
	public int color(float x, float y) {			
		y *= cyclic ? ps.length : ps.length - 1;
		
		int index = (int) Math.floor(y);
						
		y -= index;

		// for cubic interpolation we need 4 values
		int i0 = clamp(index - 1);
		int i1 = clamp(index);
		int i2 = clamp(index + 1);
		int i3 = clamp(index + 2);
		
		float L = Spline.Cubic.yNoSlope(y, ps[i0].l(x), ps[i1].l(x), ps[i2].l(x), ps[i3].l(x));
		float a = Spline.Cubic.yNoSlope(y, ps[i0].a(x), ps[i1].a(x), ps[i2].a(x), ps[i3].a(x));
		float b = Spline.Cubic.yNoSlope(y, ps[i0].b(x), ps[i1].b(x), ps[i2].b(x), ps[i3].b(x));
		
		return Colors.LabToRGB((float) L, (float) a, (float) b, 1);	
	}
}