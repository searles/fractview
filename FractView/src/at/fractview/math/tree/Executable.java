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
import java.util.List;
import java.util.ListIterator;

import at.fractview.math.Cplx;

/**
 * This class contains an interpreter for instances of CompiledExpr. The reason why this
 * class is not combined with CompiledExpr is that we might share parameters amongst various 
 * Executables. Therefore, in this case we should first collect all parameters and constants
 * in all these CompiledExpr and then create corresponding executables.
 */
public class Executable {
	
	// An instruction is compiled of several masks
	
	// TODO: Other functions (proposals?)
	
	public static final int ATOM_MASK = 0x0000ff00;
	
	public static final int NO_ATOM =   0x000000000;
	
	public static final int ATOM_C =    0x00000100;
	public static final int ATOM_Z =    0x00000200;
	public static final int ATOM_N =    0x00000300;
	public static final int ATOM_CR =    0x00000400;
	public static final int ATOM_CI =    0x00000500;
	public static final int ATOM_ZR =    0x00000600;
	public static final int ATOM_ZI =    0x00000700;

	// the following require the n-field unsigned
	public static final int ATOM_Z_LAST =  0x00000800;
	public static final int ATOM_CONST =   0x00000900;
	public static final int ATOM_VAR =     0x00000A00;

	// the following require the n-field signed
	public static final int ATOM_INT =	   0x00000B00;

	// there's space for 255 in total!
	// Check whether >> 8 has any speed up
	
	// Now comes the functions
	// They occupy the first bits so that the switch-statement is faster.
	public static final int OP_MASK = 0x0000003f;
	
	// identity-function - do nothing
	public static final int UN_ID = 0x00000000;
	
	// Now the real stuff
	public static final int UN_NEG = 0x00000001;
	public static final int UN_INV = 0x00000002;
	public static final int UN_SQR = 0x00000003;
	public static final int UN_SQRT = 0x00000004;

	// more real
	public static final int UN_EXP = 0x00000005;
	public static final int UN_LOG = 0x00000006;

	// trigonometric
	public static final int UN_SIN = 0x00000007;
	public static final int UN_COS = 0x00000008;
	public static final int UN_TAN = 0x00000009;
	public static final int UN_ATAN = 0x0000000A;
	public static final int UN_SINH = 0x0000000B;
	public static final int UN_COSH = 0x0000000C;
	public static final int UN_TANH = 0x0000000D;
	public static final int UN_ATANH = 0x0000000E;

	public static final int UN_RE = 0x0000000F;
	public static final int UN_IM = 0x00000010;
	public static final int UN_CONJ = 0x00000011;
	public static final int UN_ABS = 0x00000012;
	public static final int UN_ARG = 0x00000013;

	public static final int UN_FLOOR = 0x00000014;

	// Things that require n signed
	public static final int UN_POW_INT = 0x00000015;
	
	// Now binary functions
	public static final int BINARY_FLAG = 0x00000080;
	public static final int SWAP_FLAG = 0x00000040;
	
	public static final int BIN_ADD = 0x00000000;
	public static final int BIN_SUB = 0x00000001;
	public static final int BIN_MUL = 0x00000002;
	public static final int BIN_DIV = 0x00000003;
	public static final int BIN_POW = 0x00000004;
	
	private class Node {
		int instruction;
		Node next;
		
		Node(int instruction, Node next) {
			this.instruction = instruction;
			this.next = next;
		}
	}
	
	private Cplx[] constants;

	private Node head; // using a linked list seems to be faster than using an array...
	
	/** Creates a new Executable. Arrays here might not be copied. If they are modified (which
	 * is strongly discouraged because they might actually be shared amongst several
	 * instances of this class) the result is unexpected. 
	 * @param instructions
	 * @param constants
	 */
	public Executable(List<Integer> instructions, Cplx[] constants) {
		head = null;
		
		// Reverse list
		ListIterator<Integer> li = instructions.listIterator(instructions.size());
		
		while(li.hasPrevious()) {
			head = new Node(li.previous(), head);
		}
		
		this.constants = constants;
	}
	
	/**
	 * @param c
	 * @param zs
	 * @param n
	 * @param parameters Values of all parameters. The ith complex number here corresponds to the
	 * ith parameter in the constructor
	 */
	public void execute(Cplx c, Cplx[] zs, int n, Cplx[] parameters, Cplx dest) {
		Node node = execute(head, c, zs, n, parameters, dest);
		
		if(node != null) {
			throw new IllegalArgumentException(
					"Some instructions were ignored..." + this + ", " + Integer.toHexString(node.instruction));
		}
	}
	
	private Node execute(Node pc, Cplx c, Cplx[] zs, int n, Cplx[] parameters, Cplx dest) {
		/* interpret "functions" with 0 arguments */
		switch(pc.instruction & ATOM_MASK) {
		case ATOM_C:
			dest.set(c);
			break;
		case ATOM_Z:
			dest.set(zs[n]);
			break;
		case ATOM_N:
			dest.set(n, 0);
			break;
		case ATOM_CR:
			dest.set(c.re(), 0);
			break;
		case ATOM_CI:
			dest.set(c.im(), 0);
			break;
		case ATOM_ZR:
			dest.set(zs[n].re(), 0);
			break;
		case ATOM_ZI:
			dest.set(zs[n].im(), 0);
			break;
		case ATOM_Z_LAST: {
			int index = pc.instruction >>> 16;
			dest.set(zs[n - index]);
			break;
		}
		case ATOM_CONST: {
			int index = pc.instruction >>> 16;
			dest.set(constants[index]);
			break;
		}
		case ATOM_VAR: {
			int index = pc.instruction >>> 16;
			dest.set(parameters[index]);
			break;
		}
		case ATOM_INT:
			dest.set(pc.instruction >> 16, 0);
			break;
		default: throw new IllegalArgumentException("No such constant...");
		}
		
		while(pc != null && ((pc.instruction & BINARY_FLAG) == 0)) {
			/* interpret unary function */
			switch(pc.instruction & OP_MASK) {
			case UN_ID: break;
			case UN_NEG:
				dest.neg(dest);
				break;
			case UN_INV:
				dest.inv(dest);
				break;
			case UN_SQR:
				dest.sqr(dest);
				break;
			case UN_SQRT: 
				dest.sqrt(dest);
				break;
			case UN_EXP: 
				dest.exp(dest);
				break;
			case UN_LOG: 
				dest.log(dest);
				break;
			case UN_SIN: 
				dest.sin(dest);
				break;
			case UN_COS: 
				dest.cos(dest);
				break;
			case UN_TAN: 
				dest.tan(dest);
				break;
			case UN_ATAN: 
				dest.atan(dest);
				break;
			case UN_SINH: 
				dest.sinh(dest);
				break;
			case UN_COSH: 
				dest.cosh(dest);
				break;
			case UN_TANH: 
				dest.tanh(dest);
				break;
			case UN_ATANH: 
				dest.atanh(dest);
				break;
			case UN_RE: 
				dest.set(dest.re(), 0);
				break;
			case UN_IM: 
				dest.set(dest.im(), 0);
				break;
			case UN_CONJ: 
				dest.conj(dest);
				break;
			case UN_ABS: 
				dest.set(dest.abs(), 0);
				break;
			case UN_ARG: 
				dest.set(dest.arc(), 0);
				break;
			case UN_FLOOR: 
				dest.set(Math.floor(dest.re()), Math.floor(dest.im()));
				break;
			case UN_POW_INT:
				dest.powInt(dest, pc.instruction >> 16);
				break;
			default: throw new IllegalArgumentException("No such unary function");
			}
			
			pc = pc.next;
			
			while(pc != null && (pc.instruction & ATOM_MASK) != 0) {
				double re0 = dest.re(); // Save zn
				double im0 = dest.im();
				
				// We got another parameter. Call recursively
				pc = execute(pc, c, zs, n, parameters, dest);

				// The next function must be a binary function (otherwise
				// the recursive call would not have terminated)
				
				double re1, im1;
				
				if ((pc.instruction & SWAP_FLAG) == 0) {
					re1 = dest.re();
					im1 = dest.im();
				} else {
					re1 = re0;
					im1 = im0;
					
					re0 = dest.re();
					im0 = dest.im();
				}

				/* recursion is done, interpret binary function */
				switch(pc.instruction & OP_MASK) {
				case BIN_ADD:
					dest.add(re0, im0, re1, im1);
					break;
				case BIN_SUB: 
					dest.sub(re0, im0, re1, im1);
					break;
				case BIN_MUL:
					dest.mul(re0, im0, re1, im1);
					break;
				case BIN_DIV: 
					dest.div(re0, im0, re1, im1);
					break;
				case BIN_POW:
					dest.pow(re0, im0, re1, im1);
					break;
				default: throw new IllegalArgumentException("No such binary function");
				}

				pc = pc.next;
			}
		}
		
		return pc;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("Constants: ");
		sb.append(Arrays.toString(constants));
		sb.append("\n");
		sb.append("Instructions: [");
		
		for(Node n = head; n != null; n = n.next) {
			if(n != head) sb.append("; ");
			sb.append(Integer.toHexString(n.instruction));
		}
		
		sb.append("]");
		
		return sb.toString();
	}
}
