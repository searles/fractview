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
import at.fractview.modes.orbit.colorization.CommonOrbitToFloat;
import at.fractview.modes.orbit.colorization.OrbitTransfer;

public class BailoutDialogFragment extends InputViewDialogFragment {

	private static final String TAG = "BailoutDialogFragment";
	
	private EscapeTimeFragment taskFragment;
	
	private EditText bailoutEditor;
	
	private ArrayAdapter<CommonOrbitToFloat> methodAdapter;
	private Spinner methodSpinner;
	
	private TransferInput transferInput;

	@Override
	protected String title() {
		return "Bailout-Settings";
	}

	@Override
	protected View createView() {
		taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.bailout, null);
		
		// Initialize components
		bailoutEditor = (EditText) v.findViewById(R.id.bailoutEditor);
		
		bailoutEditor.setText(Double.toString(taskFragment.prefs().bailout()));
		
		methodSpinner = (Spinner) v.findViewById(R.id.methodSpinner);
		
		methodAdapter = new ArrayAdapter<CommonOrbitToFloat>(
				getActivity(), 
				android.R.layout.simple_list_item_1, 
				CommonOrbitToFloat.values());
		
		methodSpinner.setAdapter(methodAdapter);

		methodSpinner.setSelection(methodAdapter.getPosition(taskFragment.prefs().bailoutMethod()));

		this.transferInput = new TransferInput(getActivity(), v, taskFragment.prefs().bailoutTransfer());
		
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
		
		OrbitTransfer transfer = transferInput.get();
		
		if(transfer == null) {
			Log.w(TAG, "Transfer is null");
			return false;
		}
		
		CommonOrbitToFloat method = (CommonOrbitToFloat) methodSpinner.getSelectedItem();
		
		EscapeTime prefs = taskFragment.prefs().newBailout(bailoutValue, method, transfer);
		taskFragment.setPrefs(prefs);
		
		return true;
	}
}
