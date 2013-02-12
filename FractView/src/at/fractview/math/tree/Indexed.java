package at.fractview.math.tree;

import java.util.Map;
import java.util.Set;

import at.fractview.math.Cplx;

public abstract class Indexed extends Expr {
	private int index;
	
	public Indexed(int index) {
		this.index = index;
	}
	
	public int index() {
		return index;
	}

	@Override
	public boolean isNum() {
		return false;
	}

	@Override
	public boolean isNum(double re, double im) {
		return false;
	}

	@Override
	public boolean isApp() {
		return false;
	}

	@Override
	public boolean isApp(Op op) {
		return false;
	}

	@Override
	public Expr get(int i) {
		throw new IllegalArgumentException("No argument");
	}

	@Override
	public Cplx eval(Map<Var, Cplx> values) {
		// Cannot eval this
		return null;
	}

	@Override
	public Set<Var> parameters(Set<Var> vars) {
		return vars;
	}

	@Override
	public Expr diffZ() {
		// TODO Should we treat this like a constant?
		return null;
	}

	@Override
	public boolean containsZ() {
		return false;
	}

	public static class ZN extends Indexed {
		
		public ZN(int index) {
			super(index);
		}

		public int maxIndexZ() {
			return index() + 1;
		}
		

		@Override
		public int compareTo(Expr expr) {
			if(expr instanceof ZN) {
				ZN that = (ZN) expr;
				return index() < that.index() ? -1 : index() == that.index() ? 0 : 1;
			}
			
			return 1;
		}

		@Override
		public int hashCode() {
			return index() + 97;
		}
		
		public String toString() {
			return "z(n - " + index() + ")";
		}

	}

}
