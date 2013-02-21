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
import android.graphics.Bitmap.Config;
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
import at.fractview.modes.Preferences;
import at.fractview.modes.orbit.EscapeTime;
import at.fractview.modes.orbit.OrbitToFloat;
import at.fractview.modes.orbit.functions.Specification;
import at.fractview.tools.Labelled;

public class EscapeTimeFragment extends Fragment {
	
	private static final String TAG = "EscapeTimeFragment";
	
	// Contains bitmap, configuration, task
	// Preserved when everything is destroyed
	// This fragment should also manage dialogs.
	
	private EscapeTime prefs; // Preferences-Set
	private Preferences.Task task; // Task, created from preferences-set
	
	private Bitmap bitmap; // Bitmap that is shown in imageview
	
	private Stack<Preferences> history;
	
	public EscapeTimeFragment() {
		Log.d(TAG, "Constructor " + hashCode());

		// initialize preferences
        this.prefs = EscapeTimeFragment.initFractal2();
        
        bitmap = Bitmap.createBitmap(900, 600, Config.ARGB_8888);
        
        // Create history stack
        history = new Stack<Preferences>();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate " + hashCode());
		
		// Retain this instance so it isn't destroyed when MainActivity and
        // MainFragment change configuration.
        setRetainInstance(true);
        
        startTask(); // the first fractal should be in the history
	}

	
	// We don't have a view (yet), so I skip things here.

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy " + hashCode());
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
		Log.d(TAG, "History back");
		if(history.isEmpty()) throw new IllegalArgumentException("History is empty");		
		
		Preferences prefs = history.pop();
		setData(prefs, null, false);
			
		return !history.isEmpty();
	}
	
	public void setData(final Preferences prefs, final Bitmap bitmap, final boolean addToHistory) {
		Log.d(TAG, "Setting new data");
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

					if(addToHistory && prefs != null) {
						Log.d(TAG, "Adding to history");
						history.push(EscapeTimeFragment.this.prefs);
					}
					
					task = null;

					if(prefs != null) {
						Log.d(TAG, "Setting new preferences.");
						EscapeTimeFragment.this.prefs = (EscapeTime) prefs;
					}

					if(bitmap != null) {
						Log.d(TAG, "Setting new preferences.");
						EscapeTimeFragment.this.bitmap = bitmap;
					}

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
		Log.d(TAG, "startTask() "  + hashCode());
		
		if(task != null) {
			Log.e(TAG, "BUG: Starting task, but old task is still running");
		}
		
		if(getTargetFragment() != null) {
			Log.d(TAG, "telling image view - target fragment about this: " + getTargetFragment().hashCode());
			// Tell target that we start a calculation
			((ImageViewFragment) getTargetFragment()).initializeTaskView();
		}
		
		task = prefs.calculateInBackground(bitmap);
	}

	
	public void setSize(int width, int height) {
		Log.d(TAG, "New size: " + width + "x" + height);
		// Do not add current to history since we only change the bitmap size
		setData(this.prefs, Bitmap.createBitmap(width, height, Config.ARGB_8888), false);
	}
	
	public void setPrefs(Preferences prefs) {
		Log.d(TAG, "New preferences: " + prefs);
		// New configuration, add to history
		setData(prefs, null, true);
	}
	
	public Bitmap bitmap() {
		return bitmap;
	}
	
	public EscapeTime prefs() {
		return prefs;
	}
	
	private static EscapeTime initFractal() {
		Affine affine = Affine.scalation(4, 4);
		affine.preConcat(Affine.translation(-2, -2));
		
		/* Palette.TwoDim palette2DLake = new Palette.TwoDim(
				new int[][]{
						{0xff000000, 0xff112255, 0xffffffff, 0xff552211},
						{0xFFFFAA00, 0xFF310230, 0xff000764, 0xff206BCB, 0xffEDFFFF},
						{0xffaaff00, 0xff023130, 0xff070064, 0xff6b20cb, 0xffffedff}
						}, 
				true, true, 0, 0, 5, 2 * Math.PI);

		Colorization lakeCol = new Colorization.TwoDimensional(OrbitToDouble.SumDiff, OrbitToDouble.LastArc, palette2DLake);*/

		/*Palette2D paletteBailout = new Palette2D(
				new int[][]{
						{0xff000000, 0xffffffff},
						{0xFFFFAA00, 0xFF310230, 0xff000764, 0xff206BCB, 0xffEDFFFF},
						}, 
				true, true, 0, 0, 1, 20);

		Colorization bailoutColorization = new Colorization.TwoDimensional(OrbitToFloat.Predefined.LengthInterpolated, OrbitToFloat.Predefined.Curvature, paletteBailout);


		
        Palette2D paletteLake = new Palette2D(
                new int[][]{
                        {0xff000000, 0xff112255, 0xffffffff, 0xff552211},
                        {0xFFFFAA00, 0xFF310230, 0xff000764, 0xff206BCB, 0xffEDFFFF},
                        {0xffaaff00, 0xff023130, 0xff070064, 0xff6b20cb, 0xffffedff}
                        },
                true, true, 0, 0, 1f, (float) (2 * Math.PI));

        Colorization lakeColorization = new Colorization.TwoDimensional(OrbitToFloat.Predefined.LastRad, OrbitToFloat.Predefined.LastArc, paletteLake);
*/
        Palette bailoutPalette = new Palette(
				toHSV(new int[]{0xFFFFAA00, 0xFF310230, 0xff000764, 0xff206BCB, 0xffEDFFFF}),
				true, 1);
		
		Palette lakePalette = new Palette(
				toHSV(new int[]{0xff070064, 0xff6b20cb, 0xffffedff, 0xffaaff00, 0xff023130}), 
				true, (float) (2 * Math.PI));

		int maxLength = 1000;
		double bailout = 64.;
		double epsilon = 1e-9;
		
		// z^2 * (z + x) + y*z(n-1)
		// sqr((z^3 + 3(c - 1)z + (c - 1)(c - 2)) / (3 * z^2 + 3(c - 2)z + (c - 1)(c - 2) + 1))
		// horner(0, 0, [-1.4, -1.4], 0, c)
		// Cczcpaczcp (no, not a typo): c(z^3 + 1/z^3), 1
		
		// Golden Ratio:
		// z^3/3 - z^2/2 - z + c
		
		// Functions with two different points: x^3-x^2+c; either 2/3 or 0.
		// AbstractFunction fn = Function.create(Parser.parse("z log z + c").get(), Parser.parse("e^(-1)").get());

		String sf = "c(alpha*z^beta + gamma*z^delta)";
		String si0 = "(- gamma delta / alpha beta) ^ rec(beta - delta)";
		
		Labelled<Expr> fn = new Labelled<Expr>(Parser.parse(sf).get(), sf);
		Labelled<Expr> i0 = new Labelled<Expr>(Parser.parse(si0).get(), si0);
		
		Map<Var, Labelled<Cplx>> ps = new TreeMap<Var, Labelled<Cplx>>();
		
		ps.put(new Var("alpha"), new Labelled<Cplx>(new Cplx(1, 0), "1"));
		ps.put(new Var("beta"), new Labelled<Cplx>(new Cplx(3, 0), "3"));
		ps.put(new Var("gamma"), new Labelled<Cplx>(new Cplx(-1, 0), "-1"));
		ps.put(new Var("delta"), new Labelled<Cplx>(new Cplx(-3, 0), "-3"));
		
		List<Labelled<Expr>> l = new ArrayList<Labelled<Expr>>();
		l.add(i0);
		
		Specification spec = new Specification(fn, l, ps);
		
		return new EscapeTime(affine, maxLength, 
				spec.create(),
				bailout, OrbitToFloat.Predefined.LengthSmooth, bailoutPalette, 
				epsilon, OrbitToFloat.Predefined.LastArc, lakePalette);
	}
	
	private static float[][] toHSV(int[] palette) {
		float[][] hsv = new float[palette.length][3];
		
		for(int i = 0; i < palette.length; i++) {
			Color.colorToHSV(palette[i], hsv[i]);
		}
		
		return hsv;
	}
	
	private static EscapeTime initFractal2() {
		Affine affine = Affine.scalation(4, 4);
		affine.preConcat(Affine.translation(-2, -2));
		
		/* Palette.TwoDim palette2DLake = new Palette.TwoDim(
				new int[][]{
						{0xff000000, 0xff112255, 0xffffffff, 0xff552211},
						{0xFFFFAA00, 0xFF310230, 0xff000764, 0xff206BCB, 0xffEDFFFF},
						{0xffaaff00, 0xff023130, 0xff070064, 0xff6b20cb, 0xffffedff}
						}, 
				true, true, 0, 0, 5, 2 * Math.PI);

		Colorization lakeCol = new Colorization.TwoDimensional(OrbitToDouble.SumDiff, OrbitToDouble.LastArc, palette2DLake);*/

		/*Palette2D paletteBailout = new Palette2D(
				new int[][]{
						{0xff000000, 0xffffffff},
						{0xFFFFAA00, 0xFF310230, 0xff000764, 0xff206BCB, 0xffEDFFFF},
						}, 
				true, true, 0, 0, 1, 20);

		Colorization bailoutColorization = new Colorization.TwoDimensional(OrbitToFloat.Predefined.LengthInterpolated, OrbitToFloat.Predefined.Curvature, paletteBailout);


		
        Palette2D paletteLake = new Palette2D(
                new int[][]{
                        {0xff000000, 0xff112255, 0xffffffff, 0xff552211},
                        {0xFFFFAA00, 0xFF310230, 0xff000764, 0xff206BCB, 0xffEDFFFF},
                        {0xffaaff00, 0xff023130, 0xff070064, 0xff6b20cb, 0xffffedff}
                        },
                true, true, 0, 0, 1f, (float) (2 * Math.PI));

        Colorization lakeColorization = new Colorization.TwoDimensional(OrbitToFloat.Predefined.LastRad, OrbitToFloat.Predefined.LastArc, paletteLake);
*/
        Palette bailoutPalette = new Palette(
				toHSV(new int[]{0xFFFFAA00, 0xFF310230, 0xff000764, 0xff206BCB, 0xffEDFFFF}),
				true, 1);
		
		Palette lakePalette = new Palette(
				toHSV(new int[]{0xff070064, 0xff6b20cb, 0xffffedff, 0xffaaff00, 0xff023130}), 
				true, 360.f);

		int maxLength = 1000;
		double bailout = 64.;
		double epsilon = 1e-9;
		
		// z^2 * (z + x) + y*z(n-1)
		// sqr((z^3 + 3(c - 1)z + (c - 1)(c - 2)) / (3 * z^2 + 3(c - 2)z + (c - 1)(c - 2) + 1))
		// horner(0, 0, [-1.4, -1.4], 0, c)
		// Cczcpaczcp (no, not a typo): c(z^3 + 1/z^3), 1
		
		// Golden Ratio:
		// z^3/3 - z^2/2 - z + c
		
		// Functions with two different points: x^3-x^2+c; either 2/3 or 0.
		// AbstractFunction fn = Function.create(Parser.parse("z log z + c").get(), Parser.parse("e^(-1)").get());

		String sf = "z^2 + c";
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
		
		Specification spec = new Specification(fn, l, ps);
		
		return new EscapeTime(affine, maxLength, 
				spec.create(),
				bailout, OrbitToFloat.Predefined.LengthSmooth, bailoutPalette, 
				epsilon, OrbitToFloat.Predefined.LastArc, lakePalette);
	}
}
