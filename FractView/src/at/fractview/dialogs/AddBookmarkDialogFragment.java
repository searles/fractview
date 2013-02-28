package at.fractview.dialogs;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import at.fractview.BookmarkManager;
import at.fractview.EscapeTimeFragment;
import at.fractview.ImageViewFragment;
import at.fractview.R;
import at.fractview.modes.orbit.EscapeTime;

public class AddBookmarkDialogFragment extends InputViewDialogFragment {

	private static final String TAG = "AddBookmarkDialogFragment";
	
	private EscapeTimeFragment taskFragment;
	
	private BookmarkManager manager;
	
	private EditText titleEditor;
	
	@Override
	protected String title() {
		return "Add Bookmark";
	}
	
	@Override
	protected View createView() {
		Log.d(TAG, "createView");
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View v = inflater.inflate(R.layout.add_bookmark, null);

		if(taskFragment == null) {
			taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
			
			manager = taskFragment.bookmarkManager();
			
			titleEditor = (EditText) v.findViewById(R.id.titleEditor);
			
			String title = taskFragment.prefs().toString();
			
			if(manager.containsBookmark(title)) {
				int i = 1;
				while(manager.containsBookmark(title + " [" + i + "]")) i++;
				
				title = title + " [" + i + "]";
			}
			
			titleEditor.setText(title);
		}

		return v;
	}

	@Override
	protected boolean acceptInput() {
		// TODO: check when overwriting bookmarks.
		String title = titleEditor.getText().toString();
		manager.addBookmark(title, (EscapeTime) taskFragment.prefs(), taskFragment.bitmap());
		
		return true;
	}
}
