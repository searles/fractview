package at.fractview.dialogs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import at.fractview.EscapeTimeFragment;
import at.fractview.ImageViewFragment;
import at.fractview.R;

public class SaveDialogFragment extends InputViewDialogFragment {

	private static final String TAG = "SaveDialogFragment";
	
	private EscapeTimeFragment taskFragment;
	
	private EditText filenameEditor;
	private EditText descriptionEditor;	

	@Override
	protected String title() {
		return "Save as file";
	}

	@Override
	protected View createView() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View v = inflater.inflate(R.layout.save, null);
		
		taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
		
		filenameEditor = (EditText) v.findViewById(R.id.filenameEditor);
		descriptionEditor = (EditText) v.findViewById(R.id.descriptionEditor);
		
		filenameEditor.setText("Fractal");

		return v;
	}

	@Override
	protected boolean acceptInput() {
		String filename = filenameEditor.getText().toString();
		String description = descriptionEditor.getText().toString();
		
		Bitmap bm = taskFragment.bitmap();

		// MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bm, filename , description);

		// Get path for picture
		File directory = new File(
				Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), 
				"FractView"); 
		
		Log.v(TAG, "Path is " + directory);

		if(!directory.exists()) {
			Log.v(TAG, "Creating directory");
			directory.mkdir();
		}

		try {
			File imageFile = File.createTempFile(filename, ".png", directory);

			if(imageFile.exists()) {
				// TODO If file exists, show dialog.
				Log.v(TAG, "file exists!");
			}
			
			FileOutputStream fos = new FileOutputStream(imageFile);
			
			if(bm.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
				// Successfully written picture
				fos.close();
				Log.v(TAG, "Successfully wrote image file");

				// Add it to the gallery
				Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				Uri contentUri = Uri.fromFile(imageFile);
				mediaScanIntent.setData(contentUri);
				getActivity().sendBroadcast(mediaScanIntent);			
			} else {
				// TODO: Error message
				Log.d(getClass().toString(), "Could not write image file");
				fos.close();
				return false;
			}
		} catch(IOException e) {
			// TODO: Error message
			e.printStackTrace();
			Log.e(getClass().toString(), e.getMessage());
		}
		
		return true;
	}

}
