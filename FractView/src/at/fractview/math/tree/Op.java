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
package at.fractview.math.tree;

import at.fractview.math.Cplx;

public enum Op {
	ADD {
		@Override
		public Expr app(Expr...args) {
			if(args[0].isNum(0, 0)) {
				// 0 + r -> r
				return args[1];
			}
			
			int cmp0 = args[0].compareTo(args[1]);
			int cmp1 = args[1].compareTo(args[0]);
			
			if(cmp0 != -cmp1) {
				System.err.println("BUG: " + args[0] + " <> " + args[1] + ": " + cmp0 + " vs " + cmp1);
			}
			
			if(args[0].compareTo(args[1]) > 0) {
				// l + r -> r + l if r is smaller
				return ADD.app(args[1], args[0]);
			}
			
			if(args[0].isApp(ADD) && args[0].get(1).compareTo(args[1]) > 0) {
				// (args[0].0 + args[0].1) + r -> (args[0].0 + r) + args[0].1 if r < args[0].1
				return ADD.app(ADD.app(args[0].get(0), args[1]), args[0].get(1));
			}

			if(args[0].isApp(SUB)) {
				// (args[0].0 - args[0].1) + args[1] -> (args[0].0 + args[1]) - args[0].1
				return SUB.app(ADD.app(args[0].get(0), args[1]), args[0].get(1));
			}

			if(args[0].isApp(Op.NEG)) {
				// -l.0 + r -> r - args[0].0
				return SUB.app(args[1], args[0].get(0));
			}

			if(args[1].isApp(Op.NEG)) {
				// l + (-r.0) -> l - args[1].0
				return SUB.app(args[0], args[1].get(0));
			}
			
			return super.app(args);
		}
		
		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.add(args[0], args[1]);
		}

		@Override
		public Expr derive(String v, Expr...args) {
			// (a + b)' = a' + b'
			Expr dl = args[0].contains(v) ? args[0].derive(v) : new Num(0);
			Expr dr = args[1].contains(v) ? args[1].derive(v) : new Num(0);
			
			if(dl != null && dr != null) {
				return ADD.app(dl, dr);
			} else {
				return null;
			}
		}

		@Override
		public int arity() {
			return 2;
		}
	},
	SUB {
		@Override
		public Expr app(Expr...args) {
			if(args[0].isNum(0, 0)) {
				// 0 - r -> -r
				return Op.NEG.app(args[1]);
			}
			
			if(args[1].isNum() && !args[0].isNum()) {
				// l - num -> -num + l
				return ADD.app(Op.NEG.app(args[1]), args[0]);
			}
			
			if(args[0].isApp(SUB)) {
				// (args[0].0 - args[0].1) - r -> args[0].0 - (args[0].1 + r)
				return SUB.app(args[0].get(0), ADD.app(args[0].get(1), args[1]));
			}
			
			if(args[1].isApp(SUB)) {
				// l - (args[1].0 - args[1].1) -> (l + args[1].1) - args[1].0
				return SUB.app(ADD.app(args[0], args[1].get(1)), args[1].get(0));
			}
			
			if(args[0].isApp(Op.NEG)) {
				// (-l.0) - r -> -(l + r)
				return Op.NEG.app(ADD.app(args[0].get(0), args[1]));
			}

			if(args[1].isApp(Op.NEG)) {
				// l - (-r.0) -> l + args[1].0
				return ADD.app(args[0], args[1].get(0));
			}

			return super.app(args[0], args[1]);
		}
		
		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.sub(args[0], args[1]);
		}

		@Override
		public Expr derive(String v, Expr...args) {
			// (a - b)' = a' - b'
			Expr dl = args[0].contains(v) ? args[0].derive(v) : new Num(0);
			Expr dr = args[1].contains(v) ? args[1].derive(v) : new Num(0);
			
			if(dl != null && dr != null) {
				return SUB.app(dl, dr);
			} else {
				return null;
			}
		}

		@Override
		public int arity() {
			return 2;
		}	
	},
	MUL {		
		@Override
		public Expr app(Expr...args) {
			if(args[0].isNum(0, 0)) {
				// 0 * r -> 0
				return new Num(0);
			}
			
			if(args[0].isNum(1, 0)) {
				// 1 * r -> r
				return args[1];
			}

			if(args[0].compareTo(args[1]) > 0) {
				// l * r -> r * l if r is smaller
				return MUL.app(args[1], args[0]);
			}

			if(args[0].isApp(MUL) && args[0].get(1).compareTo(args[1]) > 0) {
				// (args[0].0 * args[0].1) * r -> (args[0].0 * r) * args[0].1 if r < args[0].1
				return MUL.app(MUL.app(args[0].get(0), args[1]), args[0].get(1));
			}

			if(args[0].isApp(DIV)) {
				// (args[0].0 / args[0].1) * args[1] -> (args[0].0 * args[1]) / args[0].1
				return DIV.app(MUL.app(args[0].get(0), args[1]), args[0].get(1));
			}
			
			/*if(args[0].isApp(DIV)) {
				// (args[0].0 / args[0].1) * r -> args[0].0 * r / args[0].1
				return DIV.app(MUL.app(args[0].get(0), args[1]), args[0].get(1));
			}

			if(args[1].isApp(MUL) && args[0].compareTo(args[1].get(0)) > 0) {
				// l * (args[1].0 * args[1].1) -> args[1].0 * (l + args[1].1) if args[1].0 < l
				return MUL.app(args[1].get(0), MUL.app(args[0], args[1].get(1)));
			}

			if(args[1].isApp(DIV)) {
				// l * (args[1].0 / args[1].1) -> (l * args[1].0) / args[1].1
				return DIV.app(MUL.app(args[0], args[1].get(0)), args[1].get(1));
			}*/

			if(args[0].isApp(Op.NEG)) {
				// -l.0 * r -> -(args[0].0 * r)
				return Op.NEG.app(MUL.app(args[0].get(0), args[1]));
			}

			if(args[1].isApp(Op.NEG)) {
				// l * -r.0 -> -(l * args[1].0)
				return Op.NEG.app(MUL.app(args[0], args[1].get(0)));
			}

			if(args[0].isApp(Op.REC)) {
				// inv(args[0].0) * r -> r / args[0].0
				return DIV.app(args[1], args[0].get(0));
			}

			if(args[1].isApp(Op.REC)) {
				// l * inv(args[1].0) -> l / args[1].0
				return DIV.app(args[0], args[1].get(0));
			}

			return super.app(args[0], args[1]);
		}
	
		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.mul(args[0], args[1]);
		}

		@Override
		public Expr derive(String v, Expr...args) {
			Expr dl = args[0].contains(v) ? args[0].derive(v) : new Num(0);
			Expr dr = args[1].contains(v) ? args[1].derive(v) : new Num(0);
			
			if(dl != null && dr != null) {
				// (a * b)' = a'*b + a*b'
				return ADD.app(MUL.app(dl, args[1]), MUL.app(args[0], dr));
			}
			
			return null;
		}

		@Override
		public int arity() {
			return 2;
		}

	},
	DIV {
		@Override
		public Expr app(Expr...args) {
			if(args[0].isNum(0, 0)) {
				// 0 / r -> 0
				return new Num(0);
			}
			
			if(args[0].isNum(1, 0)) {
				// 1 / r -> inv(args[1])
				return Op.REC.app(args[1]);
			} // the case l / 1 (-> 1 * l) is already handled in MUL
			
			if(args[1].isNum() && !args[0].isNum()) {
				// l / num -> inv(num) * l
				return MUL.app(Op.REC.app(args[1]), args[0]);
			}
			
			if(args[0].isApp(DIV)) {
				// (args[0].0 / args[0].1) / r -> args[0].0 / (args[0].1 * r)
				return DIV.app(args[0].get(0), MUL.app(args[0].get(1), args[1]));
			}
			
			if(args[1].isApp(DIV)) {
				// l / (args[1].0 / args[1].1) -> (l * args[1].1) / args[1].0
				return DIV.app(MUL.app(args[0], args[1].get(1)), args[1].get(0));
			}
			
			if(args[0].isApp(Op.NEG)) {
				// -l.0 / r -> -(args[0].0 / r)
				return Op.NEG.app(DIV.app(args[0].get(0), args[1]));
			}

			if(args[1].isApp(Op.NEG)) {
				// l / -r.0 -> -(l / args[1].0)
				return Op.NEG.app(DIV.app(args[0], args[1].get(0)));
			}

			if(args[0].isApp(Op.REC)) {
				// inv(args[0].0) / r -> inv(args[0].0 * r)
				return Op.REC.app(MUL.app(args[0].get(0), args[1]));
			}

			if(args[1].isApp(Op.REC)) {
				// l / inv(args[1].0) -> l * args[1].0
				return MUL.app(args[0], args[1].get(0));
			}

			return super.app(args[0], args[1]);
		}
		
		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.div(args[0], args[1]);
		}

		@Override
		public Expr derive(String v, Expr...args) {
			Expr dl = args[0].contains(v) ? args[0].derive(v) : new Num(0);
			Expr dr = args[1].contains(v) ? args[1].derive(v) : new Num(0);
			
			if(dl != null && dr != null) {
				// (a / b)' = a'*b - a*b' / sqr(b) = a' / b - a * b' / sqr(b)
				Expr sqr_r = Op.SQR.app(args[1]);
				
				Expr t2 = DIV.app(MUL.app(args[0], dr), sqr_r);
				
				return SUB.app(DIV.app(dl, args[1]), t2);
			}
			
			return null;
		}

		@Override
		public int arity() {
			return 2;
		}
	},
	POW {
		@Override
		public Expr app(Expr...args) {
			if(args[1].isNum(1, 0)) {
				// l ^ 1 -> l
				return args[0];
			}
			
			if(args[0].isNum(1, 0)) {
				// 1 ^ r -> 1
				return new Num(1);
			}
			
			if(args[1].isNum(-1, 0)) {
				// l ^ -1 -> inv(args[0])
				return Op.REC.app(args[0]);
			}

			if(args[1].isNum(2, 0)) {
				// l ^ 2 -> sqr(args[0])
				return Op.SQR.app(args[0]);
			}

			if(args[1].isNum(0.5, 0)) {
				// l ^ 0.5 -> sqrt(args[0])
				return Op.SQRT.app(args[0]);
			}

			if(args[0].isApp(POW)) {
				// (args[0].0 ^ args[0].1) ^ r -> args[0].0 ^ (args[0].1 * r)
				return POW.app(args[0].get(0), MUL.app(args[0].get(1), args[1]));
			}
			
			if(args[0].isApp(MUL) && args[0].get(0).isNum()) {
				// (num * args[0].1) ^ r -> num^r * args[0].1 ^ r
				return MUL.app(POW.app(args[0].get(0), args[1]), POW.app(args[0].get(1), args[1]));
			}

			if(args[0].isApp(DIV) && args[0].get(0).isNum()) {
				// (num / args[0].1) ^ r -> num^r / args[0].1 ^ r
				return DIV.app(POW.app(args[0].get(0), args[1]), POW.app(args[0].get(1), args[1]));
			}

			if(args[0].isApp(Op.REC)) {
				// inv(args[0].0) ^ r -> inv(args[0].0 ^ r)
				return Op.REC.app(POW.app(args[0].get(0), args[1]));
			}
			
			if(args[1].isApp(Op.NEG)) {
				// l ^ -r.0 -> inv(l ^ args[1].0)
				return Op.REC.app(POW.app(args[0], args[1].get(0)));
			}

			if(args[0].isApp(Op.EXP)) {
				// exp(args[0].0) ^ r -> exp(args[0].0 * r)
				return Op.EXP.app(MUL.app(args[0].get(0), args[1]));
			}
			
			return super.app(args[0], args[1]);
		}
		
		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.pow(args[0], args[1]);
		}

		@Override
		public Expr derive(String v, Expr...args) {
			if(args[1].contains(v)) {
				// (f ^ g)' = exp(log(f) * g)' = f ^ g * (log(f) * g)' = f ^ g * (g * f' / f + log(f) * g')
				Expr dl = args[0].contains(v) ? args[0].derive(v) : new Num(0);
				Expr dr = args[1].contains(v) ? args[1].derive(v) : new Num(0);
				
				if(dl != null && dr != null) {
					Expr fract = DIV.app(MUL.app(args[1], dl), args[0]);
					Expr log_l = Op.LOG.app(args[0]);
					
					Expr trail = ADD.app(fract, MUL.app(log_l, dr));
					
					return MUL.app(POW.app(args[0], args[1]), trail);
				}
				
				return null;
			} else {
				// (f ^ n)' = n * f ^ (n-1) * f'
				Expr dl = args[0].derive(v);
				
				if(dl != null) {
					Expr exp = SUB.app(args[1], new Num(1));
					
					Expr pow = MUL.app(args[1], POW.app(args[0], exp));
					
					return MUL.app(pow, dl);
				} else {
					return null;
				}		
			}
		}

		@Override
		public int arity() {
			return 2;
		}
	},
	NEG {
		@Override
		public Expr app(Expr...args) {
			if(args[0].isApp(NEG)) {
				// --a.0 -> a.0
				return args[0].get(0);
			}
			
			if(args[0].isApp(Op.SUB)) {
				// -(args[0].0 - a.1) -> a.1 - a.0
				return Op.SUB.app(args[0].get(1), args[0].get(0));
			}
			
			return super.app(args[0]);
		}
		
		@Override
		public Expr derive(String v, Expr...args) {
			Expr dt = args[0].derive(v);
			
			if(dt != null) {
				return NEG.app(dt);
			}
			
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.neg(args[0]);
		}

		@Override
		public int arity() {
			return 1;
		}

	},
	REC {
		@Override
		public Expr app(Expr...args) {
			if(args[0].isApp(NEG)) {
				// inv(-a.0) -> -inv(args[0].0)
				return NEG.app(REC.app(args[0].get(0)));
			}
			
			if(args[0].isApp(REC)) {
				// inv(inv(args[0].0)) -> a.0
				return args[0].get(0);
			}

			if(args[0].isApp(Op.DIV)) {
				// inv(args[0].0 / a.1) -> a.1 / a.0
				return Op.DIV.app(args[0].get(1), args[0].get(0));
			}
			
			return super.app(args[0]);
		}
		
		@Override
		public Expr derive(String v, Expr...args) {
			Expr dt = args[0].derive(v);
			
			if(dt != null) {
				// -dt / sqr(args[0])
				return Op.DIV.app(dt, Op.NEG.app(Op.SQR.app(args[0])));
			}
			
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.rec(args[0]);
		}

		@Override
		public int arity() {
			return 1;
		}

	},
	SREC {
		@Override
		public Expr derive(String v, Expr...args) {
			Expr dt = args[0].derive(v);
			
			if(dt != null) {
				return Op.MUL.app(Op.SUB.app(new Num(1), Op.SQR.app(args[0])));
			}
			
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.srec(args[0]);
		}

		@Override
		public int arity() {
			return 1;
		}

	},
	DREC {
		@Override
		public Expr derive(String v, Expr...args) {
			Expr dt = args[0].derive(v);
			
			if(dt != null) {
				return Op.MUL.app(Op.ADD.app(new Num(1), Op.SQR.app(args[0])));
			}
			
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.drec(args[0]);
		}

		@Override
		public int arity() {
			return 1;
		}

	},
	SQR {
		@Override
		public Expr app(Expr...args) {
			if(args[0].isApp(NEG)) {
				// sqr(-a.0) -> sqr(args[0].0)
				return SQR.app(args[0].get(0));
			}
			
			if(args[0].isApp(REC)) {
				// sqr(inv(args[0].0)) -> inv(sqr(args[0].0))
				return REC.app(SQR.app(args[0].get(0)));
			}

			return super.app(args[0]);
		}		

		@Override
		public Expr derive(String v, Expr...args) {
			Expr dt = args[0].derive(v);
			
			if(dt != null) {
				return Op.MUL.app(Op.MUL.app(new Num(2), args[0]), dt);
			}
			
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.sqr(args[0]);
		}

		@Override
		public int arity() {
			return 1;
		}
	},
	SQRT {
		@Override
		public Expr app(Expr...args) {
			if(args[0].isApp(REC)) {
				// sqrt(inv(args[0].0)) -> inv(sqrt(args[0].0))
				return REC.app(SQRT.app(args[0].get(0)));
			}

			return super.app(args[0]);
		}		

		@Override
		public Expr derive(String v, Expr...args) {
			Expr dt = args[0].derive(v);
			
			if(dt != null) {
				// sqrt(args[0])' = 0.5*da / sqrt(args[0])
				return Op.DIV.app(Op.MUL.app(new Num(0.5), dt), SQRT.app(args[0]));
			}
			
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.sqrt(args[0]);
		}

		@Override
		public int arity() {
			return 1;
		}

	},
	EXP {
		@Override
		public Expr app(Expr...args) {
			if(args[0].isApp(NEG)) {
				// exp(-a.0) -> inv(exp(args[0].0))
				return REC.app(EXP.app(args[0].get(0)));
			}

			return super.app(args[0]);
		}		

		@Override
		public Expr derive(String v, Expr...args) {
			Expr dt = args[0].derive(v);
			
			if(dt != null) {
				// exp(args[0])' = exp(args[0]) * da
				return Op.MUL.app(EXP.app(args[0]), dt);
			}
			
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.exp(args[0]);
		}

		@Override
		public int arity() {
			return 1;
		}

	},
	LOG {
		@Override
		public Expr app(Expr...args) {
			if(args[0].isApp(REC)) {
				// log(inv(args[0].0)) -> -log(args[0].0)
				return NEG.app(LOG.app(args[0].get(0)));
			}

			if(args[0].isApp(Op.POW)) {
				// log(args[0].0 ^ a.1) -> a.1 * log(args[0].0)
				return Op.POW.app(LOG.app(args[0].get(0)), args[0].get(1));
			}

			return super.app(args[0]);
		}		


		@Override
		public Expr derive(String v, Expr...args) {
			Expr dt = args[0].derive(v);
			
			if(dt != null) {
				// log(args[0])' = da / a
				return Op.DIV.app(dt, args[0]);
			}
			
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.log(args[0]);
		}


		@Override
		public int arity() {
			return 1;
		}
	},
	SIN {
		@Override
		public Expr app(Expr...args) {
			if(args[0].isApp(NEG)) {
				// sin(-a.0) -> -sin(args[0].0)
				return NEG.app(SIN.app(args[0].get(0)));
			}

			return super.app(args[0]);
		}
		
		@Override
		public Expr derive(String v, Expr...args) {
			Expr dt = args[0].derive(v);
			
			if(dt != null) {
				// sin(args[0])' = cos(args[0]) * da
				return Op.MUL.app(COS.app(args[0]), dt);
			}
			
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.sin(args[0]);
		}

		@Override
		public int arity() {
			return 1;
		}
	},
	COS {
		@Override
		public Expr app(Expr...args) {
			if(args[0].isApp(NEG)) {
				// cos(-a.0) -> cos(args[0].0)
				return COS.app(args[0].get(0));
			}

			return super.app(args[0]);
		}
		
		@Override
		public Expr derive(String v, Expr...args) {
			Expr dt = args[0].derive(v);
			
			if(dt != null) {
				// cos(args[0])' = -sin(args[0]) * da
				return Op.NEG.app(Op.MUL.app(SIN.app(args[0]), dt));
			}
			
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.cos(args[0]);
		}

		@Override
		public int arity() {
			return 1;
		}
	},
	TAN {
		@Override
		public Expr app(Expr...args) {
			if(args[0].isApp(NEG)) {
				// tan(-a.0) -> -tan(args[0].0)
				return NEG.app(TAN.app(args[0].get(0)));
			}

			return super.app(args[0]);
		}		
		
		@Override
		public Expr derive(String v, Expr...args) {
			Expr dt = args[0].derive(v);
			
			if(dt != null) {
				// tan(args[0])' = da / sqr cos args[0]
				return Op.DIV.app(dt, SQR.app(COS.app(args[0])));
			}
			
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.tan(args[0]);
		}

		@Override
		public int arity() {
			return 1;
		}
	},
	ATAN {
		@Override
		public Expr app(Expr...args) {
			if(args[0].isApp(NEG)) {
				// atan(-a.0) -> -atan(args[0].0)
				return NEG.app(ATAN.app(args[0].get(0)));
			}
			
			return super.app(args[0]);
		}		
		
		@Override
		public Expr derive(String v, Expr...args) {
			Expr dt = args[0].derive(v);
			
			if(dt != null) {
				// atan(args[0])' = da / (1 + sqr args[0])
				return Op.DIV.app(dt, Op.ADD.app(new Num(1), SQR.app(args[0])));
			}
			
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.atan(args[0]);
		}

		@Override
		public int arity() {
			return 1;
		}
	},
	SINH {
		@Override
		public Expr app(Expr...args) {
			if(args[0].isApp(NEG)) {
				// sinh(-a.0) -> -sinh(args[0].0)
				return NEG.app(SINH.app(args[0].get(0)));
			}

			return super.app(args[0]);
		}
		
		@Override
		public Expr derive(String v, Expr...args) {
			Expr dt = args[0].derive(v);
			
			if(dt != null) {
				// sinh(args[0])' = cosh(args[0]) * da
				return Op.MUL.app(COSH.app(args[0]), dt);
			}
			
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.sinh(args[0]);
		}

		@Override
		public int arity() {
			return 1;
		}


	},
	COSH {
		@Override
		public Expr app(Expr...args) {
			if(args[0].isApp(NEG)) {
				// cosh(-a.0) -> cosh(args[0].0)
				return COSH.app(args[0].get(0));
			}

			return super.app(args[0]);
		}		
		
		@Override
		public Expr derive(String v, Expr...args) {
			Expr dt = args[0].derive(v);
			
			if(dt != null) {
				// cosh(args[0])' = sinh(args[0]) * da
				return Op.MUL.app(SINH.app(args[0]), dt);
			}
			
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.cosh(args[0]);
		}

		@Override
		public int arity() {
			return 1;
		}
	},
	TANH {
		@Override
		public Expr app(Expr...args) {
			if(args[0].isApp(NEG)) {
				// tanh(-a.0) -> -tanh(args[0].0)
				return NEG.app(TANH.app(args[0].get(0)));
			}

			return super.app(args[0]);
		}		
		
		@Override
		public Expr derive(String v, Expr...args) {
			Expr dt = args[0].derive(v);
			
			if(dt != null) {
				// tanh(args[0])' = da / sqr cosh(args[0])
				return Op.DIV.app(dt, SQR.app(COSH.app(args[0])));
			}
			
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.tanh(args[0]);
		}

		@Override
		public int arity() {
			return 1;
		}
	},
	ATANH {
		@Override
		public Expr app(Expr...args) {
			// TODO
			return super.app(args[0]);
		}	
		
		@Override
		public Expr derive(String v, Expr...args) {
			Expr dt = args[0].derive(v);
			
			if(dt != null) {
				// atanh(args[0])' = da / (1 - sqr args[0])
				return Op.DIV.app(dt, Op.SUB.app(new Num(1), SQR.app(args[0])));
			}
			
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.atanh(args[0]);
		}

		@Override
		public int arity() {
			return 1;
		}
	},
	CONJ {
		// No optimizations since not a derivation (users can easily simply themselves)
		@Override
		public Expr derive(String v, Expr...args) {
			// No derivative
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.conj(args[0]);
		}

		@Override
		public int arity() {
			return 1;
		}
	}, 
	ABS {
		@Override
		public Expr derive(String v, Expr...args) {
			// No derivative
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.set(args[0].abs(), 0.);
		}

		@Override
		public int arity() {
			return 1;
		}
	}, 
	ARG {
		@Override
		public Expr derive(String v, Expr...args) {
			// No derivative
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.set(args[0].arg(), 0.);
		}

		@Override
		public int arity() {
			return 1;
		}

	}, 
	RE {
		@Override
		public Expr derive(String v, Expr...args) {
			// No derivative
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.set(args[0].re(), 0.);
		}

		@Override
		public int arity() {
			return 1;
		}
	}, 
	IM {
		@Override
		public Expr derive(String v, Expr...args) {
			// No derivative
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.set(args[0].im(), 0.);
		}

		@Override
		public int arity() {
			return 1;
		}
	},
	CABS {
		@Override
		public Expr derive(String v, Expr...args) {
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.set(Math.abs(args[0].re()), Math.abs(args[0].re()));
		}

		@Override
		public int arity() {
			return 1;
		}
	},
	POLAR {
		@Override
		public Expr derive(String v, Expr...args) {
			return null;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.set(args[0].abs(), args[0].arg());
		}

		@Override
		public int arity() {
			return 1;
		}
	},
	FLOOR {
		@Override
		public Expr derive(String v, Expr...args) {
			// This is flat
			return new Num(0);
		}

		@Override
		public Cplx eval(Cplx dest, Cplx...args) {
			return dest.set(Math.floor(args[0].re()), Math.floor(args[0].re()));
		}

		@Override
		public int arity() {
			return 1;
		}
	},
	// Now for constants:
	PI {
		@Override
		public int arity() {
			return 0;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx... args) {
			return dest.set(Math.PI, 0);
		}

		@Override
		public Expr derive(String v, Expr... args) {
			throw new IllegalArgumentException("Not a valid op");
		}
	},
	E {
		@Override
		public int arity() {
			return 0;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx... args) {
			return dest.set(Math.E, 0);
		}

		@Override
		public Expr derive(String v, Expr... args) {
			throw new IllegalArgumentException("Not a valid op");
		}
	},
	I {
		@Override
		public int arity() {
			return 0;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx... args) {
			return dest.set(0, 1);
		}

		@Override
		public Expr derive(String v, Expr... args) {
			throw new IllegalArgumentException("Not a valid op");
		}
	},
	ZR {
		@Override
		public Expr app(Expr...args) {
			return Op.RE.app(new Var("z"));
		}

		@Override
		public int arity() {
			return 0;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx... args) {
			throw new IllegalArgumentException("Not a valid op");
		}

		@Override
		public Expr derive(String v, Expr... args) {
			throw new IllegalArgumentException("Not a valid op");
		}
	},
	ZI {
		@Override
		public Expr app(Expr...args) {
			return Op.IM.app(new Var("z"));
		}

		@Override
		public int arity() {
			return 0;
		}

		@Override
		public Cplx eval(Cplx dest, Cplx... args) {
			throw new IllegalArgumentException("Not a valid op");
		}

		@Override
		public Expr derive(String v, Expr... args) {
			throw new IllegalArgumentException("Not a valid op");
		}
	},
	// Now some special things:
	DERIVE {
		@Override
		public Expr app(Expr...args) {
			return args[0].derive("z");
		}

		@Override
		public Expr derive(String v, Expr...args) {
			throw new IllegalArgumentException("Not a valid op");
		}
		
		public Cplx eval(Cplx dest, Cplx...args) {
			throw new IllegalArgumentException("Not a valid op");
		}

		@Override
		public int arity() {
			return 1;
		}
	},
	NEWTON {
		@Override
		public Expr app(Expr...args) {
			Var z = new Var("z");
			Expr da = args[0].derive("z");
			
			if(da == null) return null;
			
			Expr fract = Op.DIV.app(args[0], da);			
			return Op.SUB.app(z, fract);
		}

		@Override
		public Expr derive(String v, Expr...args) {
			throw new IllegalArgumentException("Not a valid op");
		}
		
		public Cplx eval(Cplx dest, Cplx...args) {
			throw new IllegalArgumentException("Not a valid op");
		}

		@Override
		public int arity() {
			return 1;
		}
	},
	NOVA {
		@Override
		public Expr app(Expr...args) {
			Var z = new Var("z");
			
			Expr da = args[0].derive("z");

			if(da == null) return null;

			Expr fract = Op.DIV.app(args[0], da);
			
			Expr RFract = Op.MUL.app(args[1], fract);
			Expr newton = Op.SUB.app(z, RFract);
			
			return Op.ADD.app(newton, args[2]);
		}

		@Override
		public Expr derive(String v, Expr...args) {
			throw new IllegalArgumentException("Not a valid op");
		}
		
		public Cplx eval(Cplx dest, Cplx...args) {
			throw new IllegalArgumentException("Not a valid op");
		}

		@Override
		public int arity() {
			return 3;
		}
	},
	HORNER {
		@Override
		public Expr app(Expr...args) {
			Expr z = new Var("z");
			
			Expr result = new Num(1);

			for(Expr arg : args) {
				Expr pre = Op.MUL.app(result, z);
				result = Op.ADD.app(pre, arg);
			}
			
			return result;
		}

		@Override
		public Expr derive(String v, Expr...args) {
			throw new IllegalArgumentException("Not a valid op");
		}
		
		public Cplx eval(Cplx dest, Cplx...args) {
			throw new IllegalArgumentException("Not a valid op");
		}

		@Override
		public int arity() {
			// not preassigned
			return -1;
		}
	},
	POLY { // product[i](z-args[i])
		@Override
		public Expr app(Expr...args) {
			Expr z = new Var("z");
			Expr result = new Num(1);
						
			for(Expr arg : args) {
				Expr delta = Op.SUB.app(z, arg);
				result = Op.MUL.app(result, delta);
			}
			
			return result;
		}

		@Override
		public Expr derive(String v, Expr...args) {
			throw new IllegalArgumentException("Not a valid op");
		}
		
		public Cplx eval(Cplx dest, Cplx...args) {
			throw new IllegalArgumentException("Not a valid op");
		}

		@Override
		public int arity() {
			// not preassigned
			return -1;
		}
	},
	MANDEL { // argument^2+c
		@Override
		public Expr app(Expr...args) {
			return Op.ADD.app(Op.SQR.app(args[0]), new Var("c"));
		}

		@Override
		public Expr derive(String v, Expr...args) {
			throw new IllegalArgumentException("Not a valid op");
		}
		
		public Cplx eval(Cplx dest, Cplx...args) {
			throw new IllegalArgumentException("Not a valid op");
		}

		@Override
		public int arity() {
			return 1;
		}	
	}
	;
	public abstract int arity();
	
	public abstract Cplx eval(Cplx dest, Cplx...args);
	public abstract Expr derive(String v, Expr...args);	

	public Expr app(Expr...args) {
		for(Expr arg : args) {
			if(!arg.isNum()) {
				return new App(this, args);
			}
		}
		
		Cplx[] cArgs = new Cplx[args.length];
		
		for(int i = 0; i < args.length; i++) {
			// No parameters because all are numeric values
			cArgs[i] = args[i].eval(new Cplx(), null);
		}
		
		return new Num(eval(new Cplx(), cArgs));
	}	
}
