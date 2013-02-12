package at.fractview.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import at.fractview.ImageViewFragment;
import at.fractview.R;
import at.fractview.EscapeTimeFragment;
import at.fractview.R.id;
import at.fractview.R.layout;

public class ResizeDialogFragment extends DialogFragment {
	
	private static final String TAG = "Resize";
	
	/*public static ResizeDialogFragment create(int initWidth, int initHeight) {
		ResizeDialogFragment f = new ResizeDialogFragment();
		
		Bundle args = new Bundle();
		
        args.putInt("width", initWidth);
        args.putInt("height", initHeight);

        f.setArguments(args);
        
        return f;
	}*/
	
	private EscapeTimeFragment taskFragment;
	
	public ResizeDialogFragment() {
		setRetainInstance(true);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO: use number picker
		// Set custom view
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.resize, null);
		
		EscapeTimeFragment taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
		
		int initWidth = taskFragment.bitmap().getWidth();
		int initHeight = taskFragment.bitmap().getHeight();

        final EditText widthEditText = (EditText) v.findViewById(R.id.widthEditText);
        final EditText heightEditText = (EditText) v.findViewById(R.id.heightEditText);
        
        widthEditText.setText(Integer.toString(initWidth));
        heightEditText.setText(Integer.toString(initHeight));

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setTitle("Enter size of bitmap");

        alertDialogBuilder.setView(v);		

		alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EscapeTimeFragment taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
				
				int width = Integer.valueOf(widthEditText.getText().toString());
				int height = Integer.valueOf(heightEditText.getText().toString());
				
				if(width > 2048) {
					Log.v(TAG, "Maximum width is 2048. This is a hard limit of Android");
					width = 2048;
				}
				
				if(height > 2048) {
					Log.v(TAG, "Maximum height is 2048. This is a hard limit of Android");
					height = 2048;
				}
				
				taskFragment.setSize(width, height);
				
				dialog.dismiss();
			}
		});
		
		alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.v(TAG, "Cancel resize.");
				dialog.dismiss();
			}
		});

		return alertDialogBuilder.create();
	}
	
	 @Override
	 public void onDestroyView() {
		 if (getDialog() != null && getRetainInstance())
			 getDialog().setDismissMessage(null);
		 super.onDestroyView();
	 }
}
