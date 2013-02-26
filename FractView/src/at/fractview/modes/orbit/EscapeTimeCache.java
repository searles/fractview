package at.fractview.modes.orbit;

import java.util.Arrays;

import android.util.Log;
import at.fractview.math.colors.Palette;
import at.fractview.modes.RasterTask;
import at.fractview.modes.RasterTask.Environment;
import at.fractview.modes.orbit.EscapeTime.Orbit;
import at.fractview.modes.orbit.colorization.CommonOrbitToFloat;
import at.fractview.modes.orbit.colorization.OrbitTransfer;
import at.fractview.modes.orbit.functions.Function;

public class EscapeTimeCache extends AbstractOrbitCache {
	
	// private static final String TAG = "ESC";
	
	private Stats[] stats;
	
	private int[] typeLength; // Bits 0..23 = nrIterations; rest = type.
	private float[] values;
	
	// This must be created from EscapeTime-class
	public EscapeTimeCache(EscapeTime prefs, int width, int height) {
		super(prefs, width, height);
		typeLength = new int[width * height];
		values = new float[width * height];
		
		stats = new Stats[]{new Stats(), new Stats()};
	}
	
	public EscapeTime prefs() {
		return (EscapeTime) super.prefs();
	}

	/** Calculates the color. If available, it is fetched from Cache,
	 * otherwise the orbit given in the parameter is used.
	 * @param prefs
	 * @param env 
	 * @param x
	 * @param y
	 * @param orbit
	 * @return
	 */
	public int color(Env env, int x, int y) {
		int index = x + y * width();
		
		int type;
		float v;
		
		if(typeLength[index] == 0) {
			env.orbit.generate(x, y, width, height);
			
			type = env.orbit.type();
			v = env.orbit.value;
			
			typeLength[index] = type | env.orbit.length();
			values[index] = v;
		} else {
			type = typeLength[index] & EscapeTime.TYPE_MASK;
			v = values[index];
		}
		
		// Update statistics in env
		int typeIndex = type == EscapeTime.BAILOUT_TYPE ? 0 : 1;
		env.nextValue(typeIndex, v);
		
		// Statistics in cache are not updated here but only later when everyone is sleeping or done.
		
		// Here, use statistics of cache
		// TODO: Statistics: Add 'normalize'-flag.
		//if(prefs().usesStats()) {
			v = (v - stats[typeIndex].minValue) / (stats[typeIndex].maxValue - stats[typeIndex].minValue);
		//}
		
		return prefs().color(type, v);
	}
	
	@Override
	public void initStatistics() {
		for(int i = 0; i < 2; i++) {
			stats[i].reset();
		}
	}

	@Override
	public boolean usesStats() {
		return true;
	}

	@Override
	public void updateStatisticsFromEnv(Environment env) {
		Env e = (Env) env;
		
		for(int i = 0; i < 2; i++) {
			stats[i].update(e.stats[i]);
		}
	}

	public RasterTask.Environment createEnvironment() {
		return new Env();
	}

	private class Stats {
		volatile float minValue = Float.POSITIVE_INFINITY;
		volatile float maxValue = Float.NEGATIVE_INFINITY;
		
		void reset() {
			this.minValue = Float.POSITIVE_INFINITY;
			this.maxValue = Float.NEGATIVE_INFINITY;
		}
		
		void nextValue(float v) {
			if(v < minValue) minValue = v;
			if(v > maxValue) maxValue = v;
		}
		
		void update(Stats stats) {
			if(stats.minValue < this.minValue) {
				this.minValue = stats.minValue;
			}

			if(stats.maxValue > this.maxValue) {
				this.maxValue = stats.maxValue;
			}
		}
	}
	
	private class Env implements RasterTask.Environment {
		Orbit orbit = prefs().createOrbit();
		Stats[] stats = new Stats[]{new Stats(), new Stats()};

		void nextValue(int typeIndex, float v) {
			stats[typeIndex].nextValue(v);
		}
		
		public int color(int x, int y) {
			return EscapeTimeCache.this.color(this, x, y);
		}
	}
	
	@Override
	protected void updateMaxIter(int oldMaxIter, int newMaxIter) {
		for(int i = 0; i < width() * height(); i++) {
			int length = typeLength[i] & AbstractOrbitPrefs.LENGTH_MASK;
				
			if(length == oldMaxIter || newMaxIter < length) {
				typeLength[i] = 0;
			}
		}
	}

	@Override
	protected void moveScale(int dx, int dy) {
		// TODO
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
		if(bailout != prefs().bailout()) {
			// TODO!!!
			clear();
			
			setPrefs(new EscapeTime(prefs().affine(), prefs().maxIter(), prefs().function(), 
					bailout, prefs().bailoutMethod(), prefs().bailoutTransfer(), prefs().bailoutPalette(),
					prefs().epsilon(), prefs().lakeMethod(), prefs().lakeTransfer(), prefs().lakePalette()));
		}
	}

	public void newBailoutMethod(CommonOrbitToFloat bailoutMethod) {
		if(bailoutMethod != prefs().bailoutMethod()) {
			// Clear all points that were drawn with this method
			for(int i = 0; i < width() * height(); i++) {
				int type = typeLength[i] & EscapeTime.TYPE_MASK;
				
				if(type == EscapeTime.BAILOUT_TYPE) {
					typeLength[i] = 0;
				}
			}
						
			setPrefs(new EscapeTime(prefs().affine(), prefs().maxIter(), prefs().function(), 
					prefs().bailout(), bailoutMethod, prefs().bailoutTransfer(), prefs().bailoutPalette(),
					prefs().epsilon(), prefs().lakeMethod(), prefs().lakeTransfer(), prefs().lakePalette()));
		}
	}
	
	
	public void newBailoutTransfer(OrbitTransfer bailoutTransfer) {
		if(bailoutTransfer != prefs().bailoutTransfer()) {
			clearBitmap(); 
			
			setPrefs(new EscapeTime(prefs().affine(), prefs().maxIter(), prefs().function(), 
					prefs().bailout(), prefs().bailoutMethod(), bailoutTransfer, prefs().bailoutPalette(),
					prefs().epsilon(), prefs().lakeMethod(), prefs().lakeTransfer(), prefs().lakePalette()));
		}
	}

	public void newBailoutPalette(Palette bailoutPalette) {
		// Keep cache
		clearBitmap();
		
		setPrefs(new EscapeTime(prefs().affine(), prefs().maxIter(), prefs().function(), 
				prefs().bailout(), prefs().bailoutMethod(), prefs().bailoutTransfer(), bailoutPalette,
				prefs().epsilon(), prefs().lakeMethod(), prefs().lakeTransfer(), prefs().lakePalette()));
	}
	
	public void newEpsilon(double epsilon) {
		if(epsilon != prefs().epsilon()) {
			// TODO!!!
			clear();
			
			setPrefs(new EscapeTime(prefs().affine(), prefs().maxIter(), prefs().function(), 
					prefs().bailout(), prefs().bailoutMethod(), prefs().bailoutTransfer(), prefs().bailoutPalette(),
					epsilon, prefs().lakeMethod(), prefs().lakeTransfer(), prefs().lakePalette()));
		}
	}

	public void newLakeMethod(CommonOrbitToFloat lakeMethod) {
		if(lakeMethod != prefs().lakeMethod()) {
			// Clear all points that were drawn with this method
			for(int i = 0; i < width() * height(); i++) {
				int type = typeLength[i] & EscapeTime.TYPE_MASK;
				
				if(type == EscapeTime.LAKE_TYPE) {
					typeLength[i] = 0;
				}
			}
			
			setPrefs(new EscapeTime(prefs().affine(), prefs().maxIter(), prefs().function(), 
					prefs().bailout(), prefs().bailoutMethod(), prefs().bailoutTransfer(), prefs().bailoutPalette(),
					prefs().epsilon(), lakeMethod, prefs().lakeTransfer(), prefs().lakePalette()));
		}
	}
	
	public void newLakeTransfer(OrbitTransfer lakeTransfer) {
		if(lakeTransfer != prefs().lakeTransfer()) {
			// Keep cache.
			clearBitmap(); 
			
			setPrefs(new EscapeTime(prefs().affine(), prefs().maxIter(), prefs().function(), 
					prefs().bailout(), prefs().bailoutMethod(), prefs().bailoutTransfer(), prefs().bailoutPalette(),
					prefs().epsilon(), prefs().lakeMethod(), lakeTransfer, prefs().lakePalette()));
		}
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