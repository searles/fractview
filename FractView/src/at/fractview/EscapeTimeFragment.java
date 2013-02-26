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
package at.fractview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import at.fractview.math.Affine;
import at.fractview.math.Cplx;
import at.fractview.math.colors.Palette;
import at.fractview.math.tree.Expr;
import at.fractview.math.tree.Parser;
import at.fractview.math.tree.Var;
import at.fractview.modes.AbstractImgCache;
import at.fractview.modes.Preferences;
import at.fractview.modes.orbit.EscapeTime;
import at.fractview.modes.orbit.EscapeTimeCache;
import at.fractview.modes.orbit.colorization.CommonOrbitToFloat;
import at.fractview.modes.orbit.colorization.CommonTransfer;
import at.fractview.modes.orbit.colorization.OrbitTransfer;
import at.fractview.modes.orbit.functions.Function;
import at.fractview.tools.Labelled;

public class EscapeTimeFragment extends Fragment {
	
	private static final String TAG = "EscapeTimeFragment";
	
	private static final int INIT_WIDTH = 800;
	private static final int INIT_HEIGHT = 480;
	
	// Contains bitmap, configuration, task
	// Preserved when everything is destroyed
	// This fragment should also manage dialogs.
	
	private EscapeTimeCache image; // Preferences-Set
	private AbstractImgCache.Task task; // Task, created from preferences-set
	
	private Stack<Preferences> history;
	
	public EscapeTimeFragment() {
		// initialize preferences
        this.image = (EscapeTimeCache) EscapeTimeFragment.initFractal().createImgCache(INIT_WIDTH, INIT_HEIGHT);
        
        // Create history stack
        history = new Stack<Preferences>();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Retain this instance so it isn't destroyed when MainActivity and
        // MainFragment change configuration.
        setRetainInstance(true);
        
        startTask(); // the first fractal should be in the history
	}

	
	// We don't have a view (yet), so I skip things here.

	@Override
	public void onDestroy() {
		if(task != null && task.isRunning()) task.cancel();
		super.onDestroy();
	}
	
	public boolean taskIsRunning() {
		return task != null && task.isRunning();
	}
	
	public boolean taskIsCancelled() {
		return task != null && task.isCancelled();
	}
	
	public boolean isHistoryEmpty() {
		return history.isEmpty();
	}
	
	/** One step back in history.
	 * @return false, if after this call the history is empty. null indicates that the
	 * @throws IllegalArgumentException If the history is empty before this call.
	 */
	public boolean historyBack() throws IllegalArgumentException {
		//Log.d(TAG, "History back");
		if(history.isEmpty()) throw new IllegalArgumentException("History is empty");		
		
		modifyImage(new UnsafeImageEditor() {
			@Override
			public void edit(AbstractImgCache cache) {
				cache.setNewPreferences(history.pop());
			}
		}, false);
			
		return !history.isEmpty();
	}
	
	public void modifyImage(final UnsafeImageEditor editor, final boolean addToHistory) {
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				try {
					Log.d(TAG, "Terminating threads");

					// We need to cancel the old running task and then put in data and then start it.
					if(task.isRunning()) {
						task.cancel();						
						task.join();
					}

					if(addToHistory) {
						// Add to history
						history.push(EscapeTimeFragment.this.image.prefs());
					}
					
					task = null;

					editor.edit(EscapeTimeFragment.this.image);

					// Create new task
					startTask();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
	}
	
	/**
	 * Tasks should not be started automatically because there might be some other
	 * things attached to it (like starting a progress view or regularly updating a
	 * preview).
	 */
	private void startTask() {
		if(task != null) {
			// This must not happen. Someone forgot to call "cancelTask"...
			Log.e(TAG, "BUG: Starting task, but old task is still running");
			
			if(task.isRunning()) {
				// Race condition?
				throw new IllegalStateException("Old task is still running!");
			}
		}
		
		if(getTargetFragment() != null) {
			// Tell target that we start a calculation
			((ImageViewFragment) getTargetFragment()).initializeTaskView();
		}
		
		task = image.calculateInBackground();
	}

	
	public Bitmap bitmap() {
		return image.bitmap();
	}
	
	public Preferences prefs() {
		return image.prefs();
	}

	private static float[][] toHSV(int[] palette) {
		float[][] hsv = new float[palette.length][3];
		
		for(int i = 0; i < palette.length; i++) {
			Color.colorToHSV(palette[i], hsv[i]);
		}
		
		return hsv;
	}
	
	private static EscapeTime initFractal() {
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
		
		/*ps.put(new Var("alpha"), new Labelled<Cplx>(new Cplx(1, 0), "1"));
		ps.put(new Var("beta"), new Labelled<Cplx>(new Cplx(3, 0), "3"));
		ps.put(new Var("gamma"), new Labelled<Cplx>(new Cplx(-1, 0), "-1"));
		ps.put(new Var("delta"), new Labelled<Cplx>(new Cplx(-3, 0), "-3"));*/
		
		List<Labelled<Expr>> l = new ArrayList<Labelled<Expr>>();
		l.add(i0);
		
		Function function = new Function(fn, l, ps);
		
		return new EscapeTime(affine, maxIter, function,
				bailout, CommonOrbitToFloat.Length_Smooth, new OrbitTransfer(false, 0f, 1f, CommonTransfer.Log), bailoutPalette, 
				epsilon, CommonOrbitToFloat.Last_Angle, new OrbitTransfer(true, 0f, 1f, CommonTransfer.None), lakePalette);
	}
}
