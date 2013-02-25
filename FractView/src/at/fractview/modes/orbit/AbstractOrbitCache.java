package at.fractview.modes.orbit;

import at.fractview.modes.RasterTask;
import at.fractview.modes.ScaleableCache;

public abstract class AbstractOrbitCache extends ScaleableCache implements RasterTask.Rasterable {

	protected AbstractOrbitCache(AbstractOrbitPrefs prefs, int width, int height) {
		super(prefs, width, height);
	}
	
	public void setMaxIter(int maxIter) {
		AbstractOrbitPrefs p = (AbstractOrbitPrefs) prefs();
		
		updateMaxIter(p.maxIter(), maxIter);
		
		setPrefs(p.newMaxIterInstance(maxIter));
	}
	
	/** Tells subclasses that maximum number of iterations is to be updated
	 * and that they should update their fields accordingly
	 * @param oldMaxIter
	 * @param newMaxIter
	 */
	protected abstract void updateMaxIter(int oldMaxIter, int newMaxIter);
	
	@Override
	public RasterTask calculateInBackground() {
		RasterTask task = new RasterTask(this);
		task.start(this);
		
		return task;
	}
}