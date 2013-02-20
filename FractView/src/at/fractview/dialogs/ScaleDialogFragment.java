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
package at.fractview.dialogs;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import at.fractview.EscapeTimeFragment;
import at.fractview.ImageViewFragment;
import at.fractview.R;
import at.fractview.math.Affine;
import at.fractview.modes.orbit.EscapeTime;

public class ScaleDialogFragment extends InputViewDialogFragment {
	
	private static final String TAG = "Scale";
	
	/*public static ResizeDialogFragment create(int initWidth, int initHeight) {
		ResizeDialogFragment f = new ResizeDialogFragment();
		
		Bundle args = new Bundle();
		
        args.putInt("width", initWidth);
        args.putInt("height", initHeight);

        f.setArguments(args);
        
        return f;
	}*/
	
	private EscapeTimeFragment taskFragment;
	
	private EditText[] editors;
	private double[] matrix;
	
	
	@Override
	protected String title() {
		return "Affine Transformation";
	}

	@Override
	protected View createView() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View v = inflater.inflate(R.layout.affine, null);
		
		this.taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
		
		Affine affine = taskFragment.prefs().affine();
        matrix = affine.get();
		
		editors = new EditText[6];
		
        editors[0] = (EditText) v.findViewById(R.id.aEditor);
        editors[1] = (EditText) v.findViewById(R.id.bEditor);
        editors[2] = (EditText) v.findViewById(R.id.eEditor);
        editors[3] = (EditText) v.findViewById(R.id.cEditor);
        editors[4] = (EditText) v.findViewById(R.id.dEditor);
        editors[5] = (EditText) v.findViewById(R.id.fEditor);

        // Set text in editors
        updateEditors();
        
        // Set listener for straigthen-button
        ((Button) v.findViewById(R.id.straightenButton)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View buttonView) {
				updateMatrix();
				
				// Get center
				double vx = matrix[0] + matrix[1];
				double vy = matrix[3] + matrix[4];

				double cx = vx / 2. + matrix[2];
				double cy = vy / 2. + matrix[5];
				
				// Get max scaling factor
				double scale = Math.max(Math.hypot(matrix[0], matrix[3]), Math.hypot(matrix[1], matrix[4]));
				
				// Get appropriate orientation by checking in which direction the vector matrix * (1,1) is pointing
				if(vx * vy < 0) {
					matrix[0] = matrix[4] = 0.;
					matrix[1] = matrix[3] = -scale;
				} else {
					matrix[0] = matrix[4] = scale;
					matrix[1] = matrix[3] = 0.;
				}
				
				if(vx < 0) {
					matrix[0] = -matrix[0];
					matrix[3] = -matrix[3];
				}
				
				if(vy < 0) {
					matrix[1] = -matrix[1];
					matrix[4] = -matrix[4];
				}
				
				// Move center back to top left.
				vx = matrix[0] + matrix[1];
				vy = matrix[3] + matrix[4];

				matrix[2] = cx - vx / 2.;
				matrix[5] = cy - vy / 2.;
				
				updateEditors();
			}
        });
		
		return v;
	}

	@Override
	protected boolean acceptInput() {
		updateMatrix();
		EscapeTime prefs = taskFragment.prefs().newAffineInstance(Affine.create(matrix));
		taskFragment.setPrefs(prefs);
		return true;
	}
	
	private void updateEditors() {
        for(int i = 0; i < 6; i++) {
        	editors[i].setText(Double.toString(matrix[i]));
        }
	}
	
	private void updateMatrix() {
        for(int i = 0; i < 6; i++) {
        	try {
        		matrix[i] = Double.valueOf(editors[i].getText().toString());
        	} catch(NumberFormatException e) {
        		Log.w(TAG, i + "th editor does not contain a valid number");
        	}
        }
	}
}
