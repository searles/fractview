package at.fractview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.graphics.Color;
import at.fractview.math.Affine;
import at.fractview.math.Cplx;
import at.fractview.math.colors.Palette;
import at.fractview.math.tree.Expr;
import at.fractview.math.tree.ExprAdapter;
import at.fractview.math.tree.Parser;
import at.fractview.math.tree.Var;
import at.fractview.modes.orbit.EscapeTime;
import at.fractview.modes.orbit.colorization.CommonOrbitToFloat;
import at.fractview.modes.orbit.colorization.CommonTransfer;
import at.fractview.modes.orbit.colorization.OrbitTransfer;
import at.fractview.modes.orbit.functions.Function;
import at.fractview.tools.Labelled;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BookmarkManager {
	
	private static final int PREVIEW_SIZE = 64;
	
	private Gson gson;
	
	public BookmarkManager() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Expr.class, new ExprAdapter());
		
		this.gson = gsonBuilder.create();
	}

	public String encode(EscapeTime prefs) {
		return gson.toJson(prefs);
	}
	
	public EscapeTime decode(String s) {
		return gson.fromJson(s, EscapeTime.class);
	}
	
	private static float[][] toHSV(int[] palette) {
		float[][] hsv = new float[palette.length][3];
		
		for(int i = 0; i < palette.length; i++) {
			Color.colorToHSV(palette[i], hsv[i]);
		}
		
		return hsv;
	}
	
	public static EscapeTime mandelbrot() {
		int maxIter = 100;
		double bailout = 64.;
		double epsilon = 1e-9;

		Affine affine = Affine.scalation(4, 4);
		affine.preConcat(Affine.translation(-2, -2));
		

        Palette bailoutPalette = new Palette(
				toHSV(new int[]{0xFFFFAA00, 0xFF310230, 0xff000764, 0xff206BCB, 0xffEDFFFF}),
				true);
		
		Palette lakePalette = new Palette(
				toHSV(new int[]{0xff070064, 0xff6b20cb, 0xffffedff, 0xffaaff00, 0xff023130}), 
				true);

		
		// z^2 * (z + x) + y*z(n-1)
		// sqr((z^3 + 3(c - 1)z + (c - 1)(c - 2)) / (3 * z^2 + 3(c - 2)z + (c - 1)(c - 2) + 1))
		// horner(0, 0, [-1.4, -1.4], 0, c)
		// Cczcpaczcp (no, not a typo): c(z^3 + 1/z^3), 1
		
		// Golden Ratio:
		// z^3/3 - z^2/2 - z + c
		
		// Functions with two different points: x^3-x^2+c; either 2/3 or 0.

		String sf = "sqr z + c";
		String si0 = "0";
		
		Labelled<Expr> fn = new Labelled<Expr>(Parser.parse(sf).get(), sf);
		Labelled<Expr> i0 = new Labelled<Expr>(Parser.parse(si0).get(), si0);
		
		Map<Var, Labelled<Cplx>> ps = new TreeMap<Var, Labelled<Cplx>>();
		
		List<Labelled<Expr>> l = new ArrayList<Labelled<Expr>>();
		l.add(i0);
		
		Function function = new Function(fn, l, ps);
		
		return new EscapeTime(affine, maxIter, function,
				bailout, CommonOrbitToFloat.Length_Smooth, new OrbitTransfer(false, 0f, 1f, CommonTransfer.Log), bailoutPalette, 
				epsilon, CommonOrbitToFloat.Last_Angle, new OrbitTransfer(true, 0f, 1f, CommonTransfer.None), lakePalette);
	}
	
	public static EscapeTime cczcpaczcp() {
		int maxIter = 100;
		double bailout = 64.;
		double epsilon = 1e-9;

		Affine affine = Affine.scalation(4, 4);
		affine.preConcat(Affine.translation(-2, -2));
		

        Palette bailoutPalette = new Palette(
				toHSV(new int[]{0xFFFFAA00, 0xFF310230, 0xff000764, 0xff206BCB, 0xffEDFFFF}),
				true);
		
		Palette lakePalette = new Palette(
				toHSV(new int[]{0xff070064, 0xff6b20cb, 0xffffedff, 0xffaaff00, 0xff023130}), 
				true);

		
		String sf = "c(alpha * z^beta + gamma * z^-delta)";
		String si0 = "0"; // TODO
		
		Labelled<Expr> fn = new Labelled<Expr>(Parser.parse(sf).get(), sf);
		Labelled<Expr> i0 = new Labelled<Expr>(Parser.parse(si0).get(), si0);
		
		Map<Var, Labelled<Cplx>> ps = new TreeMap<Var, Labelled<Cplx>>();
		
		ps.put(new Var("alpha"), new Labelled<Cplx>(new Cplx(1, 0), "1"));
		ps.put(new Var("beta"), new Labelled<Cplx>(new Cplx(3, 0), "3"));
		ps.put(new Var("gamma"), new Labelled<Cplx>(new Cplx(-1, 0), "-1"));
		ps.put(new Var("delta"), new Labelled<Cplx>(new Cplx(-3, 0), "-3"));
		
		List<Labelled<Expr>> l = new ArrayList<Labelled<Expr>>();
		l.add(i0);
		
		Function function = new Function(fn, l, ps);
		
		return new EscapeTime(affine, maxIter, function,
				bailout, CommonOrbitToFloat.Length_Smooth, new OrbitTransfer(false, 0f, 1f, CommonTransfer.Log), bailoutPalette, 
				epsilon, CommonOrbitToFloat.Last_Angle, new OrbitTransfer(true, 0f, 1f, CommonTransfer.None), lakePalette);
	}
	

	public static EscapeTime burningShip() {
		int maxIter = 100;
		double bailout = 64.;
		double epsilon = 1e-9;

		Affine affine = Affine.scalation(4, 4);
		affine.preConcat(Affine.translation(-2, -2));
		

        Palette bailoutPalette = new Palette(
				toHSV(new int[]{0xFFFFAA00, 0xFF310230, 0xff000764, 0xff206BCB, 0xffEDFFFF}),
				true);
		
		Palette lakePalette = new Palette(
				toHSV(new int[]{0xff070064, 0xff6b20cb, 0xffffedff, 0xffaaff00, 0xff023130}), 
				true);

		
		String sf = "sqr cabs z + c";
		String si0 = "0";
		
		Labelled<Expr> fn = new Labelled<Expr>(Parser.parse(sf).get(), sf);
		Labelled<Expr> i0 = new Labelled<Expr>(Parser.parse(si0).get(), si0);
		
		Map<Var, Labelled<Cplx>> ps = new TreeMap<Var, Labelled<Cplx>>();
		
		List<Labelled<Expr>> l = new ArrayList<Labelled<Expr>>();
		l.add(i0);
		
		Function function = new Function(fn, l, ps);
		
		return new EscapeTime(affine, maxIter, function,
				bailout, CommonOrbitToFloat.Length_Smooth, new OrbitTransfer(false, 0f, 1f, CommonTransfer.Log), bailoutPalette, 
				epsilon, CommonOrbitToFloat.Last_Angle, new OrbitTransfer(true, 0f, 1f, CommonTransfer.None), lakePalette);
	}
	
	public static EscapeTime tricorn() {
		int maxIter = 100;
		double bailout = 64.;
		double epsilon = 1e-9;

		Affine affine = Affine.scalation(4, 4);
		affine.preConcat(Affine.translation(-2, -2));
		

        Palette bailoutPalette = new Palette(
				toHSV(new int[]{0xFFFFAA00, 0xFF310230, 0xff000764, 0xff206BCB, 0xffEDFFFF}),
				true);
		
		Palette lakePalette = new Palette(
				toHSV(new int[]{0xff070064, 0xff6b20cb, 0xffffedff, 0xffaaff00, 0xff023130}), 
				true);

		
		String sf = "sqr conj z + c";
		String si0 = "0";
		
		Labelled<Expr> fn = new Labelled<Expr>(Parser.parse(sf).get(), sf);
		Labelled<Expr> i0 = new Labelled<Expr>(Parser.parse(si0).get(), si0);
		
		Map<Var, Labelled<Cplx>> ps = new TreeMap<Var, Labelled<Cplx>>();
		
		List<Labelled<Expr>> l = new ArrayList<Labelled<Expr>>();
		l.add(i0);
		
		Function function = new Function(fn, l, ps);
		
		return new EscapeTime(affine, maxIter, function,
				bailout, CommonOrbitToFloat.Length_Smooth, new OrbitTransfer(false, 0f, 1f, CommonTransfer.Log), bailoutPalette, 
				epsilon, CommonOrbitToFloat.Last_Angle, new OrbitTransfer(true, 0f, 1f, CommonTransfer.None), lakePalette);
	}
	
	public static EscapeTime phoenix() {
		int maxIter = 100;
		double bailout = 64.;
		double epsilon = 1e-9;

		Affine affine = Affine.scalation(4, 4);
		affine.preConcat(Affine.translation(-2, -2));
		

        Palette bailoutPalette = new Palette(
				toHSV(new int[]{0xFFFFAA00, 0xFF310230, 0xff000764, 0xff206BCB, 0xffEDFFFF}),
				true);
		
		Palette lakePalette = new Palette(
				toHSV(new int[]{0xff070064, 0xff6b20cb, 0xffffedff, 0xffaaff00, 0xff023130}), 
				true);

		
		String sf = "sqr z + re p + im p * z1";
		String si0 = "c";
		
		Labelled<Expr> fn = new Labelled<Expr>(Parser.parse(sf).get(), sf);
		Labelled<Expr> i0 = new Labelled<Expr>(Parser.parse(si0).get(), si0);
		
		Map<Var, Labelled<Cplx>> ps = new TreeMap<Var, Labelled<Cplx>>();
		
		Cplx p = new Cplx(0.56666667, -0.5);
		ps.put(new Var("p"), new Labelled<Cplx>(p, p.toString()));
		
		List<Labelled<Expr>> l = new ArrayList<Labelled<Expr>>();
		l.add(i0);
		
		Function function = new Function(fn, l, ps);
		
		return new EscapeTime(affine, maxIter, function,
				bailout, CommonOrbitToFloat.Length_Smooth, new OrbitTransfer(false, 0f, 1f, CommonTransfer.Log), bailoutPalette, 
				epsilon, CommonOrbitToFloat.Last_Angle, new OrbitTransfer(true, 0f, 1f, CommonTransfer.None), lakePalette);
	}
	
	public static EscapeTime newton() {
		int maxIter = 100;
		double bailout = 1e99;
		double epsilon = 1e-9;

		Affine affine = Affine.scalation(4, 4);
		affine.preConcat(Affine.translation(-2, -2));
		

        Palette bailoutPalette = new Palette(
				toHSV(new int[]{0xFFFFAA00, 0xFF310230, 0xff000764, 0xff206BCB, 0xffEDFFFF}),
				true);
		
		Palette lakePalette = new Palette(
				toHSV(new int[]{0xff070064, 0xff6b20cb, 0xffffedff, 0xffaaff00, 0xff023130}), 
				true);

		
		String sf = "newton(z^3 - 1)";
		String si0 = "c";
		
		Labelled<Expr> fn = new Labelled<Expr>(Parser.parse(sf).get(), sf);
		Labelled<Expr> i0 = new Labelled<Expr>(Parser.parse(si0).get(), si0);
		
		Map<Var, Labelled<Cplx>> ps = new TreeMap<Var, Labelled<Cplx>>();
		
		List<Labelled<Expr>> l = new ArrayList<Labelled<Expr>>();
		l.add(i0);
		
		Function function = new Function(fn, l, ps);
		
		return new EscapeTime(affine, maxIter, function,
				bailout, CommonOrbitToFloat.Zero, new OrbitTransfer(false, 0f, 1f, CommonTransfer.None), bailoutPalette, 
				epsilon, CommonOrbitToFloat.Last_Angle, new OrbitTransfer(true, 0f, 1f, CommonTransfer.None), lakePalette);
	}
	
	public static EscapeTime nova() {
		int maxIter = 100;
		double bailout = 1e99;
		double epsilon = 1e-9;

		Affine affine = Affine.scalation(4, 4);
		affine.preConcat(Affine.translation(-2, -2));
		

        Palette bailoutPalette = new Palette(
				toHSV(new int[]{0xFFFFAA00, 0xFF310230, 0xff000764, 0xff206BCB, 0xffEDFFFF}),
				true);
		
		Palette lakePalette = new Palette(
				toHSV(new int[]{0xff070064, 0xff6b20cb, 0xffffedff, 0xffaaff00, 0xff023130}), 
				true);

		
		String sf = "nova(z^3 - 1);1;1";
		String si0 = "c";
		
		Labelled<Expr> fn = new Labelled<Expr>(Parser.parse(sf).get(), sf);
		Labelled<Expr> i0 = new Labelled<Expr>(Parser.parse(si0).get(), si0);
		
		Map<Var, Labelled<Cplx>> ps = new TreeMap<Var, Labelled<Cplx>>();
		
		List<Labelled<Expr>> l = new ArrayList<Labelled<Expr>>();
		l.add(i0);
		
		Function function = new Function(fn, l, ps);
		
		return new EscapeTime(affine, maxIter, function,
				bailout, CommonOrbitToFloat.Zero, new OrbitTransfer(false, 0f, 1f, CommonTransfer.None), bailoutPalette, 
				epsilon, CommonOrbitToFloat.Last_Angle, new OrbitTransfer(true, 0f, 1f, CommonTransfer.None), lakePalette);
	}
}
