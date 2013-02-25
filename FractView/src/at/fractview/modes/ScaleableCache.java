package at.fractview.modes;

import at.fractview.math.Affine;

public abstract class ScaleableCache extends AbstractImgCache {
	
	protected ScaleableCache(ScaleablePrefs prefs, int width, int height) {
		super(prefs, width, height);
	}
	
	public ScaleablePrefs prefs() {
		return (ScaleablePrefs) super.prefs();
	}

	public void newRelativeScale(float[] m) {
		// fields 6, 7, 8 are ignored since they are considered to be 0f, 0f, 1f
		double a = m[0];
		double b = m[1];
		double e = m[2];
		double c = m[3];
		double d = m[4];
		double f = m[5];
		
		Affine affine = Affine.create(a, b, e, c, d, f);
		
		ScaleablePrefs p = prefs();
		affine.preConcat(p.affine());
		
		// And update preferences
		clear();
		setPrefs(p.newAffineInstance(affine));
	}
	
	protected abstract void moveScale(int dx, int dy);
}