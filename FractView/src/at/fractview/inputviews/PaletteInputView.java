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
import java.util.Random;

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
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
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
	
	// private static final String TAG = "PaletteInputView";
	private static final int PADDING = 5;
	
	private View view;
	
	private SeekBar hueSeekBar;
	private SeekBar satSeekBar;
	private SeekBar valSeekBar;
	
	private ImageButton leftButton;
	private ImageButton addButton;
	private ImageButton removeButton;
	private ImageButton rightButton;
	
	private CheckBox cyclicCheckBox;
	
	private int selectedIndex;
	
	private LinearLayout colorLayout;
	private ArrayList<float[]> colors;
	private ArrayList<View> views;
	
	private Palette palette;
	
	private Random rnd;
	
	private Drawable selectedDrawable;
	private Drawable unselectedDrawable;
	
	public PaletteInputView(View view, Palette palette) {
		this.rnd = new Random();
		
		this.view = view;
		
		this.palette = palette;
		
		this.colors = new ArrayList<float[]>();
		this.views = new ArrayList<View>();

		StateListDrawable listDrawables = (StateListDrawable) view.getResources().getDrawable(android.R.drawable.list_selector_background);
		listDrawables.selectDrawable(3);
		selectedDrawable = listDrawables.getCurrent();

		listDrawables.selectDrawable(0);
		unselectedDrawable = listDrawables.getCurrent();
		
		init();
	}
	
	private void init() {
		this.colorLayout = (LinearLayout) view.findViewById(R.id.color_layout);
		
		this.leftButton = (ImageButton) view.findViewById(R.id.moveLeftButton);
		this.addButton = (ImageButton) view.findViewById(R.id.addButton);
		this.removeButton = (ImageButton) view.findViewById(R.id.removeButton);
		this.rightButton = (ImageButton) view.findViewById(R.id.moveRightButton);
		
		this.hueSeekBar = (SeekBar) view.findViewById(R.id.hueSeekBar);
		this.satSeekBar = (SeekBar) view.findViewById(R.id.saturationSeekBar);
		this.valSeekBar = (SeekBar) view.findViewById(R.id.valueSeekBar);

		this.cyclicCheckBox = (CheckBox) view.findViewById(R.id.cyclicCheckBox);

		// Create listeners for add and remove-Button
		leftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int lastSelectedIndex = selectedIndex;
				
				selectedIndex = (selectedIndex - 1 + colors.size()) % colors.size();
				exchangeIndices(lastSelectedIndex, selectedIndex);
				
				updateColorItemView(lastSelectedIndex);

				updateColorItemView(selectedIndex);
				updateSeekBars();
			}
		});
		
		addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				colors.add(selectedIndex, new float[3]);
				setRandomColor(selectedIndex);
				
				addColorItemView(selectedIndex);
				
				updateColorItemView(selectedIndex + 1); // because of selection

				updateColorItemView(selectedIndex);
				updateSeekBars();
			}
		});
		
		removeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(colors.size() > 1) {
					colors.remove(selectedIndex);
					
					removeColorItemView(selectedIndex);
					
					if(selectedIndex >= colors.size()) {
						selectedIndex --;
					}
				} else {
					assert selectedIndex == 0;
					setRandomColor(0);				
				}
				
				updateColorItemView(selectedIndex);
				updateSeekBars();
			}
		});
		
		rightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int lastSelectedIndex = selectedIndex;
				
				selectedIndex = (selectedIndex + 1) % colors.size();
				exchangeIndices(lastSelectedIndex, selectedIndex);
				
				updateColorItemView(lastSelectedIndex);
				updateColorItemView(selectedIndex);
				
				updateSeekBars();
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
		setHueDrawables();
		setSatDrawables();
		// Set in XML file. updateValueProgressDrawables();
		
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
		
		// And set data
		selectedIndex = 0;
		
		for(int i = 0; i < palette.colors().length; i++) {
			this.colors.add(palette.colors()[i]); // TODO: Don't fetch colors like this.
			addColorItemView(i);
			updateColorItemView(i);
		}

		this.cyclicCheckBox.setChecked(palette.cyclic());
		
		
		updateSeekBars();
	}
	
	private void setRandomColor(int position) {
		float hue = rnd.nextFloat() * 360.f;
		float sat = rnd.nextFloat(); 
		sat = (2.f - sat) * sat; // Higher saturation
		
		float val = rnd.nextFloat();
		// Prefer brighter colors
		val = (2.f - val) * val;

		colors.get(position)[0] = hue;
		colors.get(position)[1] = sat;
		colors.get(position)[2] = val;
	}

	private void updatePalette() {
		float[][] colorArray = colors.toArray(new float[colors.size()][3]);
		boolean cyclic = cyclicCheckBox.isChecked();
		this.palette = new Palette(colorArray, cyclic);
	}
	
	public Palette acceptAndReturn() {
		updatePalette();
		return palette;
	}
	
	public View view() {
		return view;
	}
	
	private void updateSeekBars() {
		float[] hsv = colors.get(selectedIndex);
		
		hueSeekBar.setProgress((int) (1000.f * hsv[0] / 360.f));
		satSeekBar.setProgress((int) (1000.f * hsv[1]));
		valSeekBar.setProgress((int) (1000.f * hsv[2]));		
	}
	
	private void exchangeIndices(int i0, int i1) {
		for(int i = 0; i < 3; i++) {
			float t = colors.get(i0)[i];
			colors.get(i0)[i] = colors.get(i1)[i];
			colors.get(i1)[i] = t;
		}
	}
	
	private void addColorItemView(int position) {
		// Create view 
		View v = ((Activity) view.getContext()).getLayoutInflater().inflate(R.layout.color_item, colorLayout, false);
		
		v.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				// We got a new selected index
				int newSelectedIndex = colors.indexOf(v.getTag());
				
				if(newSelectedIndex >= 0) {
					int lastSelectedIndex = selectedIndex;
					selectedIndex = newSelectedIndex;
					
					updateColorItemView(lastSelectedIndex);
					updateColorItemView(selectedIndex);
					
					updateSeekBars();
				}
			}
		});
		
		// and add it
		colorLayout.addView(v, position);
		v.setTag(colors.get(position)); // Attach color object to it.
		
		views.add(position, v);
	}
	
	private void removeColorItemView(int position) {
		colorLayout.removeViewAt(position);
		views.remove(position);
	}
	
	@SuppressWarnings("deprecation") // Deprecated from API 16.
	private void updateColorItemView(int position) {
		// Sets color
		View v = views.get(position);
		
		// Mark selected. For this we need to store the padding (because the drawable has no padding)
	    
		if(position == selectedIndex) {
			v.setBackgroundDrawable(selectedDrawable);
		} else {
			v.setBackgroundDrawable(unselectedDrawable);
		}
		
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING, view.getResources().getDisplayMetrics());
		v.setPadding(px, px, px, px);
		
		// and set text + color
		TextView label = (TextView) v.findViewById(R.id.color_label);
		
		int color = Color.HSVToColor((float[]) v.getTag());
		
		if(Colors.brightness(color) < 125f) {
			label.setTextColor(0xffffffff);
		} else {
			label.setTextColor(0xff000000);
		}
		
		label.setBackgroundColor(color);
		
		String colorText = Integer.toHexString(color & 0x00ffffff); // Skip alpha.
		while(colorText.length() < 6) colorText = "0" + colorText; // Fill up with 0
		
		label.setText(colorText);
	}
	
	private void setHueDrawables() {
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
	
	private void setSatDrawables() {
		ShapeDrawable.ShaderFactory setShader = new ShapeDrawable.ShaderFactory() {
		    @Override
		    public Shader resize(int width, int height) {
		    	Bitmap bm = Bitmap.createBitmap(1, Math.max(1, height), Config.ARGB_8888);
		    	
		    	float[] hsv = new float[]{0.f, 1.f, 1.f};
		    	
		    	// Create rainbow
		    	for(int i = 0; i < height; i++) {
		    		hsv[0] = i * 360.f / height;
		    		int color = Color.HSVToColor(hsv);
		    		bm.setPixel(0, i, color);
		    	}
		    	
		    	BitmapShader shader1 = new BitmapShader(bm, TileMode.CLAMP, TileMode.CLAMP);
		    	
		    	LinearGradient shader2 = new LinearGradient(0, 0, width, 0, 0xffffffff, 0x0, Shader.TileMode.CLAMP);

		    	return new ComposeShader(shader1, shader2, PorterDuff.Mode.SCREEN);
		    }
		};

		PaintDrawable p = new PaintDrawable();
		p.setShape(new RectShape());
		p.setShaderFactory(setShader);
		
		this.satSeekBar.setProgressDrawable((Drawable) p);
	}
	
}
