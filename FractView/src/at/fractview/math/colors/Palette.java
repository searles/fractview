package at.fractview.math.colors;

import java.util.Arrays;
import java.util.Iterator;

import at.fractview.math.Spline;

// Linear gradient lab palette
public class Palette {
	
	// TODO: Documentation! float.
	private int[] colors; // Keep colors just for documentation purposes...
	private boolean cyclic;
	
	private Spline[] splines;
	private float length;
	
	public Palette(int[] colors, boolean cyclic, float length) {

		this.colors = colors;
		
		this.cyclic = cyclic;
		
		this.length = length;
		
		// Set colors
		splines = new Spline[3];
		
		float[][] Labs = new float[colors.length][3];
		
		for(int i = 0; i < colors.length; i++) {
			Labs[i] = Colors.IntToLab(colors[i]);
		}
		
		for(int c = 0; c < 3; c++) {
			float[] ys = new float[colors.length];
			
			for(int i = 0; i < colors.length; i++) {
				ys[i] = Labs[i][c];
			}
			
			splines[c] = new Spline.Cubic(ys, cyclic);
		}
	}
	
	public Iterable<Integer> colors() {
		return new Iterable<Integer>() {

			@Override
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					int i = 0;
					@Override
					public boolean hasNext() {
						return i < colors.length;
					}

					@Override
					public Integer next() {
						return colors[i++];
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
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
}
