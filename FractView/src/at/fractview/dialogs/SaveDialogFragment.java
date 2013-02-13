package at.fractview.dialogs;

import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import at.fractview.EscapeTimeFragment;
import at.fractview.ImageViewFragment;
import at.fractview.R;

public class SaveDialogFragment extends InputViewDialogFragment {

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

		MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bm, filename , description);

		// Get path for picture
		/*File storageDir = new File(
			    Environment.getExternalStoragePublicDirectory(
			        Environment.DIRECTORY_PICTURES
			    ), "FractView"); 
		
		if(bm.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream())) {
			// Successfully written picture
			
			// Add it to the gallery
		    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		    File f = new File(mCurrentPhotoPath);
		    Uri contentUri = Uri.fromFile(f);
		    mediaScanIntent.setData(contentUri);
		    this.sendBroadcast(mediaScanIntent);			
		} else {
			// TODO: Error message
		}*/
		
		return true;
	}

}
