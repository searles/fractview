package at.fractview.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import at.fractview.BookmarkManager;
import at.fractview.EscapeTimeFragment;
import at.fractview.ImageViewFragment;
import at.fractview.R;
import at.fractview.UnsafeImageEditor;
import at.fractview.modes.AbstractImgCache;
import at.fractview.modes.orbit.EscapeTime;

public class BookmarksDialogFragment extends InputViewDialogFragment {

	// TODO: Allow deleting of bookmarks
	
	// private static final String TAG = "BookmarksDialogFragment";

	private EscapeTimeFragment taskFragment;

	private ListView listView;
	
	private BookmarkManager manager;
	
	private List<String> bookmarkTitles;
	private Map<String, BookmarkManager.Bookmark> bookmarkMap;

	@Override
	protected String title() {
		return "Bookmarks";
	}

	@Override
	protected View createView() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View v = inflater.inflate(R.layout.bookmarks, null);

		if(taskFragment == null) {
			taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
			
			// Fetch bookmarks
			this.manager = taskFragment.bookmarkManager();
			
			bookmarkMap = this.manager.readBookmarks();
			
			bookmarkTitles = new ArrayList<String>();
			bookmarkTitles.addAll(bookmarkMap.keySet());
			
			listView = (ListView) v.findViewById(R.id.bookmarks);

			StateListDrawable listDrawables = (StateListDrawable) getResources().getDrawable(android.R.drawable.list_selector_background);
			listDrawables.selectDrawable(3); // The third is the one I can select
			Drawable highlightDrawable = listDrawables.getCurrent();
			
			listView.setSelector(highlightDrawable);
			
			listView.setAdapter(new Adapter());
		}

		return v;
	}

	@Override
	protected boolean acceptInput() {
		int position = listView.getCheckedItemPosition();

		if(position >= 0) {
			final EscapeTime prefs = bookmarkMap.get(bookmarkTitles.get(position)).prefs();
		
			taskFragment.modifyImage(new UnsafeImageEditor() {
				@Override
				public void edit(AbstractImgCache cache) {
					cache.setNewPreferences(prefs);
				}
			}, true);
			
			return true;
		}

		return false;
	}
	
	private class Adapter extends BaseAdapter {

		public Adapter() {
			super();
		}
		
		@Override
		public int getCount() {
			return bookmarkTitles.size();
		}

		@Override
		public BookmarkManager.Bookmark getItem(int position) {
			return bookmarkMap.get(bookmarkTitles.get(position));
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		    
			if(convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.bookmark_entry, null);
				convertView.setClickable(false);
			}
			
			TextView timeLabel = (TextView) convertView.findViewById(R.id.timeLabel);
			TextView titleLabel = (TextView) convertView.findViewById(R.id.titleLabel);
			ImageView previewImageView = (ImageView) convertView.findViewById(R.id.previewImageView);

			String title = bookmarkTitles.get(position);
			BookmarkManager.Bookmark bookmark = bookmarkMap.get(title);
			
			timeLabel.setText(bookmark.timeString());
			
			titleLabel.setText(title);
			
			previewImageView.setImageBitmap(bookmark.preview());

			return convertView;
		}
	}
}
