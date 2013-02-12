package at.fractview.modes;

import android.graphics.Bitmap;

public interface Preferences {
	public static interface Task {
		/**
		 * @return true, if the Task is most likely to be still running (there might be race conditions)
		 * If isRunning returns false, the task is not running anymore for sure.
		 */
		boolean isRunning();
		void cancel();
		
		// TODO: Things that are running 
		//int progress();
	}

	/** Calculates the image associated with this Preferences
	 * in Bitmap. Try to favor parts around x0/y0 in calculation (these parameters
	 * might be ignored).
	 * @param bitmap The bitmap that is created
	 * @return An instance of the task-interface to manage background threads.
	 */
	Task calculateInBackground(Bitmap bitmap);
}
