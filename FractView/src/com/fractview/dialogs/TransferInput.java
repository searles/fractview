package com.fractview.dialogs;

import com.fractview.R;
import com.fractview.modes.orbit.colorization.CommonTransfer;
import com.fractview.modes.orbit.colorization.OrbitTransfer;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

public class TransferInput {
	
	private static final String TAG = "TransferInput";
	
	private ArrayAdapter<CommonTransfer> transferAdapter;
	private Spinner transferSpinner;

	private EditText minEditor;
	private EditText maxEditor;
	
	private CheckBox customRangeCheckBox;
	
	// TODO: If checked

	public TransferInput(Activity activity, View v, OrbitTransfer ot, OrbitTransfer.Stats defaultStats) {
		// Initialize transfer
		minEditor = (EditText) v.findViewById(R.id.minEditor);
		maxEditor = (EditText) v.findViewById(R.id.maxEditor);
		
		customRangeCheckBox = (CheckBox) v.findViewById(R.id.customRangeCheckBox);
		customRangeCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean checked) {
				minEditor.setEnabled(checked);
				maxEditor.setEnabled(checked);
			}
		});

		transferSpinner = (Spinner) v.findViewById(R.id.transferSpinner);
		
		transferAdapter = new ArrayAdapter<CommonTransfer>(
				activity, 
				android.R.layout.simple_list_item_1, 
				CommonTransfer.values());
		
		transferSpinner.setAdapter(transferAdapter);

		if(ot.customStats()) {
			customRangeCheckBox.setChecked(true);

			minEditor.setText(Float.toString(ot.stats().minValue()));
			maxEditor.setText(Float.toString(ot.stats().maxValue()));
		} else {
			customRangeCheckBox.setChecked(false);
			
			minEditor.setText(Float.toString(defaultStats.minValue()));
			maxEditor.setText(Float.toString(defaultStats.maxValue()));
			
			minEditor.setEnabled(false);
			maxEditor.setEnabled(false);
		}

		transferSpinner.setSelection(transferAdapter.getPosition(ot.transfer()));
	}
	
	public OrbitTransfer get() {
		CommonTransfer transfer = (CommonTransfer) transferSpinner.getSelectedItem();
		
		if(customRangeCheckBox.isChecked()) {
			float min;
			float max;
			
			try {
				min = Float.parseFloat(minEditor.getText().toString());
			} catch(NumberFormatException e) {
				Log.w(TAG, e.toString());
				return null;
			}

			try {
				max = Float.parseFloat(maxEditor.getText().toString());
			} catch(NumberFormatException e) {
				Log.w(TAG, e.toString());
				return null;
			}
			
			return new OrbitTransfer(transfer, new OrbitTransfer.Stats(min, max));
		} else {
			return new OrbitTransfer(transfer, null);
		}
	}
}
