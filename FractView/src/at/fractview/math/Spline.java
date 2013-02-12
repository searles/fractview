package at.fractview.math;

public interface Spline {
	float y(float x);
	
	public static class Lin implements Spline {
		private float[] ys;

		public Lin(float[] ys) {
			this.ys = ys;
		}
		
		/**
		 * @param x in range of 0 to 1
		 * @param y0
		 * @param y1
		 * @return
		 */
		public static float y(float x, float y0, float y1) {
			return y0 * (1 - x) + y1 * x;
		}
		
		public static float x(float y, float y0, float y1) {
			return (y - y0) / (y1 - y0);
		}

		public float y(float x) {
			if(x < 0) {
				return ys[0];
			} else if(x >= 1) {
				return ys[ys.length - 1];
			} else {
				x *= ys.length - 1;
				
				int index = (int) x;
				x -= index;

				return y(x, ys[index], ys[index + 1]);
			}
		}
	}

	public static class Cubic0 implements Spline {
		private float[] ys;

		public Cubic0(float[] ys) {
			this.ys = ys;
		}
		
		public static float y(float x, float y0, float y1) {
			return y0  + (y1 - y0) * (-2 * x + 3) * x * x;
		}

		public float y(float x) {
			if(x < 0) {
				return ys[0];
			} else if(x >= 1) {
				return ys[ys.length - 1];
			} else {
				x *= ys.length - 1;
				
				int index = (int) x;
				x -= index;

				return y(x, ys[index], ys[index + 1]);
			}
		}
	}

	public static class Cubic implements Spline {
		
		private boolean cyclic;
		
		private float[] ys;
		private float[] ms;
		
		public Cubic(float[] ys, boolean cyclic) {
			this.cyclic = cyclic;
			
			int n = ys.length + (cyclic ? 1 : 0); // number of points on spline
			
			// Set ys
			this.ys = new float[n];
			
			for(int i = 0; i < ys.length; i++) {
				this.ys[i] = ys[i];
			}
			
			if(cyclic) this.ys[n - 1] = ys[0];
			
			// Get slopes
			this.ms = new float[n];
			
			for(int i = 1; i < n - 1; i++) {
				ms[i] = slope(this.ys[i - 1], this.ys[i], this.ys[i + 1]);
			}
			
			if(cyclic) {
				ms[0] = slope(this.ys[n - 1], this.ys[0], this.ys[1]);
				ms[n - 1] = slope(this.ys[n - 2], this.ys[n - 1], this.ys[0]);
			} else {
				ms[0] = ms[n - 1] = 0;
			}
		}
		
		public static float slope(float y0, float y1, float y2) {
			float d0 = y1 - y0;
			float d1 = y2 - y1;
			
			float q0 = (float) Math.sqrt(d0 * d0 + 1);
			float q1 = (float) Math.sqrt(d1 * d1 + 1);
			
			return (d0 * q1 + d1 * q0) / (q0 + q1);
		}
		
		public static float y(float x, float y0, float y1, float m0, float m1) {
			float a = (2 * y0 + m0 - 2 * y1 + m1);
			float b = (-3 * y0 - 2 * m0 + 3 * y1 - m1);
			float c = m0;
			float d = y0;
			
			return ((a * x + b) * x + c) * x + d;
		}
		
		public static float yNoSlope(float x, float y0, float y1, float y2, float y3) {
			return y(x, y1, y2, slope(y0, y1, y2), slope(y1, y2, y3));
		}
		
		public static float solveCubic(float a, float b, float c, float d) {
			//if(a == 0) return solveSquare(b, c, d);
			
			b = b / a;
			c = c / a;
			d = d / a;
			
			a = b;
			b = c;
			c = d;
			
			// and drop d
			
			// TODO!!! solve cubic eqs
			float a_over_3 = a / 3f;
			float Q = (3*b - a*a) / 9f;
			float Q_CUBE = Q*Q*Q;
			float R = (9*a*b - 27*c - 2*a*a*a) / 54f;
			float R_SQR = R*R;
			float D = Q_CUBE + R_SQR;

			if(D < 0.0) {
				// need cos, 3 real solutions.
				float theta = (float) Math.acos(R / Math.sqrt (-Q_CUBE));
				float SQRT_Q = (float) Math.sqrt(-Q);
				
				return 2.f * SQRT_Q * (float) Math.cos (theta/3.) - a_over_3;
			} else /* (D >= 0.0) */ {
				// one (or three if D == 0) real solution
				float SQRT_D = (float) Math.sqrt(D);
				float S = (float) Math.cbrt(R + SQRT_D);
				float T = (float) Math.cbrt(R - SQRT_D);
				
				return (S + T) - a_over_3;
			}
        }
		
		public static float solveSquare(float a, float b, float c) {
			if(a == 0) return solveLin(b, c);
			return Float.NaN; // TODO?
		}
		
		public static float solveLin(float a, float b) {
			return -b / a;
		}
		
		public static float x(float y, float y0, float y1, float m0, float m1) {
			float a = (2 * y0 + m0 - 2 * y1 + m1);
			float b = (-3 * y0 - 2 * m0 + 3 * y1 - m1);
			float c = m0;
			float d = y0 - y;
			
			return solveCubic(a, b, c, d);
		}

		public static float xNoSlope(float y, float y0, float y1, float y2, float y3) {
			return x(y, y1, y2, slope(y0, y1, y2), slope(y1, y2, y3));
		}

		public float y(float x) {
			if(cyclic) x = x - (float) Math.floor(x);
			
			if(x < 0) {
				return ys[0];
			} else if(x >= 1) {
				return ys[ys.length - 1];
			} else {
				x *= ys.length - 1;
				
				int index = (int) x;
				x -= index;
				
				return y(x, ys[index], ys[index + 1], ms[index], ms[index + 1]);
			}
		}
	}
}
