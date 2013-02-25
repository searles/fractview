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
import android.widget.TextView;
import at.fractview.EscapeTimeFragment;
import at.fractview.ImageViewFragment;
import at.fractview.R;
import at.fractview.UnsafeImageEditor;
import at.fractview.modes.AbstractImgCache;
import at.fractview.modes.orbit.EscapeTime;
import at.fractview.modes.orbit.EscapeTimeCache;
import at.fractview.modes.orbit.colorization.CommonOrbitToFloat;
import at.fractview.modes.orbit.colorization.OrbitTransfer;

public class OrbitTransferDialogFragment extends InputViewDialogFragment {

	private static final String TAG = "OrbitTransferDialogFragment";
	
	public enum Type { Bailout, Lake };
	
	private Type type;
	
	private EscapeTimeFragment taskFragment;
	
	private TextView valueLabel;
	private EditText valueEditor;
	private ArrayAdapter<CommonOrbitToFloat> methodAdapter;
	private Spinner methodSpinner;
	private TransferInput transferInput;

	public void setType(Type type) {
		this.type = type;

		if(valueLabel != null) {
			valueLabel.setText(type.toString());
		}
	}

	@Override
	protected String title() {
		return type + "-Settings";
	}
	
	@Override
	protected View createView() {
		// Initialize view
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.orbit, null);
		
		// Initialize components
		valueLabel = (TextView) v.findViewById(R.id.valueLabel);
		valueLabel.setText(type.toString());
		
		valueEditor = (EditText) v.findViewById(R.id.valueEditor);
		
		methodSpinner = (Spinner) v.findViewById(R.id.methodSpinner);
		
		methodAdapter = new ArrayAdapter<CommonOrbitToFloat>(
				getActivity(), 
				android.R.layout.simple_list_item_1, 
				CommonOrbitToFloat.values());
		
		methodSpinner.setAdapter(methodAdapter);

		// Get data
		taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
		EscapeTime prefs = (EscapeTime) taskFragment.prefs();

		if(type == Type.Bailout) {
			valueEditor.setText(Double.toString(prefs.bailout()));
			methodSpinner.setSelection(methodAdapter.getPosition(prefs.bailoutMethod()));
			transferInput = new TransferInput(getActivity(), v, prefs.bailoutTransfer());
		} else {
			valueEditor.setText(Double.toString(prefs.epsilon()));
			methodSpinner.setSelection(methodAdapter.getPosition(prefs.lakeMethod()));
			transferInput = new TransferInput(getActivity(), v, prefs.lakeTransfer());
		}
		
		return v;
	}
	
	@Override
	protected boolean acceptInput() {
		final double value;
		
		try {
			value = Double.parseDouble(valueEditor.getText().toString());
		} catch(NumberFormatException e) {
			Log.w(TAG, e.toString());
			return false;
		}
		
		final OrbitTransfer transfer = transferInput.get();
		
		if(transfer == null) {
			Log.w(TAG, "Transfer is null");
			return false;
		}
		
		final CommonOrbitToFloat method = (CommonOrbitToFloat) methodSpinner.getSelectedItem();
		
		UnsafeImageEditor editor;
		
		if(type == Type.Bailout) {
			editor = new UnsafeImageEditor() {
				@Override
				public void edit(AbstractImgCache cache) {
					EscapeTimeCache ch = (EscapeTimeCache) cache;
					
					ch.newBailout(value);
					ch.newBailoutMethod(method);
					ch.newBailoutTransfer(transfer);
				}
			};
		} else { // Lake
			editor = new UnsafeImageEditor() {
				@Override
				public void edit(AbstractImgCache cache) {
					EscapeTimeCache ch = (EscapeTimeCache) cache;
					
					ch.newEpsilon(value);
					ch.newLakeMethod(method);
					ch.newLakeTransfer(transfer);
				}
			};
		}
		
		taskFragment.modifyImage(editor, true);
		
		return true;
	}
}
