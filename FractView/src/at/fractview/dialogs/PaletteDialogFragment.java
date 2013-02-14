package at.fractview.dialogs;

import android.view.LayoutInflater;
import android.view.View;
import at.fractview.EscapeTimeFragment;
import at.fractview.ImageViewFragment;
import at.fractview.R;
import at.fractview.inputviews.PaletteInputView;
import at.fractview.math.colors.Palette;

public class PaletteDialogFragment extends InputViewDialogFragment {

	EscapeTimeFragment taskFragment;
	PaletteInputView input;
	
	@Override
	protected String title() {
		return "Edit Palette";
	}

	@Override
	protected View createView() {
		taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
		
		// Get value from taskFragment
		Palette palette = taskFragment.prefs().bailoutPalette();
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.palette, null);
		
		input = new PaletteInputView(v, palette);
		
		return v;
	}

	@Override
	protected boolean acceptInput() {
		return true;
	}
}
