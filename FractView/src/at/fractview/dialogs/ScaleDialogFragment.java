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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import at.fractview.ImageViewFragment;
import at.fractview.R;
import at.fractview.EscapeTimeFragment;
import at.fractview.math.Affine;

public class ScaleDialogFragment extends DialogFragment {
	
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
	
	public ScaleDialogFragment() {
		setRetainInstance(true);
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
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.affine, null);
		
		this.taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
		
		Affine affine = taskFragment.affine();
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
				
				// TODO
				Log.v(TAG, "Feature not implemented yet");
				
				updateEditors();
			}
        });
        
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setTitle("Set scale");

        alertDialogBuilder.setView(v);		

		alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// get values of matrix
				updateMatrix();
				
				taskFragment.setAffine(Affine.create(matrix));
				
				dialog.dismiss();
			}
		});
		
		alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.v(TAG, "Cancel resize.");
				dialog.dismiss();
			}
		});

		return alertDialogBuilder.create();
	}
	
	 @Override
	 public void onDestroyView() {
		 if (getDialog() != null && getRetainInstance())
			 getDialog().setDismissMessage(null);
		 super.onDestroyView();
	 }
}
