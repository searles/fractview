/*
 * This file is part of FractView.
 *
 * FractView is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FractView is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FractView.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.fractview.dialogs;

import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import at.fractview.EscapeTimeFragment;
import at.fractview.ImageViewFragment;
import at.fractview.R;
import at.fractview.UnsafeImageEditor;
import at.fractview.modes.AbstractImgCache;

public class ResizeDialogFragment extends InputViewDialogFragment {
	
	private static final String TAG = "Resize";
	
	/*public static ResizeDialogFragment create(int initWidth, int initHeight) {
	 * TODO
		ResizeDialogFragment f = new ResizeDialogFragment();
		
		Bundle args = new Bundle();
		
        args.putInt("width", initWidth);
        args.putInt("height", initHeight);

        f.setArguments(args);
        
        return f;
	}*/
	
	private EscapeTimeFragment taskFragment;
	
	private EditText widthEditor;
	private EditText heightEditor;
	
	@Override
	protected String title() {
		return "Resize";
	}

	@Override
	protected View createView() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View v = inflater.inflate(R.layout.resize, null);
		
		taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
		
		int initWidth = taskFragment.bitmap().getWidth();
		int initHeight = taskFragment.bitmap().getHeight();

        widthEditor = (EditText) v.findViewById(R.id.widthEditText);
        heightEditor = (EditText) v.findViewById(R.id.heightEditText);
        
        widthEditor.setText(Integer.toString(initWidth));
        heightEditor.setText(Integer.toString(initHeight));
        
        return v;
	}

	@Override
	protected boolean acceptInput() {
		final int width;
		
		try {
			width = Integer.parseInt(widthEditor.getText().toString());

			if(width <= 0 || width > 2048) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Range must be between 1 - 2048").setTitle("Bad size").setNeutralButton("Close", null).create().show();
				return false;
			}
		} catch(NumberFormatException e) {
			Log.d(TAG, "Invalid number format");
			return false;
		}

		final int height;
		
		try {
			height = Integer.parseInt(heightEditor.getText().toString());
			
			if(height <= 0 || height > 2048) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Range must be between 1 - 2048").setTitle("Bad size").setNeutralButton("Close", null).create().show();
				return false;
			}			
		} catch(NumberFormatException e) {
			Log.d(TAG, "Invalid number format");
			return false;
		}
		
		taskFragment.modifyImage(new UnsafeImageEditor() {
			@Override
			public void edit(AbstractImgCache cache) {
				cache.resize(width, height);
			}
		}, false);
		
		return true;
	}
}
