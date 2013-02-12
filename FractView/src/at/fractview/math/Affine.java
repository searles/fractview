package at.fractview.math;


public class Affine {
	
	public static Affine scalation(double sx, double sy) {
		return new Affine(new double[]{
				sx, 0, 0, 
				0, sy, 0});
	}
	
	public static Affine rotation(double arc) {
		double c = Math.cos(arc);
		double s = Math.sin(arc);
		
		return new Affine(new double[]{
				c, -s, 0, 
				s, c, 0});
	}
	
	public static Affine translation(double tx, double ty) {
		return new Affine(new double[]{
				1, 0, tx, 
				0, 1, ty});
	}
	
	public static Affine create(double...m) {
		// TODO: Copy of array!
		return new Affine(m);
	}
	
	private double[] m;
	
	private Affine(double...m) {
		this.m = m;
	}
	
	public Affine concat(Affine a0, Affine a1) {
		double m0 = a0.m[0] * a1.m[0] + a0.m[1] * a1.m[3];
		double m1 = a0.m[0] * a1.m[1] + a0.m[1] * a1.m[4];
		double m2 = a0.m[0] * a1.m[2] + a0.m[1] * a1.m[5] + a0.m[2];
		double m3 = a0.m[3] * a1.m[0] + a0.m[4] * a1.m[3];
		double m4 = a0.m[3] * a1.m[1] + a0.m[4] * a1.m[4];
		double m5 = a0.m[3] * a1.m[2] + a0.m[4] * a1.m[5] + a0.m[5];
		
		m[0] = m0;
		m[1] = m1;
		m[2] = m2;
		m[3] = m3;
		m[4] = m4;
		m[5] = m5;
		
		return this;
	}
	
	public Affine preConcat(Affine a) {
		return this.concat(a, this);
	}

	public Affine postConcat(Affine a) {
		return this.concat(this, a);
	}
	
	public double[] get() {
		return m;
	}
	
	public double det() {
		return m[0] * m[4] - m[1] * m[3];
	}
	
	public Cplx map(Cplx src, Cplx dest) {
		dest.set(
				m[0] * src.re() + m[1] * src.im() + m[2],
				m[3] * src.re() + m[4] * src.im() + m[5]
		);
		
		return dest;
	}
	
	public Cplx invmap(Cplx src, Cplx dest) {
		double det = det();
		
		dest.set(src.re() - m[2], src.im() - m[5]);
		
		dest.set(
				(m[0] * dest.re() + m[3] * dest.im()) / det,
				(m[1] * dest.re() + m[4] * dest.im()) / det
		);
		
		return dest;
	}
	
}
