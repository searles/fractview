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

import java.util.Stack;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;
import at.fractview.modes.AbstractImgCache;
import at.fractview.modes.Preferences;
import at.fractview.modes.orbit.EscapeTime;
import at.fractview.modes.orbit.EscapeTimeCache;
import at.fractview.modes.orbit.colorization.OrbitTransfer.Stats;

import com.google.gson.JsonSyntaxException;

public class EscapeTimeFragment extends Fragment {
	
	private static final String TAG = "EscapeTimeFragment";
	
	public static final String SETTINGS_NAME = "Settings";
	
	public static final String LAST_FRACTAL_KEY = "last.fractal";
	
	
	private static final int INIT_WIDTH = 800;
	private static final int INIT_HEIGHT = 480;
	
	// Contains bitmap, configuration, task
	// Preserved when everything is destroyed
	// This fragment should also manage dialogs.
	
	private BookmarkManager manager;
	
	private EscapeTimeCache image; // Preferences-Set
	private AbstractImgCache.Task task; // Task, created from preferences-set
	
	private Stack<Preferences> history;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// initialize preferences
		manager = new BookmarkManager(getActivity());
		
        // Create history stack
        history = new Stack<Preferences>();

		// Retain this instance so it isn't destroyed when MainActivity and
        // MainFragment change configuration.
        setRetainInstance(true);

		// Try to read from shared preferences
		SharedPreferences settings = getActivity().getSharedPreferences(SETTINGS_NAME, 0);

		String lastJson = settings.getString(LAST_FRACTAL_KEY, null);
		
		if(lastJson != null) {
			Log.d(TAG, "Found last Json");
			
			try {
				EscapeTime prefs = manager.gson().fromJson(lastJson, EscapeTime.class);
				
				if(prefs != null) {
					history.push(prefs);
					
					// Dialog "restore last session"?
					Toast.makeText(this.getActivity(), "Touch \"back\" to restore last fractal", Toast.LENGTH_SHORT).show();
					//this.image = (EscapeTimeCache) prefs.createImgCache(INIT_WIDTH, INIT_HEIGHT);
				}
			} catch(JsonSyntaxException e) {
				Log.e(TAG, "Error in last fractal!");
				e.printStackTrace();
			}
		}
		
		if(this.image == null) {
			Log.d(TAG, "Image was not set yet");
			this.image = (EscapeTimeCache) BookmarkManager.mandelbrot().createImgCache(INIT_WIDTH, INIT_HEIGHT);
		}

        startTask(); // the first fractal should be in the history
	}
	
	@Override
	public void onPause() {
		// Store last fractal in shared preferences
		String json = manager.gson().toJson(image.prefs());
		
		Log.d(TAG, "Putting last fractal into shared preferences");
		
		SharedPreferences settings = getActivity().getSharedPreferences(SETTINGS_NAME, 1);

		settings.edit().putString(LAST_FRACTAL_KEY, json).commit();

		super.onPause();
	}

	@Override
	public void onDestroy() {
		if(task != null && task.isRunning()) {
			task.cancel();
		}

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

	public BookmarkManager bookmarkManager() {
		return manager;
	}

	public Stats stats(int index) {
		return image.stats(index);
	}

	/**
	 * @return Time passed since the task was started
	 */
	public double getElapsedTime() {
		if(task == null) return 0;
		return task.getRunTime();
	}
}
