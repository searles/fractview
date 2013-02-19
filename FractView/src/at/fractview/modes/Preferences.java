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
package at.fractview.modes;

import android.graphics.Bitmap;

public interface Preferences {
	public static interface Task {
		/**
		 * @return true, if the Task is most likely to be still running (there might be race conditions)
		 * If isRunning returns false, the task is not running anymore for sure.
		 */
		boolean isRunning();
		
		/**
		 * @return true, if cancel was called on this task
		 */
		boolean isCancelled();
		void cancel();
		void join() throws InterruptedException; // Wait until task has terminated (for whatever reason)
		
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
