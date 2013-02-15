package at.fractview.dialogs;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import at.fractview.EscapeTimeFragment;
import at.fractview.ImageViewFragment;
import at.fractview.R;
import at.fractview.inputviews.PaletteInputView;
import at.fractview.math.colors.Palette;
import at.fractview.modes.orbit.EscapeTime;

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
		
		// Get value from taskFragment
		
		Palette palette;
		
		if(type == Type.Lake) {
			palette = taskFragment.prefs().lakePalette();
		} else {
			palette = taskFragment.prefs().bailoutPalette();
		}
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.palette, null);
		
		input = new PaletteInputView(v, palette);
		
		return v;
	}

	@Override
	protected boolean acceptInput() {
		Palette palette = input.acceptAndReturn();
		
		if(palette == null) {
			Log.w(TAG, "Returned palette was null");
			return false;
		}
		
		if(type == Type.Lake) {
			EscapeTime prefs = taskFragment.prefs().newLakePaletteInstance(palette);
			taskFragment.setPrefs(prefs);
		} else {
			EscapeTime prefs = taskFragment.prefs().newBailoutPaletteInstance(palette);
			taskFragment.setPrefs(prefs);
		}
		
		return true;
	}
}
