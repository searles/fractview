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
package at.fractview.inputviews;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import at.fractview.R;
import at.fractview.math.colors.Colors;
import at.fractview.math.colors.Palette;

/**
 * This class holds all the necessary parts for the color-array, i.e., an
 * array containing color values that are editable. It supports single selection
 * and dynamic adding and removing of colors.
 *
 */
public class PaletteInputView {
	
	private static final String TAG = "ColorArray";
	
	private View view;
	
	/**
	 * Layout containing color items
	 */
	private LinearLayout layout;
	
	private SeekBar hueSeekBar;
	private SeekBar satSeekBar;
	private SeekBar valSeekBar;
	
	private Button leftButton;
	private Button addButton;
	private Button removeButton;
	private Button rightButton;
	
	private EditText lengthEditor;
	
	private CheckBox cyclicCheckBox;
	
	private int selectedIndex;
	private int visibleViewCount;
	private ArrayList<View> itemViews;
	
	private ArrayList<float[]> colors;
	
	private Palette palette;
	
	public PaletteInputView(View view, Palette palette) {
		this.view = view;
		
		this.palette = palette;
		
		this.colors = new ArrayList<float[]>();
		this.itemViews = new ArrayList<View>();
		this.visibleViewCount = 0;

		init();
	}
	
	private void init() {
		this.layout = (LinearLayout) view.findViewById(R.id.colorArrayLayout);
		
		this.leftButton = (Button) view.findViewById(R.id.moveLeftButton);
		this.addButton = (Button) view.findViewById(R.id.addButton);
		this.removeButton = (Button) view.findViewById(R.id.removeButton);
		this.rightButton = (Button) view.findViewById(R.id.moveRightButton);
		
		this.hueSeekBar = (SeekBar) view.findViewById(R.id.hueSeekBar);
		this.satSeekBar = (SeekBar) view.findViewById(R.id.saturationSeekBar);
		this.valSeekBar = (SeekBar) view.findViewById(R.id.valueSeekBar);

		this.lengthEditor = (EditText) view.findViewById(R.id.lengthEditor);
		this.cyclicCheckBox = (CheckBox) view.findViewById(R.id.cyclicCheckBox);

		// Create listeners for add and remove-Button
		leftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int next = (selectedIndex + visibleViewCount - 1) % visibleViewCount;
				exchangeIndices(selectedIndex, next);
				select(next);
			}
		});
		
		addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				add(Color.BLACK);
			}
		});
		
		removeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				remove();
			}
		});
		
		rightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int next = (selectedIndex + 1) % visibleViewCount;
				exchangeIndices(selectedIndex, next);
				select(next);
			}
		});
		

		hueSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser) {
					float[] hsv = colors.get(selectedIndex);					
					hsv[0] = progress * 360.f / 1000;
					updateColorItemView(selectedIndex);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		
		// Set shape of progress drawables
		updateHueProgressDrawables();
		updateSatProgressDrawables();
		updateValueProgressDrawables();
		
		satSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser) {
					float[] hsv = colors.get(selectedIndex);					
					hsv[1] = progress / 1000.f;
					
					updateColorItemView(selectedIndex);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		
		valSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser) {
					float[] hsv = colors.get(selectedIndex);					
					hsv[2] = progress / 1000.f;
					
					updateColorItemView(selectedIndex);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		
		
		this.lengthEditor.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// Not necessary to update anything here because it is normalized anyways
				return true;
			}
		});
		
		this.lengthEditor.setText(Float.toString(palette.length()));
		
		this.cyclicCheckBox.setChecked(palette.cyclic());
		
		// Add colors
		visibleViewCount = 0;
		selectedIndex = 0;
		
		for(int color : palette.colors()) {
			add(color);
		}
		
		updateSeekBars();
	}
	
	private void updateHueProgressDrawables() {
		// Set gradients for hue-seekbar
		ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {
		    @Override
		    public Shader resize(int width, int height) {
		    	LinearGradient gradient = new LinearGradient(0.f, 0.f, width, 0.0f,  
		    			new int[] { 0xffff0000, 0xffffff00, 0xff00ff00, 0xff00ffff, 0xff0000ff, 0xffff00ff, 0xffff0000}, 
		    				      null, TileMode.CLAMP);
		    	return gradient;
		    }
		};
		
		PaintDrawable p = new PaintDrawable();
		p.setShape(new RectShape());
		p.setShaderFactory(sf);
		
		this.hueSeekBar.setProgressDrawable((Drawable) p);
	}
	
	private void updateSatProgressDrawables() {
		ShapeDrawable.ShaderFactory setShader = new ShapeDrawable.ShaderFactory() {
		    @Override
		    public Shader resize(int width, int height) {
		    	Bitmap bm = Bitmap.createBitmap(1, Math.max(1, height), Config.ARGB_8888);
		    	
		    	float[] hsv = new float[]{0.f, 1.f, 1.f};
		    	
		    	for(int i = 0; i < height; i++) {
		    		hsv[0] = i * 360.f / height;
		    		int color = Color.HSVToColor(hsv);
		    		bm.setPixel(0, i, color);
		    	}
		    	
		    	BitmapShader shader1 = new BitmapShader(bm, TileMode.CLAMP, TileMode.CLAMP);
		    	
		    	LinearGradient shader2 = new LinearGradient(0, 0, width, 0, 0x00ffffff, 0xffffffff, Shader.TileMode.CLAMP);

		    	return new ComposeShader(shader1, shader2, PorterDuff.Mode.MULTIPLY);
		    }
		};

		PaintDrawable p = new PaintDrawable();
		p.setShape(new RectShape());
		p.setShaderFactory(setShader);
		
		this.satSeekBar.setProgressDrawable((Drawable) p);
	}
	
	private void updateValueProgressDrawables() {
		ShapeDrawable.ShaderFactory valShader = new ShapeDrawable.ShaderFactory() {
		    @Override
		    public Shader resize(int width, int height) {
		    	LinearGradient gradient = new LinearGradient(0.f, 0.f, width, 0.0f,  
		    			new int[] { 0xff000000, 0xffffffff }, null, TileMode.CLAMP);
		    	return gradient;
		    }
		};
		
		PaintDrawable p = new PaintDrawable();
		p.setShape(new RectShape());
		p.setShaderFactory(valShader);
		
		this.valSeekBar.setProgressDrawable((Drawable) p);
	}
	
	private void updatePalette() {
		int[] colorArray = new int[colors.size()];
		
		for(int i = 0; i < colors.size(); i++) {
			colorArray[i] = Color.HSVToColor(colors.get(i));
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
	}
	
	public Palette acceptAndReturn() {
		updatePalette();
		return palette;
	}
	
	public View view() {
		return view;
	}
	
	private void updateColorItemView(int index) {
		Button button = (Button) itemViews.get(index).findViewById(R.id.colorButton);

		// Update color
		int color = Color.HSVToColor(colors.get(index));
		
		button.setBackgroundColor(color);
		
		if(selectedIndex == index) {
			// Get brightness and set text white or black.
			button.setTextColor(Colors.brightness(color) > 100.f ? 0xff000000 : 0xffffffff);
			button.setText(Colors.toColorString(color));
		} else {
			button.setText("");
		}
	}
	
	private void updateSeekBars() {
		float[] hsv = colors.get(selectedIndex);
		
		hueSeekBar.setProgress((int) (1000.f * hsv[0] / 360.f));
		satSeekBar.setProgress((int) (1000.f * hsv[1]));
		valSeekBar.setProgress((int) (1000.f * hsv[2]));		
	}
	
	/**
	 * Cancels any selection that was made previously
	 */
	public void select(int index) {
		int oldSelected = selectedIndex;
		selectedIndex = index;

		updateColorItemView(oldSelected);
		updateColorItemView(selectedIndex);
		
		updateSeekBars();
	}
	
	public void add(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		
		colors.add(selectedIndex, hsv);

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

		// update views
		for(int i = selectedIndex; i < visibleViewCount; i++) {
			updateColorItemView(i);
		}
		
		updateSeekBars();
	}
	
	public boolean canRemove() {
		return colors.size() > 1;
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
	
	private void exchangeIndices(int i0, int i1) {
		float[] color0 = colors.get(i0);
		float[] color1 = colors.get(i1);
		
		colors.set(i0, color1);
		colors.set(i1, color0);
		
		updateColorItemView(i0);
		updateColorItemView(i1);
		
		updateSeekBars();
	}
	
	private View createItemView() {
		LayoutInflater inflater = ((Activity) view.getContext()).getLayoutInflater();
		View v = inflater.inflate(R.layout.color_item, layout, false);
		
		final int index = itemViews.size();

		Button button = (Button) v.findViewById(R.id.colorButton);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View buttonView) {
				if(selectedIndex != index) {
					select(index);
				}
			}
		});
		
		// Add component to list
		itemViews.add(v);
		
		return v;
	}
}
