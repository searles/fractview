
package at.fractview.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import at.fractview.ImageViewFragment;
import at.fractview.PaletteAdapter;
import at.fractview.EscapeTimeFragment;
import at.fractview.math.colors.Palette;

public class PaletteDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
	private static final String TAG = "PaletteDialogFragment";

	private EscapeTimeFragment taskFragment;
	private PaletteAdapter colorArray;
	
	public PaletteDialogFragment() {
		setRetainInstance(true);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Fetch taskFragment
		Log.v(TAG, "onCreate, savedInstanceState = " + savedInstanceState);
		taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
		super.onCreate(savedInstanceState);	
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.v(TAG, "onCreateDialog, savedInstanceState = " + savedInstanceState);
		
        Palette paletteBailout = new Palette(
				new int[]{0xFFFFAA00, 0xFF310230, 0xff000764, 0xff206BCB, 0xffEDFFFF},
				true, 1);

        PaletteAdapter adapter = PaletteAdapter.create(getActivity(), paletteBailout);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setTitle("Set palette");

		alertDialogBuilder.setView(adapter.view());		

		alertDialogBuilder.setPositiveButton("OK", this);
		alertDialogBuilder.setNegativeButton("Cancel", this);

		return alertDialogBuilder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if(which == -1) {
			Log.v(TAG, "ok was clicked");
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
