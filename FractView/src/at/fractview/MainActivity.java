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

import android.app.Activity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import at.fractview.dialogs.BailoutDialogFragment;
import at.fractview.dialogs.FunctionDialogFragment;
import at.fractview.dialogs.LakeDialogFragment;
import at.fractview.dialogs.MaxIterDialogFragment;
import at.fractview.dialogs.ResizeDialogFragment;
import at.fractview.dialogs.SaveDialogFragment;
import at.fractview.dialogs.ScaleDialogFragment;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.v(TAG, "onCreate");
		
		setContentView(R.layout.activity_main);
		
		// Keep the screen on when the fractal is rendering and the app is in the foreground
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_menu, menu);
		getMenuInflater().inflate(R.menu.escapetime, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.save:
			new SaveDialogFragment().show(getFragmentManager(), "dialog");
			return true;
		case R.id.function:
			new FunctionDialogFragment().show(getFragmentManager(), "dialog");
			return true;
		case R.id.bailout:
			new BailoutDialogFragment().show(getFragmentManager(), "dialog");
			return true;
		case R.id.lake:
			new LakeDialogFragment().show(getFragmentManager(), "dialog");
			return true;
		case R.id.maxiter:
			new MaxIterDialogFragment().show(getFragmentManager(), "dialog");
			return true;
		case R.id.scale:
			new ScaleDialogFragment().show(getFragmentManager(), "dialog");
			return true;
		case R.id.resize:
			new ResizeDialogFragment().show(getFragmentManager(), "dialog");
			return true;
		default: return super.onOptionsItemSelected(item);
		}
	}
}
