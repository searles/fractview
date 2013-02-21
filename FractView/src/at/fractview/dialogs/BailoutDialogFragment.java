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
 */package at.fractview.dialogs;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import at.fractview.EscapeTimeFragment;
import at.fractview.ImageViewFragment;
import at.fractview.R;
import at.fractview.modes.orbit.EscapeTime;
import at.fractview.modes.orbit.OrbitToFloat;

public class BailoutDialogFragment extends InputViewDialogFragment {

	private static final String TAG = "BailoutDialogFragment";
	
	private EscapeTimeFragment taskFragment;
	
	private EditText bailoutEditor;
	private ArrayAdapter<OrbitToFloat.Predefined> colorizationAdapter;
	private Spinner colorizationTypeSpinner;
	
	@Override
	protected String title() {
		return "Bailout-Settings";
	}

	@Override
	protected View createView() {
		taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
		
		// Get values from taskFragment
		double bailoutValue = taskFragment.prefs().bailout();
		OrbitToFloat.Predefined method = taskFragment.prefs().bailoutDrawingMethod();
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.bailout, null);
		
		// Initialize components
		bailoutEditor = (EditText) v.findViewById(R.id.bailoutEditor);
		
		bailoutEditor.setText(Double.toString(bailoutValue));
		
		colorizationTypeSpinner = (Spinner) v.findViewById(R.id.colorizationTypeSpinner);
		
		colorizationAdapter = new ArrayAdapter<OrbitToFloat.Predefined>(
				getActivity(), 
				android.R.layout.simple_list_item_1, 
				OrbitToFloat.Predefined.values());
		
		colorizationTypeSpinner.setAdapter(colorizationAdapter);

		colorizationTypeSpinner.setSelection(colorizationAdapter.getPosition(method));
		
		return v;
	}

	@Override
	protected boolean acceptInput() {
		double bailoutValue;
		
		try {
			bailoutValue = Double.parseDouble(bailoutEditor.getText().toString());
		} catch(NumberFormatException e) {
			Log.w(TAG, e.toString());
			return false;
		}
		
		OrbitToFloat.Predefined method = (OrbitToFloat.Predefined) colorizationTypeSpinner.getSelectedItem();
		
		EscapeTime prefs = taskFragment.prefs().newBailoutInstance(bailoutValue, method);
		taskFragment.setPrefs(prefs);
		
		return true;
	}
}
