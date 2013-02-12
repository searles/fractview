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

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import at.fractview.modes.ScaleablePrefs;

public class ImageViewFragment extends Fragment {
	
	private static final String TAG = "ViewFragment";

	public static final String TASK_TAG = "at.fractview.TASK_FRAGMENT_TAG";
	static final int TASK_FRAGMENT = 0;
	
	private static final int MILLISECONDS_TILL_UPDATE = 125; // 8 times per second

	private static final Matrix.ScaleToFit viewPolicy = Matrix.ScaleToFit.CENTER; 

	private ImageView imageView; // Viewer for image

	private Handler handler; // Handler to regularly update view
	private Runnable updateView;
	
	private Matrix viewToImageMatrix; // Matrix to scale image to screen size
	private Matrix inverseViewToImageMatrix; // Corresponding inverse matrix
	
	private Matrix imageMatrix; // Matrix that is used on the bitmap
	
	private EscapeTimeFragment taskFragment;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {        
		
		Log.v(TAG, "onCreateView");

		View v = inflater.inflate(R.layout.imageview, container, false);
		
		imageView = (ImageView) v.findViewById(R.id.imageView);

		imageView.setOnTouchListener(new TouchListener());
		
		// Create data structures
		viewToImageMatrix = new Matrix();
		inverseViewToImageMatrix = new Matrix();

		imageMatrix = new Matrix();

		// And set matrices as soon as view is layouted
		imageView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			// TODO: Would this be deleted on time?
		    @Override
		    public void onGlobalLayout() {
		    	Log.v(TAG, "Global layout: updating image view matrices");
		    	initImageMatrix();
		    }
		});
		
		// Create handler to get regular updates
		this.handler = new Handler();
		this.updateView = new Runnable() {
			// Careful: this runnable holds a reference to the MainActivity
			
			@Override
			public void run() {
				// update view
				imageView.invalidate();

				if(taskFragment.isRunning()) {
					handler.postDelayed(this, MILLISECONDS_TILL_UPDATE);
				} else {
					Log.v(this.toString(), "No further updates because task is not running anymore");
				}
			}
		};

		// Get (if there is) old DataFragment
		taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(TASK_TAG);
		
		if(taskFragment == null) {
			Log.v(TAG, "No saved instance --> creating new data fragment");
			taskFragment = new EscapeTimeFragment();
			getFragmentManager().beginTransaction().add(taskFragment, TASK_TAG).commit();

			taskFragment.setTargetFragment(this, TASK_FRAGMENT);

			// Start task and also handler
			taskFragment.startTask();
			// via target this will call back to here.
		} else {
			// set target
			taskFragment.setTargetFragment(this, TASK_FRAGMENT);
			
			// start handler
			initializeTaskView();
		}

		
        // Inflate the layout for this fragment
        return v;
    }
	
	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy: Stopping handler");
		this.handler.removeCallbacks(updateView);
		super.onDestroy();
	}
	
	private void initImageMatrix() {
		// Set initMatrix + inverse to current view-size
		float vw = imageView.getWidth();
		float vh = imageView.getHeight();
		
		// Right after creation, this view might not 
		// be inside anything and therefore have size 0.
		if(vw <= 0 && vh <= 0) return; // do nothing.
		
		float bw = taskFragment.bitmap().getWidth();
		float bh = taskFragment.bitmap().getHeight();
		
		RectF viewRect = new RectF(0f, 0f, vw, vh);
		RectF bitmapRect;
		
		if(vw > vh) {
			// if width of view is bigger, match longer side to it
			bitmapRect = new RectF(0f, 0f, Math.max(bw, bh), Math.min(bw, bh));
		} else {
			bitmapRect = new RectF(0f, 0f, Math.min(bw, bh), Math.max(bw, bh));
		}
		
		viewToImageMatrix.setRectToRect(bitmapRect, viewRect, viewPolicy);
		
		if(vw > vh ^ bw > bh) {
			// Turn centerImageMatrix by 90 degrees
			Matrix m = new Matrix();
			m.postRotate(90f);
			m.postTranslate(bh, 0);
			
			viewToImageMatrix.preConcat(m);
		}
		
		if(!viewToImageMatrix.invert(inverseViewToImageMatrix)) {
			throw new IllegalArgumentException("matrix cannot be inverted...");
		}
		
		imageView.setImageMatrix(viewToImageMatrix);
	}

	public void initializeTaskView() {
		// TODO: A better name would be "initializeTaskView".
		Log.v(TAG, "initializing view...");

		imageView.setImageBitmap(taskFragment.bitmap());
		initImageMatrix(); // Bitmap might have been resized

		handler.post(updateView);
	}
	
	public void taskCancelled() {
		Log.v(TAG, "Got info that task was cancelled");
		handler.removeCallbacks(updateView);
	}
	
	private void applyTouch(Matrix bitmapMatrix, Matrix prefsMatrix) {
		taskFragment.cancelTask(); // stop calculation

		// Get old data
		ScaleablePrefs scaleable = (ScaleablePrefs) taskFragment.prefs();		
		Bitmap bitmap = taskFragment.bitmap();

		// Update zoom
		Matrix m = new Matrix();
		prefsMatrix.invert(m);
		
		float[] matrix = new float[9];
		m.getValues(matrix);
		
		// Create new data
		ScaleablePrefs prefs = scaleable.relativelyScaledInstance(matrix);
		Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		
		// Create preview
		Canvas c = new Canvas(newBitmap);
		c.drawBitmap(bitmap, bitmapMatrix, null);
		
		// And set new data
		taskFragment.setData(prefs, newBitmap);

		taskFragment.startTask(); // start calculation
	}
	
	private class TouchListener implements OnTouchListener {
		
		private MultiTouch bitmapTouch; // current touch event, scaled to bitmap size
		private MultiTouch prefsTouch; // current touch event, scaled to preferences size
		
		@Override
		public boolean onTouch(View v, MotionEvent evt) {
			// Careful not to confuse index and id of evt

			ImageView view = (ImageView) v;
			ScaleablePrefs scaleable = (ScaleablePrefs) taskFragment.prefs();

			int w = taskFragment.bitmap().getWidth();
			int h = taskFragment.bitmap().getHeight();

			switch (evt.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				bitmapTouch = new MultiTouch();
				prefsTouch = new MultiTouch();

				for(int i = 0; i < evt.getPointerCount(); i++) {
					// Yes, in fact pointerCounter can be > 1.

					float[] p = new float[]{evt.getX(i), evt.getY(i)};
					inverseViewToImageMatrix.mapPoints(p);

					int id = evt.getPointerId(i);

					bitmapTouch.down(id, p);

					scaleable.norm(p, w, h);
					prefsTouch.down(id, p);
				}

				break;
			case MotionEvent.ACTION_UP:
				// Apply selection: Change prefs, create new bitmap with preview and set it in view
				if(bitmapTouch != null) {
					applyTouch(bitmapTouch.matrix(), prefsTouch.matrix());
					bitmapTouch = prefsTouch = null;
				} else {
					Log.w(TAG, "Received Action-up, but there's no touch-object...");
				}
				break;

			case MotionEvent.ACTION_MOVE:
				if (bitmapTouch != null) {
					for (int i = 0; i < evt.getPointerCount(); i++) {
						float[] p = new float[]{evt.getX(i), evt.getY(i)};
						inverseViewToImageMatrix.mapPoints(p);

						int id = evt.getPointerId(i); // Careful, we need the id in the touch event
						bitmapTouch.moveTo(id, p);

						scaleable.norm(p, w, h);
						prefsTouch.moveTo(id, p);					
					}
				}

				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				if (bitmapTouch != null) {
					int i = evt.getActionIndex();
					int id = evt.getPointerId(i);

					float[] p = new float[]{evt.getX(i), evt.getY(i)};
					inverseViewToImageMatrix.mapPoints(p);

					bitmapTouch.down(id, p);

					scaleable.norm(p, w, h);
					prefsTouch.down(id, p);					
				}
				break;
			case MotionEvent.ACTION_POINTER_UP:
				if(bitmapTouch != null) {
					int id = evt.getPointerId(evt.getActionIndex());
					bitmapTouch.up(id);
					prefsTouch.up(id);
				}
				break;
			default:
			}

			if (bitmapTouch != null) {
				imageMatrix.setConcat(viewToImageMatrix, bitmapTouch.matrix());
				
				view.setImageMatrix(imageMatrix);
				
				v.invalidate();
			}

			return true;
		}
	}
}
