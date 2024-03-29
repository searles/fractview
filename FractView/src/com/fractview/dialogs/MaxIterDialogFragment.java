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
package com.fractview.dialogs;

import com.fractview.EscapeTimeFragment;
import com.fractview.ImageViewFragment;
import com.fractview.R;
import com.fractview.UnsafeImageEditor;
import com.fractview.modes.AbstractImgCache;
import com.fractview.modes.orbit.AbstractOrbitPrefs;
import com.fractview.modes.orbit.EscapeTime;
import com.fractview.modes.orbit.EscapeTimeCache;

import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class MaxIterDialogFragment extends InputViewDialogFragment {
	
	private static final String TAG = "MaxIter";

	private EditText editor;
	private EscapeTimeFragment taskFragment;
	
	private int maxIter;
	
	@Override
	protected String title() {
		return "Maximum number of iterations";
	}

	@Override
	protected View createView() {		
		taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View v = inflater.inflate(R.layout.maxiter, null);

		editor = (EditText) v.findViewById(R.id.maxIterEditor);
		
		maxIter = ((EscapeTime) taskFragment.prefs()).maxIter();
		
		editor.setText(Integer.toString(maxIter));
		
		return v;
	}

	@Override
	protected boolean acceptInput() {
		try {
			int maxIter = Integer.parseInt(editor.getText().toString());
			
			if(maxIter == 0 || maxIter > AbstractOrbitPrefs.MAX_LENGTH) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Range must be between 1 - " + AbstractOrbitPrefs.MAX_LENGTH).setTitle("Bad number").setNeutralButton("Close", null).create().show();
				maxIter = AbstractOrbitPrefs.MAX_LENGTH;
			}

			final int i = maxIter;

			taskFragment.modifyImage(new UnsafeImageEditor() {
				@Override
				public void edit(AbstractImgCache cache) {
					EscapeTimeCache ch = (EscapeTimeCache) cache;
					ch.setMaxIter(i);
				}
			}, true);
			
			return true;
		} catch(NumberFormatException e) {
			Log.d(TAG, "Invalid number format");
			return false;
		}
	}
}
