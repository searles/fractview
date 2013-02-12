package at.fractview.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import at.fractview.FunctionAdapter;
import at.fractview.ImageViewFragment;
import at.fractview.EscapeTimeFragment;

public class FunctionDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
	private static final String TAG = "FunctionDialogFragment";

	private EscapeTimeFragment taskFragment;
	private FunctionAdapter functionView;
	
	public FunctionDialogFragment() {
		setRetainInstance(true);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate, savedInstanceState = " + savedInstanceState);
		taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
		super.onCreate(savedInstanceState);	
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.v(TAG, "onCreateDialog, savedInstanceState = " + savedInstanceState);
		
		this.functionView = FunctionAdapter.create(getActivity(), taskFragment.function().spec());

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setTitle("Set function");

		alertDialogBuilder.setView(this.functionView.view());		

		alertDialogBuilder.setPositiveButton("OK", this);
		alertDialogBuilder.setNegativeButton("Cancel", this);

		return alertDialogBuilder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if(which == -1) {
			functionView.acceptAllInput();
			taskFragment.setFunction(functionView.spec().create());
		}		
		
		dialog.dismiss();
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
