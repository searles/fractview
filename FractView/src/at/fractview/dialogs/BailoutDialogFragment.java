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
import at.fractview.inputviews.PaletteInputView;
import at.fractview.math.colors.Palette;
import at.fractview.modes.orbit.EscapeTime;
import at.fractview.modes.orbit.OrbitToFloat;

public class BailoutDialogFragment extends InputViewDialogFragment {

	private static final String TAG = "BailoutDialogFragment";
	
	EscapeTimeFragment taskFragment;
	
	EditText bailoutEditor;
	ArrayAdapter<OrbitToFloat.Predefined> colorizationAdapter;
	Spinner colorizationTypeSpinner;
	PaletteInputView paletteEditor;
	
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
		Palette palette = taskFragment.prefs().bailoutPalette();
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.bailout, null);
		
		// Initialize components
		bailoutEditor = (EditText) v.findViewById(R.id.bailoutEditor);
		
		bailoutEditor.setText(Double.toString(bailoutValue));
		
		colorizationTypeSpinner = (Spinner) v.findViewById(R.id.colorizationTypeSpinner);
		
		// TODO: only items that are useful for bailout
		colorizationAdapter = new ArrayAdapter<OrbitToFloat.Predefined>(
				getActivity(), 
				android.R.layout.simple_list_item_1, 
				OrbitToFloat.Predefined.values());
		
		colorizationTypeSpinner.setAdapter(colorizationAdapter);

		colorizationTypeSpinner.setSelection(colorizationAdapter.getPosition(method));
		
		paletteEditor = new PaletteInputView(v, palette);

		return v;
	}

	@Override
	protected boolean acceptInput() {
		Palette palette = paletteEditor.acceptAndReturn();
		
		if(palette == null) {
			Log.v(TAG, "Palette was null");
			return false;
		}
		
		double bailoutValue;
		
		try {
			bailoutValue = Double.parseDouble(bailoutEditor.getText().toString());
		} catch(NumberFormatException e) {
			Log.w(TAG, e.toString());
			return false;
		}
		
		OrbitToFloat.Predefined method = (OrbitToFloat.Predefined) colorizationTypeSpinner.getSelectedItem();
		
		EscapeTime prefs = taskFragment.prefs().newBailoutInstance(bailoutValue, method, palette);
		taskFragment.setPrefs(prefs);
		
		return true;
	}
}
