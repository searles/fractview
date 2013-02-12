package at.fractview;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import at.fractview.dialogs.FunctionDialogFragment;
import at.fractview.dialogs.MaxIterDialogFragment;
import at.fractview.dialogs.PaletteDialogFragment;
import at.fractview.dialogs.ResizeDialogFragment;
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
			// TODO
			return true;
		case R.id.share:
			// TODO
			return true;
		case R.id.function:
			new FunctionDialogFragment().show(getFragmentManager(), "dialog");
			return true;
		case R.id.bailout:
			//new BailoutDialogFragment().show(getFragmentManager(), "dialog");
			return true;
		case R.id.lake:
			//new LakeDialogFragment().show(getFragmentManager(), "dialog");
			return true;
		case R.id.maxiter:
			//new MaxIterDialogFragment().show(getFragmentManager(), "dialog");
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
