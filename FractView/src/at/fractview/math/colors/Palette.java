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
package at.fractview.math.colors;

import android.graphics.Color;
import at.fractview.math.Spline;

// Linear gradient lab palette
public class Palette {
	
	private float[][] colors; // Keep colors just for documentation purposes...
	private boolean cyclic;
	
	private Spline[] splines;
	private float length;
	
	/**
	 * @param colors Colors in hsv format
	 * @param cyclic
	 * @param length
	 */
	public Palette(float[][] colors, boolean cyclic, float length) {

		this.colors = new float[colors.length][3];
		
		this.cyclic = cyclic;
		
		this.length = length;
		
		// Set colors
		splines = new Spline[3];
		
		float[][] Labs = new float[colors.length][3];
		
		for(int i = 0; i < colors.length; i++) {
			this.colors[i] = new float[]{ colors[i][0], colors[i][1], colors[i][2] };
			Labs[i] = Colors.IntToLab(Color.HSVToColor(colors[i]));
		}
		
		for(int c = 0; c < 3; c++) {
			float[] ys = new float[colors.length];
			
			for(int i = 0; i < colors.length; i++) {
				ys[i] = Labs[i][c];
			}
			
			splines[c] = new Spline.Cubic(ys, cyclic);
		}
	}
	
	public float[][] colors() {
		float[][] retVal = new float[colors.length][3];

		for(int i = 0; i < colors.length; i++) {
			retVal[i] = new float[]{ colors[i][0], colors[i][1], colors[i][2] };
		}

		return retVal;
	}
	
	// Default visibility so that other palettes can access these
	// No public visibility because these things might be subject to changes
	float norm(float x) {
		return x / length;
	}
	
	float l(float f) {
		return splines[0].y(f);
	}
	
	float a(float f) {
		return splines[1].y(f);
	}
	
	float b(float f) {
		return splines[2].y(f);
	}
	
	public int color(float x) {
		float d = x / length;
		
		return Colors.LabToRGB(
				splines[0].y(d), 
				splines[1].y(d), 
				splines[2].y(d), 1);
	}

	public float length() {
		return length;
	}

	public boolean cyclic() {
		return cyclic;
	}
	
	public String toString() {
		StringBuilder sb = null;
		
		for(float[] hsv : colors) {
			if(sb == null) {
				sb = new StringBuilder();
			} else {
				sb.append(", ");
			}
			sb.append(Integer.toHexString(Color.HSVToColor(hsv)));
		}
		
		return sb.toString();
	}
}
