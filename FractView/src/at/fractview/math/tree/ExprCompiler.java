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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;
import at.fractview.math.Cplx;

/** This class analyzes an expression that has been successfully parsed.
 * 
 * It creates a list of the number of required initialization values for z,
 * all constants and parameters. As an intermediate result, an Instruction Tree is created
 * (this is not necessary but it makes debugging easier), which then is serialized into 
 * a list of instructions.
 */
public class ExprCompiler {

	// private static final String TAG = "ExprCompiler";
	
	public static final Collection<Var> predefinedVars = Arrays.asList(
			new Var[]{new Var("c"), new Var("n"), new Var("cr"), new Var("ci"), new Var("z"), new Var("zr"), new Var("zi")}
	);
	
	/** Compiles this expression.
	 * Adds parameters and constants to list (if necessary). Indices for parameters and constants in
	 * the list of instructions that is returned are based on their position in the constant and 
	 * parameters-List. Therefore, if multiple parameters should be shared, one should reuse old
	 * parameters-Lists.
	 * @param expr
	 * @param constants
	 * @param parameters
	 * @return the last instruction put onto the list
	 */
	public static List<Integer> generateInstructionList(Expr expr, List<Cplx> constants, List<String> parameters) {
		InstructionTree tree = compile(expr, constants, parameters);
		
		List<Integer> instructionList = new LinkedList<Integer>();
		tree.generateInstructionList(instructionList);

		return instructionList;
	}
	
	private static InstructionTree compile(Expr expr, List<Cplx> constants, List<String> parameters) {
		if(expr instanceof Var) {
			return parameter((Var) expr, constants, parameters);
		}

		if(expr instanceof Num) {
			return constant((Num) expr, constants, parameters);
		}

		if(expr instanceof App) {
			return app((App) expr, constants, parameters);
		}
		
		if(expr instanceof Indexed.ZN) {
			return new InstructionTree(Executable.ATOM_Z_LAST, true, ((Indexed.ZN) expr).index());
		}
		
		throw new IllegalArgumentException("Unsupported type");
	}

	private static InstructionTree app(App app, List<Cplx> constants, List<String> parameters) {
		InstructionTree[] args = new InstructionTree[app.op().arity()];
		
		for(int i = 0; i < app.op().arity(); i++) {
			args[i] = compile(app.get(i), constants, parameters);
		}

		switch(app.op()) {
		case ADD: return new InstructionTree(Executable.BINARY_FLAG | Executable.BIN_ADD, false, 0, args);
		case SUB: return new InstructionTree(Executable.BINARY_FLAG | Executable.BIN_SUB, false, 0, args);
		case MUL: return new InstructionTree(Executable.BINARY_FLAG | Executable.BIN_MUL, false, 0, args);
		case DIV: return new InstructionTree(Executable.BINARY_FLAG | Executable.BIN_DIV, false, 0, args);
		case POW: {
			if(args[1].opCode == Executable.ATOM_INT) {
				// If we can encode an integer, we can use POW_INT
				return new InstructionTree(Executable.UN_POW_INT, true, args[1].n, args[0]);
			} else {
				return new InstructionTree(Executable.BINARY_FLAG | Executable.BIN_POW, false, 0, args);
			}
		}
		case ABS: return new InstructionTree(Executable.UN_ABS, false, 0, args);
		case ARG: return new InstructionTree(Executable.UN_ARG, false, 0, args);
		case ATAN: return new InstructionTree(Executable.UN_ATAN, false, 0, args);
		case ATANH: return new InstructionTree(Executable.UN_ATANH, false, 0, args);
		case CONJ: return new InstructionTree(Executable.UN_CONJ, false, 0, args);
		case COS: return new InstructionTree(Executable.UN_COS, false, 0, args);
		case COSH: return new InstructionTree(Executable.UN_COSH, false, 0, args);
		case EXP: return new InstructionTree(Executable.UN_EXP, false, 0, args);
		case FLOOR: return new InstructionTree(Executable.UN_FLOOR, false, 0, args);
		case IM: return new InstructionTree(Executable.UN_IM, false, 0, args);
		case REC: return new InstructionTree(Executable.UN_REC, false, 0, args);
		case DREC: return new InstructionTree(Executable.UN_DREC, false, 0, args);
		case LOG: return new InstructionTree(Executable.UN_LOG, false, 0, args);
		case NEG: return new InstructionTree(Executable.UN_NEG, false, 0, args);
		case RE: return new InstructionTree(Executable.UN_RE, false, 0, args);
		case SIN: return new InstructionTree(Executable.UN_SIN, false, 0, args);
		case SINH: return new InstructionTree(Executable.UN_SINH, false, 0, args);
		case SQR: return new InstructionTree(Executable.UN_SQR, false, 0, args);
		case SQRT: return new InstructionTree(Executable.UN_SQRT, false, 0, args);
		case SREC: return new InstructionTree(Executable.UN_SREC, false, 0, args);
		case TAN: return new InstructionTree(Executable.UN_TAN, false, 0, args);
		case TANH: return new InstructionTree(Executable.UN_TANH, false, 0, args);
		default: throw new IllegalArgumentException("Found function " + app.op() + 
				" but it is not implemented. Please file a bug!");
		}
	}

	private static InstructionTree constant(Num num, List<Cplx> constants, List<String> parameters) {
		if(num.isInt()) {		
			// If int/short, encode
			int value = num.intValue();

			if(value == (short) value) {
				return new InstructionTree(Executable.ATOM_INT, true, (short) value);
			}
		}
		
		// Otherwise store as constant
		int index = constants.indexOf(num.value(null));
		
		if(index < 0) {
			index = constants.size();
			constants.add(num.value(null));
		}

		return new InstructionTree(Executable.ATOM_CONST, true, index);
	}

	private static InstructionTree parameter(Var v, List<Cplx> constants, List<String> parameters) {
		if(v.is("c")) {
			return new InstructionTree(Executable.ATOM_C, false, 0);
		} else if(v.is("z") || v.is("zn")) {
			return new InstructionTree(Executable.ATOM_Z, false, 0);
		} else if(v.is("cr")) {
			return new InstructionTree(Executable.ATOM_CR, false, 0);
		} else if(v.is("ci")) {
			return new InstructionTree(Executable.ATOM_CI, false, 0);
		} else if(v.is("zr")) {
			return new InstructionTree(Executable.ATOM_ZR, false, 0);
		} else if(v.is("zi")) {
			return new InstructionTree(Executable.ATOM_ZI, false, 0);
		} else if(v.is("n")) {
			return new InstructionTree(Executable.ATOM_N, false, 0);
		} else {
			// It is a new parameter
			int index = 0;
			
			// Find index (case insensitive)
			for(String p : parameters) {
				if(v.is(p)) {
					break;
				}
				
				index ++;
			}
			
			if(index == parameters.size()) {
				parameters.add(v.id());
				// index is already the right value
			}
			
			return new InstructionTree(Executable.ATOM_VAR, true, index);
		}
	}
	
	
	private static class InstructionTree {
		// We do not create a list of instructions immediately since we can merge 
		// instructions into single instructions. This is done in the instructionList-method
		
		int opCode;
		boolean usesN;
		int n;
		
		InstructionTree[] sub;
		
		InstructionTree(int opCode, boolean usesN, int n, InstructionTree...sub) {
			this.opCode = opCode;
			this.usesN = usesN;
			this.n = n;
			
			this.sub = sub;
		}
		
		boolean isAtom() {
			return sub.length == 0; 
		}
		
		void generateInstructionList(List<Integer> instructions) {
			int merge = -1;

			for(int i = 0; i < sub.length; i++) {
				if(merge < 0 && sub[i].isAtom() && !(sub[i].usesN && this.usesN)) {
					// We can encode argument i
					merge = i;
				} else {
					// Add argument to instruction list
					sub[i].generateInstructionList(instructions);
				}
			}
			
			int instruction = opCode | n << 16;
			
			if(merge >= 0) {
				instruction |= sub[merge].opCode | sub[merge].n << 16;
				
				if(merge == 0 && sub.length == 2) {
					instruction |= Executable.SWAP_FLAG;
				}
			}
			
			instructions.add(instruction);
		}
		
		public String toString() {
			String s = "[" + Integer.toHexString(opCode) + ", " + usesN + ":" + n + "] ( ";
			
			for(InstructionTree child : sub) {
				s += child.toString() + " ";
			}
			
			s += ")";
			
			return s;
		}
	}
}