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
package com.fractview;

import android.graphics.Matrix;
import android.util.SparseArray;

/**
 *
 */
public class MultiTouch {

	// private static String TAG = "MultiTouch";
	
	private SparseArray<float[]> first; // start positions of pointers of the current selection
	private SparseArray<float[]> current;  // current positions

	private Matrix matrix; 	// matrix that represents the selection of all previous 
							// (i.e. not including the current one) selections
	
	public MultiTouch() {
		first = new SparseArray<float[]>();
		current = new SparseArray<float[]>();
		
		matrix = new Matrix();
	}
	
	/**
	 * @return The matrix that represents the current selection
	 */
	public Matrix matrix() {
		Matrix m = currentMatrix();
		m.preConcat(matrix);
		
		return m;
	}

	/**
	 * This method takes the current selection, combines it with the current 
	 * transformation matrix and resets the initial points to the current selection.
	 * Thereby we obtain incremental selections, i.e., we can combine a one-finger-move-selection
	 * with scaling and skewing.
	 */
	private void updateMatrix() {
		// Create matrix out of points, then set start to current
		Matrix m = currentMatrix();
		
		matrix.postConcat(m);
		
		// set first to current
		first = current;
		
		// and clear current
		current = new SparseArray<float[]>();
	}
	
	/** Tells that id was put down.
	 * @param id
	 * @param p
	 */
	public void down(int id, float[] p) {
		updateMatrix(); // update matrix, clear first and 
		first.put(id,  new float[]{p[0], p[1]}); // add new point to first
	}

	/** moves of "id" to p.
	 * @param id
	 * @param p
	 */
	public void moveTo(int id, float[] p) {
		if(first.get(id) == null) {
			// Sometimes, a multi-touch event is initialized
			// with not enough fingers
			first.put(id, new float[]{p[0], p[1]});
		}
		
		current.put(id, new float[]{p[0], p[1]});
	}

	/** Tells that id was lifted up
	 * @param id
	 */
	public void up(int id) {
		updateMatrix(); // update matrix and
		first.remove(id); // remove point
	}
	
	/** This method takes up to 3 points where a selection started/ended and 
	 * returns the corresponding affine transformation. By transforming the points
	 * it is easy to use this method also to scaled instances. In order to be
	 * compatible to android's matrix-class the final array will have 9 elements where
	 * the last ones are 0f, 0f and 1f.
	 * @param start
	 * @param end
	 * @return
	 */
	private Matrix currentMatrix() {
		if(first.size() == 0) return new Matrix();
		
		// Create matrix out of points, then set start to current
		float[][] start = new float[first.size()][];
		float[][] end = new float[first.size()][];
		
		for (int i = 0; i < first.size(); i++) {
			int key = first.keyAt(i);

			start[i] = first.get(key);
			end[i] = current.get(key);
			
			if(end[i] == null) end[i] = start[i];
		}
		
		float[] m;
		
		if (start.length == 1) {
			// Move only
			float e = end[0][0] - start[0][0];
			float f = end[0][1] - start[0][1];

			m = new float[]{1, 0, e, 0, 1, f, 0f, 0f, 1f};
		} else if (start.length == 2) {
			// Scale + rotate + move
			float vx0 = start[1][0] - start[0][0];
			float vy0 = start[1][1] - start[0][1];

			float vx1 = end[1][0] - end[0][0];
			float vy1 = end[1][1] - end[0][1];
			
			float det = vx0 * vx0 + vy0 * vy0;

			float a = (vx0 * vx1 + vy0 * vy1) / det;
			float b = -(vx0 * vy1 - vx1 * vy0) / det;

			float mx = (start[0][0] + start[1][0]) / 2f;
			float my = (start[0][1] + start[1][1]) / 2f;

			float e = -(a * mx + b * my) + (end[0][0] + end[1][0]) / 2f;
			float f = -(-b * mx + a * my) + (end[0][1] + end[1][1]) / 2f;

			m = new float[]{a, b, e, -b, a, f, 0f, 0f, 1f};
		} else /* if (start.size() == 3)*/ {
			// Here we only consider 3 points. Other points are ignored.
			// Free style
			// Matrix to solve is
			// p00 p01 1 | q0n  
			// p10 p11 1 | q1n = a b e (for n = 0), c d f (for n = 1)
			// p20 p21 1 | q2n
			
			float det = start[0][0] * start[1][1] + start[1][0] * start[2][1] + start[2][0] * start[0][1]
					  - start[0][0] * start[2][1] - start[1][0] * start[0][1] - start[2][0] * start[1][1];
			
			// Cramer: replace start[n][0] by end[n][0] 
			float detA = end[0][0] * start[1][1] + end[1][0] * start[2][1] + end[2][0] * start[0][1]
					  - end[0][0] * start[2][1] - end[1][0] * start[0][1] - end[2][0] * start[1][1];

			// Cramer: replace start[n][1] by end[n][0] 
			float detB = start[0][0] * end[1][0] + start[1][0] * end[2][0] + start[2][0] * end[0][0]
					   - start[0][0] * end[2][0] - start[1][0] * end[0][0] - start[2][0] * end[1][0];

			// Cramer: replace start[n][0] by end[n][0] 
			float detC = end[0][1] * start[1][1] + end[1][1] * start[2][1] + end[2][1] * start[0][1]
					   - end[0][1] * start[2][1] - end[1][1] * start[0][1] - end[2][1] * start[1][1];

			// Cramer: replace start[n][1] by end[n][0] 
			float detD = start[0][0] * end[1][1] + start[1][0] * end[2][1] + start[2][0] * end[0][1]
					   - start[0][0] * end[2][1] - start[1][0] * end[0][1] - start[2][0] * end[1][1];

			// Cramer: replace 1 by end[n][0] 
			float detE = start[0][0] * start[1][1] * end[2][0] + start[1][0] * start[2][1] * end[0][0] + start[2][0] * start[0][1] * end[1][0]
					   - start[0][0] * start[2][1] * end[1][0] - start[1][0] * start[0][1] * end[2][0] - start[2][0] * start[1][1] * end[0][0];
			
			// Cramer: replace 1 by end[n][0] 
			float detF = start[0][0] * start[1][1] * end[2][1] + start[1][0] * start[2][1] * end[0][1] + start[2][0] * start[0][1] * end[1][1]
					   - start[0][0] * start[2][1] * end[1][1] - start[1][0] * start[0][1] * end[2][1] - start[2][0] * start[1][1] * end[0][1];

			m = new float[]{detA / det, detB / det, detE / det, detC / det, detD / det, detF / det, 0f, 0f, 1f};
		}
		
		Matrix ret = new Matrix();
		ret.setValues(m);
		
		return ret;
	}
}