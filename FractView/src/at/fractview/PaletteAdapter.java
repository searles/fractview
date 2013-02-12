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
package at.fractview;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import at.fractview.math.colors.Colors;
import at.fractview.math.colors.Palette;

/**
 * This class holds all the necessary parts for the color-array, i.e., an
 * array containing color values that are editable. It supports single selection
 * and dynamic adding and removing of colors.
 *
 */
public class PaletteAdapter {
	
	private static final String TAG = "ColorArray";
	
	private static final int PREVIEW_WIDTH = 64; 
	
	private View view;
	
	/**
	 * Layout containing color items
	 */
	LinearLayout layout;
	
	Button addButton;
	Button removeButton;
	
	EditText lengthEditor;
	
	CheckBox cyclicCheckBox;
	
	private Bitmap preview;
	ImageView previewView;
	
	int selectedIndex;
	int visibleViewCount;
	ArrayList<View> itemViews;
	
	private ArrayList<Integer> colors;
	
	private Palette palette;
	
	public static PaletteAdapter create(Activity activity, Palette palette) {
		LayoutInflater inflater = activity.getLayoutInflater();
		View v = inflater.inflate(R.layout.palette, null);
		
		return new PaletteAdapter(v, palette);
	}
	
	private PaletteAdapter(View view, Palette palette) {
		this.view = view;
		
		this.palette = palette;
		
		this.colors = new ArrayList<Integer>();
		this.itemViews = new ArrayList<View>();
		this.visibleViewCount = 0;

		init();
	}
	
	private void init() {
		this.layout = (LinearLayout) view.findViewById(R.id.colorArrayLayout);
		
		this.addButton = (Button) view.findViewById(R.id.addButton);
		this.removeButton = (Button) view.findViewById(R.id.removeButton);

		this.lengthEditor = (EditText) view.findViewById(R.id.lengthEditor);
		this.cyclicCheckBox = (CheckBox) view.findViewById(R.id.cyclicCheckBox);

		this.previewView = (ImageView) view.findViewById(R.id.palettePreviewImageView);
				

		// Create listeners for add and remove-Button
		addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addSelect(Color.GREEN);
			}
		});
		
		removeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				remove();
			}
		});
		
		this.lengthEditor.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				acceptInput();
				return true;
			}
		});
		
		this.lengthEditor.setText(Float.toString(palette.length()));
		
		this.cyclicCheckBox.setChecked(palette.cyclic());
		
		this.cyclicCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean checked) {
				acceptInput();
			}
		});
		
		this.preview = Bitmap.createBitmap(PREVIEW_WIDTH, 1, Config.ARGB_8888);
		this.previewView.setImageBitmap(this.preview);
		
		// Add colors
		for(int color : palette.colors()) {
			add(color);
		}
		
		// and update all color items
		for(int i = 0; i < visibleViewCount; i++) {
			updateColorItemView(i);
		}
		
		updatePreview();
	}
	
	
	private void updatePreview() {
		for(int x = 0; x < PREVIEW_WIDTH; x++) {
			int color = palette.color(x * palette.length() / (PREVIEW_WIDTH - 1));
			this.preview.setPixel(x, 0, color);
		}
		
		this.previewView.invalidate();
	}
	
	public void acceptInput() {
		int[] colorArray = new int[colors.size()];
		
		for(int i = 0; i < colors.size(); i++) {
			colorArray[i] = colors.get(i);
		}
		
		boolean cyclic = cyclicCheckBox.isChecked();
		
		float length;
		
		try {
			length = Float.valueOf(lengthEditor.getText().toString());
		} catch(NumberFormatException e) {
			// Use old value
			length = palette.length();
			lengthEditor.setText(Float.toString(length));
		}
		
		this.palette = new Palette(colorArray, cyclic, length);
		
		updatePreview();		
	}
	
	public Palette get() {
		return palette;
	}
	
	public View view() {
		return view;
	}
	
	private void updateColorItemView(int index) {
		Log.v(TAG, "Updating view of " + index);
		// Updates the content of the view at position index.
		View v = itemViews.get(index);
		
		// update listener
		((Listener) v.getTag()).setIndex(index);
		
		// update content of view
		EditText editor = (EditText) itemViews.get(index).findViewById(R.id.colorText);
		Button button = (Button) itemViews.get(index).findViewById(R.id.colorButton);

		// Update color
		int color = colors.get(index);
		button.setBackgroundColor(color);
		
		// Get brightness
		// and set text white or black.
		button.setTextColor(Colors.brightness(color) > 100.f ? 0xff000000 : 0xffffffff);
		
		editor.setText(Colors.toColorString(color));
		
		// Toggle selection
		button.setText(index == selectedIndex ? "Selected" : "");
	}
	
	/**
	 * Cancels any selection that was made previously
	 */
	public void select(int index) {
		int oldSelectedIndex = selectedIndex;
		selectedIndex = index;
		
		if(oldSelectedIndex >= 0) {
			// Cancel selection
			Button button = (Button) itemViews.get(oldSelectedIndex).findViewById(R.id.colorButton);
			button.setText("");
		}
		
		if(selectedIndex >= 0) {
			Button button = (Button) itemViews.get(selectedIndex).findViewById(R.id.colorButton);
			button.setText("Selected");
		}
	}
	
	private int add(int color) {
		int index;
		
		if(selectedIndex < 0) {
			index = colors.size() - 1;
		} else {
			index = selectedIndex;
		}
		
		colors.add(index, color);

		Log.v(TAG, "itemViewSize = " + itemViews.size());
		
		// update layout: Add one element
		if(itemViews.size() == visibleViewCount) {
			// not enough views
			Log.v(TAG, "Creating view with index " + visibleViewCount);
			createItemView();
		}

		Log.v(TAG, "Adding view with index " + visibleViewCount);
		
		layout.addView(itemViews.get(visibleViewCount), visibleViewCount);
		visibleViewCount++;
		
		return index;
	}
	
	/** Adds a color.
	 * @param color
	 */
	public void addSelect(int color) {
		// Set selection to new element
		selectedIndex = add(color);
		
		// update views
		for(int i = selectedIndex; i < visibleViewCount; i++) {
			updateColorItemView(i);
		}
	}
	
	public boolean hasSelected() {
		return selectedIndex != -1;
	}
	
	public boolean canRemove() {
		return selectedIndex >= 0 && colors.size() > 1;
	}
	
	public boolean remove() {
		if(canRemove()) {
			colors.remove(selectedIndex);
			
			if(selectedIndex == colors.size()) {
				// if it was the last one that was removed, go left.
				selectedIndex--;
			}
			
			// Remove last element from layout
			--visibleViewCount;
			Log.v(TAG, "Removing view with index " + visibleViewCount);

			layout.removeViewAt(visibleViewCount);
			
			// update views
			for(int i = selectedIndex; i < visibleViewCount; i++) {
				updateColorItemView(i);
			}
			
			return true;
		}
		
		return false;
	}
	
	private View createItemView() {
		LayoutInflater inflater = ((Activity) view.getContext()).getLayoutInflater();
		View v = inflater.inflate(R.layout.color_item, layout, false);
		
		// Create listener
		EditText editor = (EditText) v.findViewById(R.id.colorText);
		
		Listener l = new Listener(editor);
		l.setIndex(itemViews.size());
		
		editor.setOnEditorActionListener(l);
		((Button) v.findViewById(R.id.colorButton)).setOnClickListener(l);
		
		// Set it as tag
		v.setTag(l);
		
		// Add component to list
		itemViews.add(v);
		
		return v;
	}
	
	private class Listener implements OnEditorActionListener, OnClickListener {

		int index;
		EditText editor;
		
		Listener(EditText editor) {
			this.editor = editor;
		}
		
		void setIndex(int index) {
			this.index = index;
		}
		
		@Override
		public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
			// Set color
			String colorString = textView.getText().toString();
			Log.v(TAG, "Editor Action: " + colorString);
			
			try {
				int color = Colors.parseColorString(colorString);
				colors.set(index, color);
				updateColorItemView(index);
				acceptInput();
			} catch(NumberFormatException e) {
				return false;
			}
			
			return true;
		}

		@Override
		public void onClick(View buttonView) {
			if(selectedIndex == index) {
				// unselect
				select(-1);
			} else {
				select(index);
			}
			
			editor.requestFocus();
		}
	}
}
