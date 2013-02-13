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
import java.util.TreeMap;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
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
	
	public EscapeTimeFragment() {
        initFractal();
        bitmap = Bitmap.createBitmap(900, 600, Config.ARGB_8888);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.v(TAG, "onCreate finally called");
		
		// Retain this instance so it isn't destroyed when MainActivity and
        // MainFragment change configuration.
        setRetainInstance(true);
	}

	
	// We don't have a view (yet), so I skip things here.

	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy");
		if(task != null && task.isRunning()) task.cancel();
		super.onDestroy();
	}
	
	public boolean isRunning() {
		return task != null && task.isRunning();
	}
	
	public void cancelTask() {
		if(task != null) {
			if(task.isRunning()) {
				Log.v(TAG, "Cancelling task");
				task.cancel();
				if(getTargetFragment() != null) {
					// Tell target that we cancel a calculation
					try {
						((ImageViewFragment) getTargetFragment()).taskCancelled();
					} catch(ClassCastException e) {
						// TODO
						Log.w(TAG, getTargetFragment().toString());
					}
				}
			}
			
			Log.v(TAG, "Deleting old task");
			
			task = null;
		}
	}
	
	/**
	 * Tasks should not be started automatically because there might be some other
	 * things attached to it (like starting a progress view or regularly updating a
	 * preview).
	 */
	public void startTask() {
		if(task != null) {
			Log.e(TAG, "Starting task, but current task is not null!");
			
			cancelTask();
		}
		
		if(getTargetFragment() != null) {
			// Tell target that we start a calculation
			((ImageViewFragment) getTargetFragment()).initializeTaskView();
		}
		
		task = prefs.calculateInBackground(bitmap);
	}
	
	private void initFractal() {
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
				new int[]{0xFFFFAA00, 0xFF310230, 0xff000764, 0xff206BCB, 0xffEDFFFF},
				true, 1);
		
		Palette lakePalette = new Palette(
				new int[]{0xff070064, 0xff6b20cb, 0xffffedff, 0xffaaff00, 0xff023130}, 
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
		String si0 = "(- gamma delta / alpha beta) ^ inv(beta - delta)";
		
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
		
		prefs = new EscapeTime(affine, maxLength, 
				spec.create(),
				bailout, OrbitToFloat.Predefined.LengthSmooth, bailoutPalette, 
				epsilon, OrbitToFloat.Predefined.LastArc, lakePalette);
	}
	
	public void setSize(int width, int height) {
		Log.v(TAG, "New size: " + width + "x" + height);
		cancelTask();

		setData(this.prefs, Bitmap.createBitmap(width, height, Config.ARGB_8888));
		
		startTask();
	}
	

	public void setData(Preferences prefs, Bitmap bitmap) {
		if(this.task != null && this.task.isRunning()) {
			task.cancel();
			Log.v(TAG, "cancelling task.");
		}
		
		this.prefs = (EscapeTime) prefs;
		this.bitmap = bitmap;
	}
	
	public void setPrefs(Preferences prefs) {
		cancelTask();
		
		this.bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		this.prefs = (EscapeTime) prefs;
		
		startTask();
	}
	
	public Bitmap bitmap() {
		return bitmap;
	}
	
	public EscapeTime prefs() {
		return prefs;
	}
}
