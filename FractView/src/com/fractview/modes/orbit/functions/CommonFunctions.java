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
package com.fractview.modes.orbit.functions;

import java.util.LinkedList;

import com.fractview.math.Cplx;
import com.fractview.math.tree.Expr;
import com.fractview.math.tree.Op;
import com.fractview.math.tree.Var;

public class CommonFunctions {
	/*public static final class Mandelbrot extends ParameterizedFn {
		public Mandelbrot() {
			super(new LinkedList<String>());
		}

		@Override
		public int init(Cplx[] orbit, Cplx c) {
			orbit[0].set(0);
			return 1;
		}

		@Override
		public void step(Cplx[] zs, int n, Cplx c) {
			zs[n+1].add(zs[n+1].sqr(zs[n]), c);
		}
	}

	public static final class BurningMandelbrot extends ParameterizedFn {
		
		// 0, 0 = mandelbrot
		// 0, 1 = tricorn
		// 0, 2 = dino
		// 0, 3 = 0, 2
		// 0, 4 = 0, 0
		// 0, 5 = bee
		// 0, 6 = hedgehog
		// 0, 7 = 0, 6
		// 1, 0 = 0, 1
		// 1, 1 = 0, 0
		// 1, 2 = 0, 2
		// 1, 5 = pagoda
		// 1, 6 = crane
		// 2, 0 = 0, 5
		// 2, 1 = 1, 5
		// 2, 2 = burning ship
		// 2, 5 = 0, 0
		// 2, 6 = 0, 6
		// 3, 0 = 1, 5
		// 3, 1 = 0, 5
		// 3, 2 = 2, 2
		// 3, 5 = 0, 1
		// 3, 6 = 1, 6
		// 4, 0 = 0, 0
		// 5, 0 = 0, 2
		// 5, 1 = 0, 2
		// 5, 2 = 0, 0
		// 5, 3 = 0, 1
		// 5, 5 = 2, 2
		// 5, 6 = burning mandelbrot
		// 5, 7 = kiwi
		// 6, 0 = 0, 6
		// 6, 1 = 1, 6
		// 6, 2 = 5, 6
		// 6, 3 = 5, 7
		// 6, 5 = 0, 6
		// 6, 6 = 0, 0
		// 6, 7 = 0, 5
		// 7, 0 = 5, 6
		// 7, 1 = 5, 7
		// 7, 2 = 0, 6
		// 7, 3 = 1, 6
		// 7, 5 = 5, 7
		// 7, 6 = 0, 3
		// 7, 7 = 2, 2
		
		public enum Type { Mandelbrot, Tricorn, Dino, Bee, Hedgehog, Pagoda, BurningShip, Melting, Kiwi };
		
		private Type type;
		
		public BurningMandelbrot(Type type) {
			super(new LinkedList<String>());
			this.type = type;
		}

		@Override
		public int init(Cplx[] orbit, Cplx c) {
			orbit[0].set(0);
			return 1;
		}

		@Override
		public void step(Cplx[] zs, int n, Cplx c) {
			double zr = zs[n].re();
			double zi = zs[n].im();

			switch(type) {
			case Tricorn: zi = -zi; break;
			case Dino: zi = Math.abs(zi); break;
			case Bee: zr = Math.abs(zr); break;
			case Hedgehog: if(zr < 0) zi = Math.abs(zi); break;
			case Pagoda: zr = -Math.abs(zr); break;
			case BurningShip: zr = Math.abs(zr); zi = Math.abs(zi); break;
			case Kiwi: if(zi < 0) zr = Math.abs(zr); zi = -Math.abs(zi); break;
			case Melting: if(zi < 0) zr = -Math.abs(zr); break;
			case Mandelbrot: break;
			}

			zs[n+1].set(zr, zi);
			zs[n+1].add(zs[n+1].sqr(zs[n]), c);
		}
	}
	
	public static final class Cczcpaczcp extends ParameterizedFn {
		
		private Expr z0;
		
		public Cczcpaczcp() {
			super("alpha", "beta", "gamma", "delta");

			Var alpha = new Var("alpha");
			Var beta = new Var("beta");
			Var gamma = new Var("gamma");
			Var delta = new Var("delta");
			
			Expr exp = Op.REC.app(Op.SUB.app(beta, delta));
			Expr quot = Op.NEG.app(Op.DIV.app(Op.MUL.app(gamma, delta), Op.MUL.app(alpha, beta)));
			
			// (- gamma delta / alpha beta) ^ inv(beta - delta)
			this.z0 = Op.POW.app(quot, exp);
		}

		@Override
		public int init(Cplx[] orbit, Cplx c) {
			// TODO: How to get it into this one? If it's immutable, it's easy.
			orbit[0].set(z0.eval(null, null));
			return 1;
		}

		@Override
		public void step(Cplx[] zs, int n, Cplx c) {
			zs[n+1].add(zs[n+1].sqr(zs[n]), c);
		}
	}*/
}
