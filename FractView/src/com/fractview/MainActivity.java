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
package com.fractview;

import com.fractview.dialogs.AddBookmarkDialogFragment;
import com.fractview.dialogs.BookmarksDialogFragment;
import com.fractview.dialogs.FunctionDialogFragment;
import com.fractview.dialogs.MaxIterDialogFragment;
import com.fractview.dialogs.OrbitTransferDialogFragment;
import com.fractview.dialogs.PaletteDialogFragment;
import com.fractview.dialogs.ResizeDialogFragment;
import com.fractview.dialogs.SaveDialogFragment;
import com.fractview.dialogs.ScaleDialogFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	private static final String TAG = "MainActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate activity");
		
		setContentView(R.layout.activity_main);
		
		// Keep the screen on when the fractal is rendering and the app is in the foreground
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.save:
			new SaveDialogFragment().show(getSupportFragmentManager(), "dialog");
			return true;
		case R.id.function:
			new FunctionDialogFragment().show(getSupportFragmentManager(), "dialog");
			return true;
		case R.id.bailout:
			OrbitTransferDialogFragment otdf = new OrbitTransferDialogFragment();
			otdf.setType(OrbitTransferDialogFragment.Type.Bailout);
			otdf.show(getSupportFragmentManager(), "dialog");
			return true;
		case R.id.bailoutcolors:
			PaletteDialogFragment pdf = new PaletteDialogFragment();
			pdf.setType(PaletteDialogFragment.Type.Bailout); // TODO Use setArguments
			pdf.show(getSupportFragmentManager(), "dialog");
			return true;
		case R.id.lake:
			otdf = new OrbitTransferDialogFragment();
			otdf.setType(OrbitTransferDialogFragment.Type.Lake);
			otdf.show(getSupportFragmentManager(), "dialog");
			return true;
		case R.id.lakecolors:
			pdf = new PaletteDialogFragment();
			pdf.setType(PaletteDialogFragment.Type.Lake); // TODO Use setArguments
			pdf.show(getSupportFragmentManager(), "dialog");
			return true;
		case R.id.maxiter:
			new MaxIterDialogFragment().show(getSupportFragmentManager(), "dialog");
			return true;
		case R.id.scale:
			new ScaleDialogFragment().show(getSupportFragmentManager(), "dialog");
			return true;
		case R.id.resize:
			new ResizeDialogFragment().show(getSupportFragmentManager(), "dialog");
			return true;
		case R.id.add_bookmark:
			new AddBookmarkDialogFragment().show(getSupportFragmentManager(), "dialog");
			return true;
		case R.id.from_bookmark:
			new BookmarksDialogFragment().show(getSupportFragmentManager(), "dialog");
			return true;
		default: return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onBackPressed() {
		EscapeTimeFragment taskFragment = (EscapeTimeFragment) getSupportFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);

		if(taskFragment.isHistoryEmpty()) {
			super.onBackPressed();			
		} else {
			if(!taskFragment.historyBack()) {
				Toast.makeText(this, "First element in history", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
