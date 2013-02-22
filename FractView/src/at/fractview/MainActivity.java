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

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import at.fractview.dialogs.BailoutDialogFragment;
import at.fractview.dialogs.FunctionDialogFragment;
import at.fractview.dialogs.LakeDialogFragment;
import at.fractview.dialogs.MaxIterDialogFragment;
import at.fractview.dialogs.PaletteDialogFragment;
import at.fractview.dialogs.ResizeDialogFragment;
import at.fractview.dialogs.SaveDialogFragment;
import at.fractview.dialogs.ScaleDialogFragment;

public class MainActivity extends FragmentActivity {

	@SuppressWarnings("unused")
	private static final String TAG = "MainActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
			new SaveDialogFragment().show(getSupportFragmentManager(), "dialog");
			return true;
		case R.id.function:
			new FunctionDialogFragment().show(getSupportFragmentManager(), "dialog");
			return true;
		case R.id.bailout:
			new BailoutDialogFragment().show(getSupportFragmentManager(), "dialog");
			return true;
		case R.id.bailoutcolors:
			PaletteDialogFragment pdf = new PaletteDialogFragment();
			pdf.setType(PaletteDialogFragment.Type.Bailout); // TODO Use setArguments
			pdf.show(getSupportFragmentManager(), "dialog");
			return true;
		case R.id.lake:
			new LakeDialogFragment().show(getSupportFragmentManager(), "dialog");
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
