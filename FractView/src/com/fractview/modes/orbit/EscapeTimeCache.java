package com.fractview.modes.orbit;

import java.util.Arrays;

import com.fractview.math.colors.Palette;
import com.fractview.modes.RasterTask;
import com.fractview.modes.RasterTask.Environment;
import com.fractview.modes.orbit.EscapeTime.Orbit;
import com.fractview.modes.orbit.colorization.CommonOrbitToFloat;
import com.fractview.modes.orbit.colorization.OrbitTransfer;
import com.fractview.modes.orbit.functions.Function;

public class EscapeTimeCache extends AbstractOrbitCache {
	
	// private static final String TAG = "ESC";
	private OrbitTransfer.Stats[] stats;
	
	private int[] typeLength; // Bits 0..23 = nrIterations; rest = type.
	private float[] values;
	
	// This must be created from EscapeTime-class
	public EscapeTimeCache(EscapeTime prefs, int width, int height) {
		super(prefs, width, height);
		typeLength = new int[width * height];
		values = new float[width * height];
		
		stats = new OrbitTransfer.Stats[2];
	}
	
	public EscapeTime prefs() {
		return (EscapeTime) super.prefs();
	}
	
	public OrbitTransfer.Stats stats(int index) {
		return stats[index];
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
		
		// Save or fetch value
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

		// TODO: The following thing is ugly...
		
		// Update statistics in env
		if(type == EscapeTime.BAILOUT_TYPE) {
			float tv = prefs().bailoutTransfer().value(v, env.stats[0], stats[0]);
			return prefs().bailoutPalette().color(tv);
		} else /*if(type == EscapeTime.LAKE_TYPE)*/ {
			float tv = prefs().lakeTransfer().value(v, env.stats[1], stats[1]);
			return prefs().lakePalette().color(tv);
		}
	}
	
	@Override
	public void initStatistics() {
		if(!prefs().bailoutTransfer().customStats()) {
			stats[0] = new OrbitTransfer.Stats();
		} else {
			stats[0] = null;
		}

		if(!prefs().lakeTransfer().customStats()) {
			stats[1] = new OrbitTransfer.Stats();
		} else {
			stats[1] = null;
		}
	}

	@Override
	public boolean usesStats() {
		return !(prefs().bailoutTransfer().customStats() && prefs().lakeTransfer().customStats());
	}

	@Override
	public void updateStatisticsFromEnv(Environment env) {
		Env e = (Env) env;
		
		for(int i = 0; i < 2; i++) {
			if(stats[i] != null) {
				stats[i].update(e.stats[i]);
			}
		}
	}

	public RasterTask.Environment createEnvironment() {
		return new Env();
	}

	
	private class Env implements RasterTask.Environment {
		Orbit orbit = prefs().createOrbit();
		OrbitTransfer.Stats[] stats = new OrbitTransfer.Stats[]{new OrbitTransfer.Stats(), new OrbitTransfer.Stats()};

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


	@Override
	protected void moveData(int dx, int dy) {
		int ix = dx >= 0 ? 1 : -1;
		int iy = dy >= 0 ? 1 : -1;
		
		for(int y = (iy > 0 ? 0 : height() - 1); 0 <= y && y < height(); y += iy) {
			if(0 <= y + dy && y + dy < height()) { // We can reuse stuff of y
				for(int x = (ix > 0 ? 0 : width() - 1); 0 <= x && x < width(); x += ix) {
					int index = x + y * width();
					
					if(0 <= x + dx && x + dx < width()) {
						int lastIndex = (x + dx) + (y + dy) * width();
						typeLength[index] = typeLength[lastIndex];
						values[index] = values[lastIndex];
					} else {
						typeLength[index] = 0;
					}
				}
			} else {
				int index = y * width();
				for(int x = 0; x < width(); x++) {
					typeLength[index] = 0;
					index ++;
				}
			}
		}
	}

	public void newFunction(Function function) {
		clear();
		
		setPrefs(new EscapeTime(prefs().affine(), prefs().maxIter(), function, 
				prefs().bailout(), prefs().bailoutMethod(), prefs().bailoutTransfer(), prefs().bailoutPalette(),
				prefs().epsilon(), prefs().lakeMethod(), prefs().lakeTransfer(), prefs().lakePalette()));
	}

	public void newBailout(double bailout) {
		if(bailout != prefs().bailout()) {
			// if new bailout is greater, then clear all bailouts.
			// if new bailout is smaller, then clear everything.
			if(bailout > prefs().bailout()) {
				for(int i = 0; i < width() * height(); i++) {
					int type = typeLength[i] & EscapeTime.TYPE_MASK;
					
					if(type == EscapeTime.BAILOUT_TYPE) {
						typeLength[i] = 0;
					}
				}
			} else {
				clear();
			}
			
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
			// if new epsilon is smaller, then clear all lake.
			// if new epsilon is greater, then clear everything.
			if(epsilon > prefs().epsilon()) {
				for(int i = 0; i < width() * height(); i++) {
					int type = typeLength[i] & EscapeTime.TYPE_MASK;
					
					if(type == EscapeTime.LAKE_TYPE) {
						typeLength[i] = 0;
					}
				}
			} else {
				clear();
			}			
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

}