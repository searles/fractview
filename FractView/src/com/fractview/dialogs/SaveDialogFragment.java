package com.fractview.dialogs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.fractview.EscapeTimeFragment;
import com.fractview.ImageViewFragment;
import com.fractview.R;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

// TODO: Create text file with dump.
public class SaveDialogFragment extends InputViewDialogFragment {

	private static final String TAG = "SaveDialogFragment";
	
	private EscapeTimeFragment taskFragment;
	
	private EditText filenameEditor;
	private CheckBox shareCheckBox;
	private CheckBox wallpaperCheckBox;
	
	@Override
	protected String title() {
		return "Save/Share Image";
	}
	
	@Override
	protected View createView() {
		Log.d(TAG, "createView");
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View v = inflater.inflate(R.layout.save, null);

		if(taskFragment == null) {
			taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
			
			filenameEditor = (EditText) v.findViewById(R.id.filenameEditor);
			filenameEditor.setText("FractView " + taskFragment.prefs().toString());
			
			shareCheckBox = (CheckBox) v.findViewById(R.id.share_check_box);
			wallpaperCheckBox = (CheckBox) v.findViewById(R.id.wallpaper_check_box);
		}

		return v;
	}
	
	private boolean saveFile() {
		String filename = filenameEditor.getText().toString();
		
		Bitmap bm = taskFragment.bitmap();

		// Get path for picture
		File directory = new File(
				Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), 
				"FractView"); 
		
		Log.d(TAG, "Path is " + directory);

		if(!directory.exists()) {
			Log.d(TAG, "Creating directory");
			directory.mkdir();
		}

		File imageFile = new File(directory, filename + ".png");
		
		for(int i = 1; imageFile.exists(); i++) {
			// We do not erase old files.
			Log.d(TAG, "file exists! " + imageFile);
			imageFile = new File(directory, filename + "(" + i + ").png");
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(imageFile);
			
			if(bm.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
				// Successfully written picture
				fos.close();

				// TODO: If checked, create text file with description
				
				// Show toast
				Toast.makeText(getActivity(), "Image saved as " + imageFile.getName(), Toast.LENGTH_SHORT).show();

				// Add it to the gallery
				Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				Uri contentUri = Uri.fromFile(imageFile);
				mediaScanIntent.setData(contentUri);
				getActivity().sendBroadcast(mediaScanIntent);
				
				// If share is selected share it
				if(shareCheckBox.isChecked()) {
					Intent share = new Intent(Intent.ACTION_SEND);
					share.setType("image/png");
					share.putExtra(Intent.EXTRA_STREAM, contentUri);
					startActivity(Intent.createChooser(share, "Share Image"));
				}
				
				// If wallpaper is selected, set as wallpaper
				if(wallpaperCheckBox.isChecked()) {
					WallpaperManager wallpaperManager = WallpaperManager.getInstance(getActivity());
					wallpaperManager.setBitmap(bm);
				}
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Unknown error calling 'compress' on bitmap").setTitle("Error saving file").setNeutralButton("Close", null).create().show();
				
				Log.w(TAG, "Could not write image file");
				fos.close();
				return false;
			}
		} catch(IOException e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(e.getMessage()).setTitle("Error saving file").setNeutralButton("Close", null).create().show();
			Log.e(TAG, e.getMessage());
		}
		
		return true;
	}
	
	@Override
	protected boolean acceptInput() {
		if(taskFragment.taskIsRunning()) {
			// Show yes-no-dialog indicating that the task is still running...
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			    	if(which == DialogInterface.BUTTON_POSITIVE) {
			    		// save file 
			    		if(saveFile()) {
				    		// and close original dialog
				    		SaveDialogFragment.this.dismiss();
			    		}
			    	} else {
			    		// Dismiss save-dialog without saving.
			    		SaveDialogFragment.this.dismiss();			    		
			    	}
			        
			    	// Dismiss this dialog
			        // Will be done anyways...
			    	// dialog.dismiss();
			    }
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Calculation is still running. Do you still want to save image?")
				.setPositiveButton("Yes", dialogClickListener)
			    .setNegativeButton("No", dialogClickListener).show();
			
			return false;
		}
		
		return saveFile();
	}

}
