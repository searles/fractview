package at.fractview.math.colors;

import at.fractview.math.Spline;

public class Palette2D {
	// TODO Keep colors and all parameters of constructor.
	private int[][] colors;
	
	private boolean cyclic;
	
	private float offset;
	private float length;

	private Palette[] ps;	

	public Palette2D(int[][] colors, 
			boolean xCyclic, boolean yCyclic, 
			float xLength, float yLength) {
		
		this.cyclic = yCyclic;
		this.length = yLength;
		
		ps = new Palette[colors.length];
		
		for(int i = 0; i < colors.length; i++) {
			ps[i] = new Palette(colors[i], xCyclic, xLength);
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
		
		y = (y + offset) / length;
		
		int index = (int) Math.floor(y);
						
		y -= index;

		// for cubic interpolation we need 4 values
		int i0 = clamp(index - 1);
		int i1 = clamp(index);
		int i2 = clamp(index + 1);
		int i3 = clamp(index + 2);
		
		float f0 = ps[i0].norm(x);
		float f1 = ps[i1].norm(x);
		float f2 = ps[i2].norm(x);
		float f3 = ps[i3].norm(x);

		float L = Spline.Cubic.yNoSlope(y, ps[i0].l(f0), ps[i1].l(f1), ps[i2].l(f2), ps[i3].l(f3));
		float a = Spline.Cubic.yNoSlope(y, ps[i0].a(f0), ps[i1].a(f1), ps[i2].a(f2), ps[i3].a(f3));
		float b = Spline.Cubic.yNoSlope(y, ps[i0].b(f0), ps[i1].b(f1), ps[i2].b(f2), ps[i3].b(f3));
		
		return Colors.LabToRGB((float) L, (float) a, (float) b, 1);	
	}
}