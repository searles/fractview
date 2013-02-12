package at.fractview.math;

public class Cplx {

	private double re;
	private double im;

	public Cplx() {
		this.re = 0.;
		this.im = 0.;
	}

	public Cplx(double re, double im) {
		this.re = re;
		this.im = im;
	}
	
	public Cplx(Cplx cplx) {
		this(cplx.re, cplx.im);
	}
	
	public boolean isInfinite() {
		return Double.isInfinite(re) || Double.isInfinite(im);
	}
	
	public boolean isNaN() {
		return Double.isNaN(re) || Double.isNaN(im);
	}
	
	// Setter and getter
	public double re() {
		return re;
	}

	public double im() {
		return im;
	}

	public Cplx set(double d) {
		this.re = d;
		this.im = 0.;
		return this;
	}

	public Cplx set(Cplx that) {
		this.set(that.re, that.im);
		return this;
	}

	public Cplx set(double re, double im) {
		this.re = re;
		this.im = im;
		return this;
	}

	// Some values of interest
	public double absSqr() {
		return re * re + im * im;
	}
	
	public double distSqr(Cplx arg) {
		double dr = re - arg.re;
		double di = im - arg.im;
		
		return dr * dr + di * di;
	}
	
	public double arc() {
		double arc = Math.atan2(im(), re());
		if(arc < 0) arc += 2 * Math.PI;
		return arc;
	}

	public double abs() {
		return Math.hypot(im(), re());
	}
	
	// Now come the operations
	
	// First binary things:
	public Cplx add(double re0, double im0, double re1, double im1) {
		this.re = re0 + re1;
		this.im = im0 + im1;
		return this;
	}

	public Cplx sub(double re0, double im0, double re1, double im1) {
		this.re = re0 - re1;
		this.im = im0 - im1;
		return this;
	}
	
	public Cplx mul(double re0, double im0, double re1, double im1) {
		this.re = re0 * re1 - im0 * im1; 
		this.im = re0 * im1 + im0 * re1;
		return this;
	}

	public Cplx div(double re0, double im0, double re1, double im1) {
		double abs = re1 * re1 + im1 * im1;

		this.re = (re0 * re1 + im0 * im1) / abs; 
		this.im = (-re0 * im1 + im0 * re1) / abs;

		return this;
	}
	
	public Cplx powInt(Cplx zn, int exp) {

		boolean inv = false;
		
		if(exp < 0) {
			inv = true;
			exp = -exp;
		}
		
		double zr = zn.re;
		double zi = zn.im;
		
		this.set(1., 0.);
		
		while (exp > 0) { // Power
			if ((exp & 1) != 0) {
				this.mul(re, im, zr, zi);
			}

			exp >>= 1;
		
			// squaring the base
			double t = zr;
			zr = zr * zr - zi * zi;
			zi = 2 * t * zi;
		}

		if(inv) this.inv(this);
		
		return this;
	}

	public Cplx pow(double re0, double im0, double re1, double im1) {
		// TODO: Something's wrong here... 0.^something gives Nan?
		double hyp = Math.hypot(re0, im0);
		
		if(hyp > 0.) {
			double reLn = Math.log(hyp); // re for ln
			double imLn = Math.atan2(im0, re0); // im for ln
			
			// ln(a) * b
			double reExp = reLn * re1 - imLn * im1;
			double imExp = reLn * im1 + imLn * re1;
	
			// exp( ... )
			double ea = Math.exp(reExp);		
			
			this.re = ea * Math.cos(imExp);
			this.im = ea * Math.sin(imExp);
	
			return this;
		} else {
			if(re1 > 0) {
				return this.set(0, 0);
			} else if(re1 == 0.) {
				return this.set(1., 0.);
			} else {
				return this.set(Double.POSITIVE_INFINITY, 0);
			}
		}
	}
	
	// Unary operations:
	// First things like sqr, inv, neg etc... 

	public Cplx neg(double re, double im) {
		this.re = -re;
		this.im = -im;
		
		return this;
	}

	public Cplx sqr(double re, double im) {
		this.re = re * re - im * im;
		this.im = 2. * re * im;
		
		return this;
	}
	
	public Cplx inv(double re, double im) {
		double abs = re * re + im * im;

		this.re = re / abs;
		this.im = -im / abs;
		
		return this;
	}

	// Exp-stuff
	
	public Cplx exp(double re, double im) {
		double ea = Math.exp(re);
		this.re = ea * Math.cos(im);
		this.im = ea * Math.sin(im);
		return this;
	}
	
	public Cplx log(double re, double im) {
		this.re = Math.log(Math.hypot(re, im));
		this.im = Math.atan2(im, re);
		
		return this;
	}

	// Trigonometric functions:
	public Cplx sin(double re, double im) {
		// sin x + iy = sin x * cosh y + i cos x * sinh y
		this.re = Math.sin(re) * Math.cosh(im);
		this.im = Math.cos(re) * Math.sinh(im);
		
		return this;
	}
	
	public Cplx cos(double re, double im) {
		// cos x + iy = cos x * cosh y - i sin x * sinh y
		this.re = Math.cos(re) * Math.cosh(im);
		this.im = -Math.sin(re) * Math.sinh(im);
		
		return this;
	}
	
	public Cplx tan(double re, double im) {
		double sx = Math.sin(re);
		double cx = Math.cos(re);
		double shy = Math.sinh(im);
		double chy = Math.cosh(im);
		
		double abs = cx * cx + shy * shy;
		
		this.re = sx * cx / abs;
		this.im = shy * chy / abs;

		return this;
	}

	public Cplx atan(double re, double im) {
		// TODO 
		/* http://mathforum.org/library/drmath/view/72732.html
		 * 
		 *  http://gcc.gnu.org/onlinedocs/gcc-4.6.3/libstdc++/api/a00813_source.html */
		return null;
	}

	public Cplx sinh(double re, double im) {
		// sinh x + iy = sinh x * cos y + i cosh x * sin y
		this.re = Math.sinh(re) * Math.cos(im);
		this.im = Math.cosh(re) * Math.sin(im);
		
		return this;
	}

	public Cplx cosh(double re, double im) {
		// cosh x + iy = cosh x * cos y + i sinh x * sin y
		this.re = Math.cosh(re) * Math.cos(im);
		this.im = Math.sinh(re) * Math.sin(im);
		
		return this;
	}
	
	public Cplx tanh(double re, double im) {
		// TODO
		double shx = Math.sinh(re);
		double chx = Math.cosh(re);
		double sy = Math.sin(im);
		double cy = Math.cos(im);
		
		double abs = chx * chx + sy * sy;
		
		this.re = shx * chx / abs;
		this.im = sy * cy / abs;

		return null;
	}

	public Cplx atanh(double re, double im) {
		// TODO
		return null;
	}

	public Cplx conj(double re, double im) {
		this.re = re;
		this.im = -im;
		
		return this;
	}

	public Cplx sqrt(double re, double im) {
		double r = Math.hypot(re,  im);
		
		this.re = Math.sqrt((r + re) / 2.);
		this.im = Math.sqrt((r - re) / 2.);
		
		if(im < 0) this.im = -this.im;
		
		return this;
	}

	public Cplx add(Cplx arg0, Cplx arg1) {
		return this.add(arg0.re, arg0.im, arg1.re, arg1.im);
	}
	
	public Cplx sub(Cplx arg0, Cplx arg1) {
		return this.sub(arg0.re, arg0.im, arg1.re, arg1.im);
	}

	public Cplx mul(Cplx arg0, Cplx arg1) {
		return this.mul(arg0.re, arg0.im, arg1.re, arg1.im);
	}

	public Cplx div(Cplx arg0, Cplx arg1) {
		return this.div(arg0.re, arg0.im, arg1.re, arg1.im);
	}
	
	public Cplx pow(Cplx arg0, Cplx arg1) {
		return this.pow(arg0.re, arg0.im, arg1.re, arg1.im);
	}	

	public Cplx neg(Cplx arg) {
		return this.neg(arg.re, arg.im);
	}

	public Cplx sqr(Cplx arg) {
		return this.sqr(arg.re, arg.im);
	}

	public Cplx inv(Cplx arg) {
		return this.inv(arg.re, arg.im);
	}

	public Cplx exp(Cplx arg) {
		return this.exp(arg.re, arg.im);
	}
	
	public Cplx log(Cplx arg) {
		return this.log(arg.re, arg.im);
	}

	public Cplx sin(Cplx arg) {
		return this.sin(arg.re, arg.im);
	}
	
	public Cplx cos(Cplx arg) {
		return this.cos(arg.re, arg.im);
	}
	
	public Cplx tan(Cplx arg) {
		return this.tan(arg.re, arg.im);
	}

	public Cplx atan(Cplx arg) {
		return this.atan(arg.re, arg.im);
	}


	public Cplx sinh(Cplx arg) {
		return this.sinh(arg.re, arg.im);
	}

	public Cplx cosh(Cplx arg) {
		return this.cosh(arg.re, arg.im);
	}
	
	public Cplx tanh(Cplx arg) {
		return this.tanh(arg.re, arg.im);
	}

	public Cplx atanh(Cplx arg) {
		return this.atanh(arg.re, arg.im);
	}
	
	public Cplx conj(Cplx arg) {
		return this.conj(arg.re, arg.im);
	}
	
	public Cplx sqrt(Cplx arg) {
		return this.sqrt(arg.re, arg.im);
	}

	// Stuff for affine things
	public Cplx map(Cplx arg, Affine affine) {
		return affine.map(arg, this);
	}

	public Cplx invMap(Cplx arg, Affine affine) {
		return affine.invmap(arg, this);
	}
	
	public String toString() {
		return "(" + re() + ", " + im() + ")";
	}
}