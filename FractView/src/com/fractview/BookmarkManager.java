package com.fractview;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;

import com.fractview.math.Affine;
import com.fractview.math.Cplx;
import com.fractview.math.colors.Palette;
import com.fractview.math.tree.Expr;
import com.fractview.math.tree.ExprAdapter;
import com.fractview.math.tree.Parser;
import com.fractview.math.tree.Var;
import com.fractview.modes.orbit.EscapeTime;
import com.fractview.modes.orbit.colorization.CommonOrbitToFloat;
import com.fractview.modes.orbit.colorization.CommonTransfer;
import com.fractview.modes.orbit.colorization.OrbitTransfer;
import com.fractview.modes.orbit.functions.Function;
import com.fractview.tools.Labelled;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

public class BookmarkManager {
	
	public static final String BOOKMARKS_NAME = "Bookmarks";
	
	private static final int PREVIEW_SIZE = 48;
	private static final String TAG = "BookmarkManager";
	
	public class Bookmark {
		Time time;
		EscapeTime prefs;
		Bitmap preview;
		
		private Bookmark() {} // For GSon
		
		private Bookmark(EscapeTime prefs, Bitmap preview) {
			this.prefs = prefs;
			this.preview = preview;
			
			this.time = new Time();
			this.time.setToNow();
		}

		public Bitmap preview() {
			return preview;
		}

		public EscapeTime prefs() {
			return prefs;
		}

		public String timeString() {
			return time.format("%Y %m %d - %H:%M:%S");
		}
	}
	
	private static class BitmapAdapter implements JsonSerializer<Bitmap>, JsonDeserializer<Bitmap> {

		@Override
		public JsonElement serialize(Bitmap src, Type typeOfSrc,
		        JsonSerializationContext context) {
			ByteBuffer buffer = ByteBuffer.allocate(src.getWidth() * src.getHeight() * 4);
			
			src.copyPixelsToBuffer(buffer);
			
			String pixels = Base64.encodeToString(buffer.array(), Base64.DEFAULT);
			
			//int[] pixels = new int[src.getWidth() * src.getHeight()];
			//src.getPixels(pixels, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());
			
			JsonObject retValue = new JsonObject();
		    retValue.addProperty("pixels", pixels);
		    retValue.addProperty("width", src.getWidth());
		    retValue.addProperty("height", src.getHeight());

		    return retValue;
		}

		@Override
		public Bitmap deserialize(JsonElement json, Type typeOfT,
		        JsonDeserializationContext context) throws JsonParseException  {
			
			String pixels = json.getAsJsonObject().get("pixels").getAsString();
			
			byte[] buffer = Base64.decode(pixels, Base64.DEFAULT);

			int width = json.getAsJsonObject().get("width").getAsInt();
			int height = json.getAsJsonObject().get("height").getAsInt();

			Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			
			bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(buffer));

			return bitmap;
		}
	}
	
	private Gson gson;
	private SharedPreferences bookmarks;
	
	public BookmarkManager(Activity activity) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Expr.class, new ExprAdapter());
		gsonBuilder.registerTypeAdapter(Bitmap.class, new BitmapAdapter());
		
		this.gson = gsonBuilder.create();
		
		this.bookmarks = activity.getSharedPreferences(BOOKMARKS_NAME, 0);
	}
	
	public void updateBookmarks() {
		
	}
	
	public Bookmark create(EscapeTime prefs, Bitmap bitmap) {
		// Create preview by scaling down the original
		Bitmap preview = Bitmap.createBitmap(PREVIEW_SIZE, PREVIEW_SIZE, Config.ARGB_8888);
		
		Canvas canvas = new Canvas(preview);
		Matrix matrix = new Matrix();
		
		int min = Math.min(bitmap.getWidth(), bitmap.getHeight());
		
		RectF src = new RectF(
				(bitmap.getWidth() - min) / 2, (bitmap.getHeight() - min) / 2, 
				(bitmap.getWidth() + min) / 2, (bitmap.getHeight() + min) / 2);
		
		RectF dst = new RectF(0, 0, PREVIEW_SIZE, PREVIEW_SIZE);
		matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
		
		canvas.drawBitmap(bitmap, matrix, null);
		
		return new Bookmark(prefs, preview);
	}
	
	public Map<String, Bookmark> readBookmarks(Activity activity) {

		TreeMap<String, Bookmark> retVal = new TreeMap<String, Bookmark>();
		
		addBookmarks(this.bookmarks, retVal);
		
		// Retrieve old bookmarks
		try {
			Context oldContext = activity.createPackageContext("at.fractview", Context.MODE_WORLD_READABLE);

			// TODO: Show dialog whether old bookmarks should be imported
			SharedPreferences oldBookmarks = oldContext.getSharedPreferences(BOOKMARKS_NAME, 0);
			
			addBookmarks(oldBookmarks, retVal);
		} catch (NameNotFoundException e) {
			// Ignore...
			e.printStackTrace();
		} catch(Throwable th) {
			th.printStackTrace();
		}
		
		return retVal;
	}
	
	private void addBookmarks(SharedPreferences bms, TreeMap<String, Bookmark> retVal) {
		// Open file and read all entries
		Map<String, ?> map = bookmarks.getAll();
		
		for(Map.Entry<String, ?> entry : map.entrySet()) {
			String title = entry.getKey();
			Object value = entry.getValue();
			
			if(!(value instanceof String)) {
				Log.e(TAG, "Entry to " + title + " is not a string: " + entry.getValue());
			} else {
				try {
					Bookmark bookmark = gson.fromJson((String) value, Bookmark.class);
					retVal.put(title, bookmark);
				} catch(JsonSyntaxException e) {
					Log.e(TAG, "Error when trying to decipher entry");
					e.printStackTrace();
					Log.e(TAG, ((String) value));
				}
			}
		}
	}
	
	public void removeBookmark(String title) {
		bookmarks.edit().remove(title).commit(); // TODO: what happens if title does not exist?
	}
	
	public boolean containsBookmark(String title) {
		return bookmarks.contains(title);
	}
	
	public void addBookmark(String title, EscapeTime prefs, Bitmap image) {
		// If title already exists, it will be overwritten.
		Bookmark entry = create(prefs, image);
		
		String json = gson.toJson(entry);
		bookmarks.edit().putString(title, json).commit();
	}

	public Gson gson() {
		return gson;
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
				bailout, CommonOrbitToFloat.Length_Smooth, new OrbitTransfer(CommonTransfer.Log, new OrbitTransfer.Stats(0, 1)), bailoutPalette, 
				epsilon, CommonOrbitToFloat.Last_Angle, new OrbitTransfer(CommonTransfer.None, null), lakePalette);
	}
	
	/*public static EscapeTime cczcpaczcp() {
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
	}*/
}
