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

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.fractview.math.Cplx;

/** Call me crazy but I think figuring out how I can use ANTLR here
 * might have taken more time than implementing this parser.
 * And I managed to add some gimmicks (like allowing expresions like
 * 2z sin z that would be very difficult to write as a formal grammar)
 */
public class Parser { 
	
	public static Parser parse(CharSequence s) {
		return new Parser(s);
	}
	
	// No sgn because otherwise 3 +3 is interpreted as 3 * 3.
	private final Pattern doublePattern = Pattern.compile("-?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
	private static final char EOS = (char) -1; // returned by ch() to indicate that the string is finished
	
	private CharSequence s; // String to be parsed
	private int i; // current parsing position
	
	private List<ErrorMsg> errors;
	private Expr expr;
	
	private Parser(CharSequence s) {
		this.s = s;
		i = 0;
		
		skipWhite();
		
		this.expr = null;
		errors = new LinkedList<ErrorMsg>();
	}
	
	public Expr get() {
		if(expr == null) {
			expr = sum();

			if(ch() != EOS) {
				reportError("Parts of the string were not matched.");
			}

			if(expr == null) {
				reportError("Expression was null, using 0");
				expr = new Num(0);				
			}
		}
		
		return expr;
	}
	
	public List<ErrorMsg> errors() {
		return errors;
	}
	
	void reportError(String msg) {
		errors.add(new ErrorMsg(msg));
	}
	
	private void skipWhite() {
		while(i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
	}		
	
	private boolean incr() {
		i++;
		skipWhite();
		return i < s.length();
	}
	
	private char ch() {
		return i < s.length() ? s.charAt(i) : EOS;
	}
	
	private Expr sum() {
		// sum = product ( '+'|'-' product )*
		Expr sum = product();
		
		if(sum == null) return null;
		
		while(ch() == '+' || ch() == '-') {
			Op op = ch() == '+' ? Op.ADD : Op.SUB;
			incr(); // advance
			
			Expr addend = product();
			
			if(addend == null) {
				reportError("Expected addend is null, ignoring");
			} else {
				sum = op.app(sum, addend);
			}
		}

		return sum;
	}
	
	private Expr product() {
		// product = power ('*'|'/' power)*
		Expr product = power();
		
		if(product == null) return null;
		
		while(ch() == '*' || ch() == '/') {
			Op op = ch() == '*' ? Op.MUL : Op.DIV;
			incr(); // advance
			
			Expr factor = power();

			if(factor == null) {
				reportError("Expected factor is null, ignoring");
			} else {
				product = op.app(product, factor);
			}
		}
		
		return product;
	}
	
	private Expr power() {
		// power = derived ('^' power)?
		Expr base = derived();
		
		if(base == null) return null;
		
		if(ch() == '^') {
			incr(); // skip '^'
			
			// This one is right associative (a ^ b ^ c = a ^ (b ^ c))
			Expr exp = power();
			
			if(exp == null) {
				reportError("Expected exponent is null, ignoring");
			} else {
				base = Op.POW.app(base, exp);
			}
		}
		
		return base;
	}
	
	private Expr derived() {
		// term (')*
		Expr term = term(true);
		
		// Now for trailing ' for derivations
		while(term != null && ch() == '\'') {
			incr();
			term = term.diffZ();
				
			if(term == null) {
				reportError("Cannot calculate derivate of expression");
			}
		}
		
		return term;
	}
	
	/**
	 * @return null, if this member is not a valid term
	 */
	private Expr term(boolean mayBeNeg) {
		//term = combination of number, id and (..)		
		Expr e = number(mayBeNeg); // numbers handle '-' different because -3,2 should be -3 + 2i.
		
		if(e == null) {
			boolean neg = false;
			
			if(mayBeNeg && ch() == '-') {
				neg = true; // we got a leading -, so negate expression.
				incr(); // skip '-'
			}
			
			// Two possibilities:
			e = enclosed(); // Expressions like '( blabla )'
			if(e == null) e = app(); // or 'sin x' // in this case the recursion below is never called

			if(e == null) {
				// No expression?
				if(neg) {
					// but a -?? 
					reportError("Had a '-' (neg) but no expression following it");
				}
				
				return null;
			} else {
				if(neg) e = Op.NEG.app(e);
			}
		}
		
		// Every term might be followed by other terms (but without leading '-' to
		// avoid confusion with SUB)
		Expr e2 = term(false);
				
		return e2 == null ? e : Op.MUL.app(e, e2);
	}
	
	private Expr app() {
		// id ( '(' ... ')' )?
		// or also id term (which then is an unary function.
		// In order to distinguish (..,..) from (sum) we first match (..,..) which
		// gives a valid list and therefore the same result as (sum)
		String id = id();
		
		if(id == null) {
			return null;
		} else {
			if(ch() == '[') {
				// z[index]
				incr();
				
				// TODO index(); // TODO
				
				if(ch() == ']') {
					incr();
				} else {
					reportError("Missing ]");
				}
				
				// could be a lot of things
				// Ideas:
				// sum[2..5]
				// What is not possible: nested sums where you need to access the run-var inside.
				return null;
			} else if(ch() == '(') { // ( e1 ; e2 ; ... ; en )
				// ; because explain to me, why should horner 2,3 be equal to horner((2,3)) but different from horner(2,3)?
				
				incr();
				
				List<Expr> l = args();
				
				if(ch() != ')') {
					reportError("Missing )");
				} else {
					incr();
				}
				
				return Expr.create(this, id, l);
			} else {
				Expr arg = term(false); // is it an id followed by an argument? sin -x not allowed. sry.

				if(arg == null) {
					return Expr.create(id);
				} else {
					return Expr.create(this, id, arg);
				}
			}
		}
	}
	
	private List<Expr> args() {
		// e1 ; e2 ; e3 etc...
		List<Expr> l = new LinkedList<Expr>();
		
		Expr e = sum();
		
		if(e == null) {
			reportError("missing argument in argument-list");
		} else {
			l.add(e);
		}
		
		while(ch() == ';') {
			incr(); // skip ';'
			
			e = sum();
			
			if(e == null) {
				reportError("missing argument in argument-list");
			} else {
				l.add(e);
			}
		}
		
		return l;
	}
	
	private Expr enclosed() {
		// (..) [ cannot use '|' because we can't distinguish opening and closing | ]
		char start = ch();
		
		if(start == '(') {
			incr(); // skip (
			
			Expr sum = sum();
			
			if(sum == null) {
				reportError("Empty inner expression");
			}

			if(start == '(' && ch() != ')') {
				reportError("Missing )");
			} else {
				incr();
			}
			
			return sum;
		} else {
			return null;
		}
	}
	
	private String id() {
		if(Character.isLetter(ch())) {
			int start = i;
			i++;
			
			while(Character.isLetterOrDigit(ch())) i++;
			
			CharSequence id = s.subSequence(start, i);
			
			skipWhite();
			
			return id.toString();
		} else {
			return null;
		}
	}
	
	private Expr number(boolean mayBeNeg) {
		Double re = real(mayBeNeg);
		
		if(re == null) return null;
		
		if(ch() == ',') {
			incr();
			// complex number I assume.
			Double im = real(true);
			
			if(im == null) {
				reportError("Imaginary part of number was empty");
				im = 0.;
			}
			
			return new Num(new Cplx(re, im));
		} else {
			return new Num(re);
		}
		
	}
	
	private Double real(boolean mayBeNeg) {
		// Should not use Scanner here because we want to allow expressions like 2z.
		if(ch() == '-' && !mayBeNeg) return null; // first character is a -, but this is not allowed.
		
		Matcher m = doublePattern.matcher(s.subSequence(i, s.length()));
		
		if(m.lookingAt()) {
			int length = m.end();
			double d = Double.parseDouble(m.group());
			
			i += length;
			skipWhite();
			
			return d;
		} else {
			return null;
		}
	}
	
	public class ErrorMsg {
		private String msg;
		private int pos;
		
		private ErrorMsg(String msg){
			this.msg = msg;
			this.pos = i;
		}
		
		public String toString() {
			return msg + ": " + s.subSequence(0, pos) + "_" + s.subSequence(pos, s.length());
		}
	}

	public static void main(String...args) {
		Scanner sc = new Scanner(System.in);
		
		while(sc.hasNextLine()) {
			String s = sc.nextLine();
			
			Parser parser = Parser.parse(s);

			Expr expr = parser.get();

			for(ErrorMsg e : parser.errors()) {
				System.out.println("Warning: " + e);
			}
			
			System.out.println("Expression is " + expr);
			
			//System.out.println("Compiled expression is " + Function.create(expr));
			
			//System.out.println("Found root: " + expr.findRoot(new Var("z"), new TreeMap<Var, Cplx>(), 1e-13, 100));
		}
	}
	
	public static class Log {
		public static void v(String tag, String msg) {
			System.out.println(tag + ": " + msg);
		}
	}
}