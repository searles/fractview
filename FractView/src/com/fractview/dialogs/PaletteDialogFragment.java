package com.fractview.dialogs;

import com.fractview.EscapeTimeFragment;
import com.fractview.ImageViewFragment;
import com.fractview.R;
import com.fractview.UnsafeImageEditor;
import com.fractview.inputviews.PaletteInputView;
import com.fractview.math.colors.Palette;
import com.fractview.modes.AbstractImgCache;
import com.fractview.modes.orbit.EscapeTime;
import com.fractview.modes.orbit.EscapeTimeCache;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class PaletteDialogFragment extends InputViewDialogFragment {

	public enum Type { Bailout, Lake };
	
	private static final String TAG = "PaletteDialogFragment";
	
	private EscapeTimeFragment taskFragment;
	private PaletteInputView input;
	
	private Type type;
	
	@Override
	protected String title() {
		return "Edit Palette";
	}
	
	public void setType(Type type) {
		this.type = type;
	}

	@Override
	protected View createView() {
		taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
		EscapeTime prefs = (EscapeTime) taskFragment.prefs();

		// Get value from taskFragment
		
		Palette palette;
		
		if(type == Type.Lake) {
			palette = prefs.lakePalette();
		} else {
			palette = prefs.bailoutPalette();
		}
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.palette, null);
		
		input = new PaletteInputView(v, palette);
		
		return v;
	}

	@Override
	protected boolean acceptInput() {
		final Palette palette = input.acceptAndReturn();
		
		if(palette == null) {
			Log.w(TAG, "Returned palette was null");
			return false;
		}
		
		UnsafeImageEditor editor;
		
		if(type == Type.Bailout) {
			editor = new UnsafeImageEditor() {
				@Override
				public void edit(AbstractImgCache cache) {
					EscapeTimeCache ch = (EscapeTimeCache) cache;
					ch.newBailoutPalette(palette);
				}
			};
		} else {
			editor = new UnsafeImageEditor() {
				@Override
				public void edit(AbstractImgCache cache) {
					EscapeTimeCache ch = (EscapeTimeCache) cache;
					ch.newLakePalette(palette);
				}
			};
		}
		
		taskFragment.modifyImage(editor, true);
		
		return true;
	}
}
