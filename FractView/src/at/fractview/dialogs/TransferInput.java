package at.fractview.dialogs;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import at.fractview.R;
import at.fractview.modes.orbit.colorization.CommonTransfer;
import at.fractview.modes.orbit.colorization.OrbitTransfer;

public class TransferInput {
	
	private static final String TAG = "TransferInput";
	
	private ArrayAdapter<CommonTransfer> transferAdapter;
	private Spinner transferSpinner;

	private EditText minEditor;
	private EditText maxEditor;

	public TransferInput(Activity activity, View v, OrbitTransfer ot) {
		// Initialize transfer
		minEditor = (EditText) v.findViewById(R.id.minEditor);
		
		minEditor.setText(Float.toString(ot.min()));

		maxEditor = (EditText) v.findViewById(R.id.maxEditor);
		
		maxEditor.setText(Float.toString(ot.max()));

		transferSpinner = (Spinner) v.findViewById(R.id.transferSpinner);
		
		transferAdapter = new ArrayAdapter<CommonTransfer>(
				activity, 
				android.R.layout.simple_list_item_1, 
				CommonTransfer.values());
		
		transferSpinner.setAdapter(transferAdapter);

		transferSpinner.setSelection(transferAdapter.getPosition(ot.transfer()));
	}
	
	public OrbitTransfer get() {
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
		
		CommonTransfer transfer = (CommonTransfer) transferSpinner.getSelectedItem();
		
		return new OrbitTransfer(min, max, transfer);
	}
}
