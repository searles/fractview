package at.fractview.modes;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;

public abstract class AbstractImgCache {
	
	protected int width;
	protected int height;
	
	private Bitmap bitmap;
	private Canvas canvas;
	
	private Preferences prefs;
	
	protected AbstractImgCache(Preferences prefs, int width, int height) {
		this.prefs = prefs;
		this.width = width;
		this.height = height;
		this.bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		this.canvas = new Canvas(bitmap);
	}
	
	public Preferences prefs() {
		return this.prefs;
	}
	
	protected void setPrefs(Preferences prefs) {
		this.prefs = prefs;
	}
	
	public void setNewPreferences(Preferences prefs) {
		// If we call it from outside, we should also call clear.
		setPrefs(prefs);
		clear();
	}
	
	public Canvas canvas() {
		return canvas;
	}
	
	public Bitmap bitmap() {
		return bitmap;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}
	
	public void clearBitmap() {
		bitmap.eraseColor(android.graphics.Color.TRANSPARENT);
	}
	
	public void resizeBitmap(int width, int height) {
		this.bitmap = null;
		
		System.gc();
		
		this.width = width;
		this.height = height;
		
		this.bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		this.canvas = new Canvas(bitmap);
	}
	
	public abstract void clear();
	
	public abstract void resize(int width, int height);

	
	/** Calculates the image associated with this Preferences
	 * in Bitmap. Try to favor parts around x0/y0 in calculation (these parameters
	 * might be ignored).
	 * @return An instance of the task-interface to manage background threads.
	 */
	public abstract Task calculateInBackground();
	
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
}