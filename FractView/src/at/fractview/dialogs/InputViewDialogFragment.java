package at.fractview.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public abstract class InputViewDialogFragment extends DialogFragment {
	
	private static final String TAG = "AdapterDialogFragment";
	
	public InputViewDialogFragment() {
		setRetainInstance(true);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate, savedInstanceState = " + savedInstanceState);
		super.onCreate(savedInstanceState);	
	}

	protected abstract String title();
	protected abstract View createView();
	protected abstract boolean acceptInput();

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.d(TAG, "onCreateDialog, savedInstanceState = " + savedInstanceState);
		
		View v = createView();

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setTitle(title());

		alertDialogBuilder.setView(v);		

		alertDialogBuilder.setPositiveButton("OK", null); // We set ok-button later
		alertDialogBuilder.setNegativeButton("Cancel", null);
		
		final AlertDialog dialog = alertDialogBuilder.create();
		
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

		    @Override
		    public void onShow(DialogInterface d) {
		    	// This is the listener for the ok-button
		        Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		        b.setOnClickListener(new View.OnClickListener() {
		            @Override
		            public void onClick(View view) {
		                if(acceptInput()) { 
		                	dialog.dismiss();
		                } else {
		                	Log.d(TAG, "Not dismissing dialog");
		                }
		            }
		        });
		    }
		});

		return dialog;
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		Log.d(TAG, "onDismiss");
		super.onDismiss(dialog);
	}
	
	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView");

		if (getDialog() != null && getRetainInstance()) {
			Log.d(TAG, "setDismissMessage(null)");
			getDialog().setDismissMessage(null);
		}
		
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}
}
