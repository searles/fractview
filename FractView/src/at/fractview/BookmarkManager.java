package at.fractview;

import java.util.List;

import android.graphics.Bitmap;
import at.fractview.modes.Preferences;

public class BookmarkManager {
	
	private static final int PREVIEW_SIZE = 64;
	
	private class Entry {
		Bitmap preview;
		Preferences prefs;
		
		
	}
	
	List<Entry> bookmarkList;
	
	// TODO: Load from file, store to file
}
