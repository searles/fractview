package at.fractview.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public abstract class InputViewDialogFragment extends DialogFragment {
	
	private static final String TAG = "InputViewDialogFragment";
	private View view;
	
	public InputViewDialogFragment() {
		setRetainInstance(true);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate, savedInstanceState = " + savedInstanceState);
		super.onCreate(savedInstanceState);
		
		this.view = createView();
	}

	protected abstract String title();
	protected abstract View createView();
	protected abstract boolean acceptInput();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	Log.d(TAG, "onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.d(TAG, "onCreateDialog, savedInstanceState = " + savedInstanceState);
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setTitle(title());

		alertDialogBuilder.setView(view);		

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
			// See http://creeder.com/?&page=AsyncTask
			Log.d(TAG, "setDismissMessage(null)");
			getDialog().setDismissMessage(null);

			// Remove view from parent
			((ViewGroup) view.getParent()).removeView(view);
		}
		
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}
}
