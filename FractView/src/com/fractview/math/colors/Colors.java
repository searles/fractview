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

import android.graphics.Color;



public class Colors {

	public static int parseColorString(String s) {
		return Integer.parseInt(s, 16) | 0xff000000;
	}
	
	public static String toColorString(int color) {
		String s = Integer.toHexString(color & 0xffffff);
		
		while(s.length() < 6) s = "0" + s;
		
		return s;
	}
	
	public static float brightness(int color) {
		return 0.299f * Color.red(color) + 0.587f * Color.green(color) + 0.144f * Color.blue(color);
	}

	// TODO: Well, look at it...
	
	private static float crop(float value, float min, float max) {
		return value > max ? max : value < min ? min : value;
	}
	
	/** Extracts alpha from an int-color-value
	 * @param color
	 * @return Alpha-value between 0f and 1f.
	 */
	public static float alpha(int color) {
		return ((color >> 24) & 0xff) / 255f;
	}
	
	public static float r(int color) {
		return ((color >> 16) & 0xff) / 255f;
	}
	
	public static float g(int color) {
		return ((color >> 8) & 0xff) / 255f;
	}
	
	public static float b(int color) {
		return (color & 0xff) / 255f;
	}
	
	/** Converts float-values between 0f and 1f into int-color-format. Values are cropped.
	 * @param r
	 * @param g
	 * @param b
	 * @param alpha
	 * @return
	 */
	public static int color(float r, float g, float b, float alpha) {
		int ir = (int) crop(r * 256f, 0f, 255f);
		int ig = (int) crop(g * 256f, 0f, 255f);
		int ib = (int) crop(b * 256f, 0f, 255f);
		int ia = (int) crop(alpha * 256f, 0f, 255f);

		return ia << 24 | ir << 16 | ig << 8 | ib; 
	}
	
	public static int color(float r, float g, float b) {
		return color(r, g, b, 1f);
	}
	
	public static int combineAlpha(int color0, int color1, float degree) {
		float alpha = alpha(color0) * degree + alpha(color1) * (1 - degree);
		
		int a = (int) crop(alpha * 256f, 0f, 255f);
		
		return a << 24;
	}
	
	/*public static int combineLab(int color0, int color1, float degree) {
		float[] lab0 = IntToLab(color0);
		float[] lab1 = IntToLab(color1);

		for(int i = 0; i < 3; i++) {
			lab0[i] = lab0[i] * degree + lab1[i] * (1 - degree); 
		}
		
		return LabToInt(lab0) | combineAlpha(color0, color1, degree);
	}
	
	public static int combineRGB(int color0, int color1, float degree) {
		float[] rgb0 = IntToRGB(color0);
		float[] rgb1 = IntToRGB(color1);
		
		for(int i = 0; i < 3; i++) {
			rgb0[i] = rgb0[i] * degree + rgb1[i] * (1 - degree); 
		}
		
		return RGBToInt(rgb0) | combineAlpha(color0, color1, degree);
	}*/

	
	public static float[] IntToRGB(int rgb) {
		float[] rgb0 = new float[]{(rgb & 0x00ff0000) >> 16, 
				(rgb & 0x0000ff00) >> 8, 
				(rgb & 0x000000ff)};
		
		for(int i = 0; i < 3; i++) {
			rgb0[i] /= 255f;
		}
		
		return rgb0;
	}
	
	public static int RGBToInt(float[] rgb) {
		int color = Math.max(0, Math.min((int) (rgb[0] * 256f), 255)) << 16 
				| Math.max(0, Math.min((int) (rgb[1] * 256f), 255)) << 8
				| Math.max(0, Math.min((int) (rgb[2] * 256f), 255));
		
		return color | 0xff000000;
	}
	
	public static float[] IntToLab(int rgb) {
		return RGBToLab(IntToRGB(rgb));
	}
	
	public static int LabToRGB(float L, float a, float b, float alpha) {
	    float delta = 6.0f / 29.0f;

	    float fy = (L + 16.f) / 116.0f;
	    float fx = fy + (a / 500.0f);
	    float fz = fy - (b / 200.0f);
	    
	    float x = (fx > delta)? 0.9505f * (fx*fx*fx) : (fx - 16.0f/116.0f) * 3 * (delta*delta) * 0.9505f;
	    float y = (fy > delta)? 1.0f * (fy*fy*fy) : (fy - 16.0f/116.0f) * 3 * (delta*delta) * 1.0f;
	    float z = (fz > delta)? 1.0890f * (fz*fz*fz) : (fz - 16.0f/116.0f) * 3 * (delta*delta) * 1.0890f;

	    float red = x * 3.2410f - y * 1.5374f - z * 0.4986f; // red
	    float green = -x * 0.9692f + y * 1.8760f - z * 0.0416f; // green
	    float blue = x * 0.0556f - y * 0.2040f + z * 1.0570f; // blue

	    red = (red <= 0.0031308f)? 
    			12.92f * red : 
    			(1 + 0.055f) * (float) Math.pow(red, (1.0f/2.4f)) - 0.055f;

	    green = (green <= 0.0031308f)? 
    			12.92f * green : 
    			(1 + 0.055f) * (float) Math.pow(green, (1.0f/2.4f)) - 0.055f;

	    blue = (blue <= 0.0031308f)? 
    			12.92f * blue : 
    			(1 + 0.055f) * (float) Math.pow(blue, (1.0f/2.4f)) - 0.055f;
	    
		return color(red, green, blue, alpha);
	}
	
	// Lab to XYZ to RGB
	
	private static final float a = 0.055f;
	
	private static final float Xn = 0.9505f;
	private static final float Yn = 1.f;
	private static final float Zn = 1.0890f;
	
	private static float f(float t) {
		return ((t > (216.f / 24389.f)) ? 
				(float) Math.cbrt(t) : 
				(841.f / 108.f * t + 4.f / 29.f));
	}
	
	private static float fInv(float t) {
		return (t > (6.f / 29.f)) ? 
				t * t * t : 
				3.f * 6.f / 29.f * 6.f / 29.f * (t - 16.f / 116.f);
	}
	
	public static float[] XYZToLab(float[] XYZ) {
		float L = 116.f * f(XYZ[1] / Yn) - 16.f;
		float a = 500.f * (f(XYZ[0] / Xn) - f(XYZ[1] / Yn));
		float b = 200.f * (f(XYZ[1] / Yn) - f(XYZ[2] / Zn));
		
		return new float[]{(float) L, (float) a, (float) b};
	}
	
	public static float[] LabToXYZ(float[] Lab) {
	    float fy = (Lab[0] + 16.f) / 116.f;
	    float fx = fy + Lab[1] / 500.f;
	    float fz = fy - Lab[2] / 200.f;
	    
	    float X = Xn * fInv(fx);
	    float Y = Yn * fInv(fy);
	    float Z = Zn * fInv(fz);
	    
	    return new float[]{(float) X, (float) Y, (float) Z};
	}

	private static float Csrgb(float Clin) {
		return Clin <= 0.0031308f ? 
     			12.92f * Clin : 
     			(1.f + a) * (float) Math.pow(Clin, 1.f / 2.4f) - a;
	}
	
	public static float[] XYZToRGB(float[] xyz) {
	    float rlin = xyz[0] * 3.2410f - xyz[1] * 1.5374f - xyz[2] * 0.4986f; // red
	    float glin = -xyz[0] * 0.9692f + xyz[1] * 1.8760f - xyz[2] * 0.0416f; // green
	    float blin = xyz[0] * 0.0556f - xyz[1] * 0.2040f + xyz[2] * 1.0570f; // blue

	    return new float[]{(float) Csrgb(rlin), (float) Csrgb(glin), (float) Csrgb(blin)};
	}
	
	
	public static float[] LabToRGB(float[] Lab) {
		return XYZToRGB(LabToXYZ(Lab));
	}
	
	// RGB to XYZ to Lab
	
	private static float g(float K) {
		if(K > 0.04045f) {
			return (float) Math.pow((K + a) / (1 + a), 2.2);
		} else {
			return K / 12.92f;
		}
	}
	
	public static float[] RGBToXYZ(float[] rgb) {
		// Convert to sRGB
		float r = g(rgb[0]);
		float g = g(rgb[1]);
		float b = g(rgb[2]);
		
		float X = 0.4124f * r + 0.3576f * g + 0.1805f * b;
		float Y = 0.2126f * r + 0.7152f * g + 0.0722f * b;
		float Z = 0.0193f * r + 0.1192f * g + 0.9505f * b;
		
		return new float[]{(float) X, (float) Y, (float) Z};
	}
	
	public static float[] RGBToLab(float[] rgb) {
		return XYZToLab(RGBToXYZ(rgb));
	}
	
	public static int rgb(float[] rgb, float alpha) {
		int a = Math.min(255, Math.max(0, (int) (alpha * 256)));
		int r = Math.min(255, Math.max(0, (int) (rgb[0] * 256)));
		int g = Math.min(255, Math.max(0, (int) (rgb[1] * 256)));
		int b = Math.min(255, Math.max(0, (int) (rgb[2] * 256)));
		
		return a << 24 | r << 16 | g << 8 | b;
	}

	public static int LabToInt(float[] Lab) {
		return rgb(LabToRGB(Lab), 1);
	}
}
