package at.fractview.modes.orbit;

import java.util.Arrays;

import android.util.Log;
import at.fractview.math.colors.Palette;
import at.fractview.modes.orbit.EscapeTime.Orbit;
import at.fractview.modes.orbit.colorization.CommonOrbitToFloat;
import at.fractview.modes.orbit.colorization.OrbitTransfer;
import at.fractview.modes.orbit.functions.Function;

public class EscapeTimeCache extends AbstractOrbitCache {
	
	private static final String TAG = "ESC";
	
	private int[] typeLength; // Bits 0..23 = nrIterations; rest = type.
	private float[] values;
	
	// This must be created from EscapeTime-class
	public EscapeTimeCache(EscapeTime prefs, int width, int height) {
		super(prefs, width, height);
		typeLength = new int[width * height];
		values = new float[width * height];
	}
	
	public EscapeTime prefs() {
		return (EscapeTime) super.prefs();
	}

	/** Calculates the color. If available, it is fetched from Cache,
	 * otherwise the orbit given in the parameter is used.
	 * @param prefs
	 * @param x
	 * @param y
	 * @param orbit
	 * @return
	 */
	public int color(EscapeTime prefs, int x, int y, Orbit orbit) {
		int index = x + y * width();
		
		int type;
		float v;
		
		if(typeLength[index] == 0) {
			orbit.generate(x, y, width, height);
			
			type = orbit.type();
			v = orbit.value;
			
			typeLength[index] = type | orbit.length();
			values[index] = v;
		} else {
			type = typeLength[index] & EscapeTime.TYPE_MASK;
			v = values[index];
		}
			
		return prefs.color(type, v);
	}

	@Override
	protected void updateMaxIter(int oldMaxIter, int newMaxIter) {
		// TODO Auto-generated method stub
		clear();
	}

	@Override
	protected void moveScale(int dx, int dy) {
		Log.d("Cache", "Move " + dx + ", " + dy);
		clear();
	}

	public void newFunction(Function function) {
		clear();
		
		setPrefs(new EscapeTime(prefs().affine(), prefs().maxIter(), function, 
				prefs().bailout(), prefs().bailoutMethod(), prefs().bailoutTransfer(), prefs().bailoutPalette(),
				prefs().epsilon(), prefs().lakeMethod(), prefs().lakeTransfer(), prefs().lakePalette()));
	}

	public void newBailout(double bailout) {
		// TODO: Depending on whether bailout grows or shrinks, only update lake or bailout.
		clear();
		
		setPrefs(new EscapeTime(prefs().affine(), prefs().maxIter(), prefs().function(), 
				bailout, prefs().bailoutMethod(), prefs().bailoutTransfer(), prefs().bailoutPalette(),
				prefs().epsilon(), prefs().lakeMethod(), prefs().lakeTransfer(), prefs().lakePalette()));
	}

	public void newBailoutMethod(CommonOrbitToFloat bailoutMethod) {
		// TODO: Update only bailout
		clear();
		
		setPrefs(new EscapeTime(prefs().affine(), prefs().maxIter(), prefs().function(), 
				prefs().bailout(), bailoutMethod, prefs().bailoutTransfer(), prefs().bailoutPalette(),
				prefs().epsilon(), prefs().lakeMethod(), prefs().lakeTransfer(), prefs().lakePalette()));
	}
	
	
	public void newBailoutTransfer(OrbitTransfer bailoutTransfer) {
		// Keep cache.
		clearBitmap(); 
		
		setPrefs(new EscapeTime(prefs().affine(), prefs().maxIter(), prefs().function(), 
				prefs().bailout(), prefs().bailoutMethod(), bailoutTransfer, prefs().bailoutPalette(),
				prefs().epsilon(), prefs().lakeMethod(), prefs().lakeTransfer(), prefs().lakePalette()));
	}

	public void newBailoutPalette(Palette bailoutPalette) {
		// Keep cache
		clearBitmap();
		
		setPrefs(new EscapeTime(prefs().affine(), prefs().maxIter(), prefs().function(), 
				prefs().bailout(), prefs().bailoutMethod(), prefs().bailoutTransfer(), bailoutPalette,
				prefs().epsilon(), prefs().lakeMethod(), prefs().lakeTransfer(), prefs().lakePalette()));
	}
	
	public void newEpsilon(double epsilon) {
		// TODO: Depending on whether epsilon grows or shrinks, only update lake or epsilon.
		clear();
		
		setPrefs(new EscapeTime(prefs().affine(), prefs().maxIter(), prefs().function(), 
				prefs().bailout(), prefs().bailoutMethod(), prefs().bailoutTransfer(), prefs().bailoutPalette(),
				epsilon, prefs().lakeMethod(), prefs().lakeTransfer(), prefs().lakePalette()));
	}

	public void newLakeMethod(CommonOrbitToFloat lakeMethod) {
		// TODO: Update only lake
		clear();
		
		setPrefs(new EscapeTime(prefs().affine(), prefs().maxIter(), prefs().function(), 
				prefs().bailout(), prefs().bailoutMethod(), prefs().bailoutTransfer(), prefs().bailoutPalette(),
				prefs().epsilon(), lakeMethod, prefs().lakeTransfer(), prefs().lakePalette()));
	}
	
	
	public void newLakeTransfer(OrbitTransfer lakeTransfer) {
		// Keep cache.
		clearBitmap(); 
		
		setPrefs(new EscapeTime(prefs().affine(), prefs().maxIter(), prefs().function(), 
				prefs().bailout(), prefs().bailoutMethod(), prefs().bailoutTransfer(), prefs().bailoutPalette(),
				prefs().epsilon(), prefs().lakeMethod(), lakeTransfer, prefs().lakePalette()));
	}

	public void newLakePalette(Palette lakePalette) {
		// Keep cache
		clearBitmap();
		
		setPrefs(new EscapeTime(prefs().affine(), prefs().maxIter(), prefs().function(), 
				prefs().bailout(), prefs().bailoutMethod(), prefs().bailoutTransfer(), prefs().bailoutPalette(),
				prefs().epsilon(), prefs().lakeMethod(), prefs().lakeTransfer(), lakePalette));
	}

	@Override
	public void clear() {
		Log.d(TAG, "clear()");
		Arrays.fill(typeLength, 0);
	}

	@Override
	public void resize(int width, int height) {
		this.typeLength = null;
		this.values = null;
		
		resizeBitmap(width, height);
		
		typeLength = new int[width * height];
		values = new float[width * height];
	}
}