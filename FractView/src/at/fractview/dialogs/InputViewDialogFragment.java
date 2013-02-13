package at.fractview.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public abstract class InputViewDialogFragment extends DialogFragment implements OnClickListener {
	
	private static final String TAG = "AdapterDialogFragment";
	
	public InputViewDialogFragment() {
		setRetainInstance(true);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate, savedInstanceState = " + savedInstanceState);
		super.onCreate(savedInstanceState);	
	}

	protected abstract String title();
	protected abstract View createView();
	protected abstract boolean acceptInput();

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.v(TAG, "onCreateDialog, savedInstanceState = " + savedInstanceState);
		
		View v = createView();

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setTitle(title());

		alertDialogBuilder.setView(v);		

		alertDialogBuilder.setPositiveButton("OK", this);
		alertDialogBuilder.setNegativeButton("Cancel", this);

		return alertDialogBuilder.create();
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if(which == -1) {
			if( acceptInput()) {
				dialog.dismiss();
			} else {
				Log.v(TAG, "Not dismissing dialog because of errors");
			}
			// else the view itself is supposed to show
			// an error dialog indicating what went wrong.
		} else {
			// cancel
			dialog.dismiss();
		}
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		Log.v(TAG, "onDismiss");
		super.onDismiss(dialog);
	}
	
	@Override
	public void onDestroyView() {
		Log.v(TAG, "onDestroyView");

		if (getDialog() != null && getRetainInstance()) {
			Log.v(TAG, "setDismissMessage(null)");
			getDialog().setDismissMessage(null);
		}
		
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy");
		super.onDestroy();
	}
}
