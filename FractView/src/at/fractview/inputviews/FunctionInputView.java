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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import at.fractview.R;
import at.fractview.math.Cplx;
import at.fractview.math.tree.Expr;
import at.fractview.math.tree.ExprCompiler;
import at.fractview.math.tree.Num;
import at.fractview.math.tree.Parser;
import at.fractview.math.tree.Var;
import at.fractview.modes.orbit.functions.Specification;
import at.fractview.tools.Labelled;

/**
 * @author searles
 * TODO: Change so that it can hold also only parameter values (for predefined fractals).
 */
public class FunctionInputView {

	private static final String TAG = "FunctionAdapter";
	
	public static FunctionInputView create(Activity activity, Specification spec) {
		LayoutInflater inflater = activity.getLayoutInflater();
		View v = inflater.inflate(R.layout.function_parameters, null);
		
		return new FunctionInputView(v, spec);
	}
	
	// We repack data of specification and merge them with a label
	// that then can be used in a ListView
	private boolean hasFunction;
	
	private Fn fn;
	private ArrayList<Init> inits;
	private TreeMap<Var, Parameter> parameters;
	
	// Once we created an element, we do not delete it because we might use it some other time.
	
	// This is the number of inits required by fn. 
	// It might be smaller than inits.size()
	private int initCount; 
	
	// This is the set of variables used in fn and currently used inits. 
	// It is a subset of parameters.keySey()
	private Set<Var> usedParameters;

	private View view;
	
	private LinearLayout layout;
	
	private List<View> itemViews;
	private int viewCountInLayout; // We store old itemViews, therefore this might be smaller than itemViews.size()
	
	private FunctionInputView(View view, Specification spec) {
		this.view = view;
		this.layout = (LinearLayout) view.findViewById(R.id.functionLayout);;
		
		this.itemViews = new ArrayList<View>();
		this.viewCountInLayout = 0;
		
		// Create data structures out of parameters
		this.hasFunction = true;
		this.fn = new Fn(spec.function());
		this.inits = new ArrayList<Init>(spec.initsSize());
		this.parameters = new TreeMap<Var, Parameter>();
		
		// Initialize initCount and init
		this.initCount = spec.initsSize();

		for(int i = 0; i < spec.initsSize(); i++) {
			this.inits.add(new Init(i, spec.init(i)));
		}

		// and usedParameters.
		this.usedParameters = new TreeSet<Var>();
		this.usedParameters.addAll(spec.parameters());

		for(Var v : spec.parameters()) {
			parameters.put(v,  new Parameter(v, spec.parameter(v)));
		}
		
		updateViews();
	}
	
	public void showError(String title, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
		builder.setMessage(msg).setTitle(title);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public View view() {
		return view;
	}
	
	/** Creates a Specification (out of which we can create a function) out
	 * of the data inside this ArrayAdapter
	 * @return
	 */
	public Specification acceptAndReturn() {
		Log.v(TAG, "acceptAndReturn");

		// First part: Accept input
		if(!fn.acceptInput()) return null;
		
		for(int i = 0; i < initCount; i++) {
			if(!inits.get(i).acceptInput()) return null;
		}
		
		for(Var v : usedParameters) {
			if(!parameters.get(v).acceptInput()) return null;
		}

		// Second part: Accept input.
		Labelled<Expr> f = fn.expr;
		ArrayList<Labelled<Expr>> is = new ArrayList<Labelled<Expr>>(inits.size());
		Map<Var, Labelled<Cplx>> ps = new TreeMap<Var, Labelled<Cplx>>();

		// add data
		for(int i = 0; i < initCount; i++) {
			is.add(inits.get(i).expr);
		}

		for(Var v : usedParameters) {
			ps.put(v, parameters.get(v).value);
		}

		return new Specification(f, is, ps);
	}	
	
	private void updateViews() {
		Log.v(TAG, "updateView()");
		
		// add view for funtion
		// Inflate new view
		int index = 0;

		if(hasFunction) {
			updateView(index ++, fn);
			
			for(int i = 0; i < initCount; i++) {
				updateView(index ++, inits.get(i));
			}
		}

		for(Var v : usedParameters) {
			updateView(index ++, parameters.get(v));
		}
		
		if(hasFunction) {
			// Remove views that are not needed anymore
			while(viewCountInLayout > index) {
				viewCountInLayout--;
				layout.removeViewAt(viewCountInLayout);
			}
		}
	}
	
	private View updateView(int index, PackedExpr expr) {
		assert index <= itemViews.size();
		assert index <= viewCountInLayout;
		
		View itemView;
		
		boolean isNewView = index == itemViews.size();
		
		if(isNewView) {
			Log.v(TAG, "Creating new view at index " + index + " = " + expr);
			// Create new view
			LayoutInflater inflater = ((Activity) view.getContext()).getLayoutInflater();
			itemView = inflater.inflate(R.layout.function_parameter_item, layout, false);
			
			itemViews.add(itemView);			
		} else {
			itemView = itemViews.get(index);
		}
		
		TextView label = (TextView) itemView.findViewById(R.id.functionItemLabel);
		EditText editor = (EditText) itemView.findViewById(R.id.functionItemEditor);

		// Attach PackedExpr to listener.
		EditorListener listener;
		
		if(isNewView) {
			// Create new listener if necessary
			listener = new EditorListener();
			
			editor.addTextChangedListener(listener);
			editor.setOnEditorActionListener(listener);
			editor.setTag(listener);
		} else {
			listener = (EditorListener) editor.getTag();
		}
		
		listener.setExpr(expr);
		
		// and update text.
		label.setText(expr.label());
		editor.setText(expr.input());
		
		if(index == viewCountInLayout) {
			// Add view to layout
			layout.addView(itemView, index);
			viewCountInLayout++;
		}
		
		return itemView;
	}
	
	/**
	 * @return true if the number of inits or parameters changed
	 */
	private boolean updateInits() {
		Log.v(TAG, "updateInits()");

		int initCount = fn.expr.get().maxIndexZ();

		boolean initCountChanged = initCount != this.initCount;
		
		this.initCount = initCount;
		
		while(inits.size() < initCount) {
			// Add new inits if neccesary.
			inits.add(new Init(inits.size(), new Labelled<Expr>(new Num(0), "0")));
		}

		boolean usedParametersChanged = updateParameters();
		
		return initCountChanged || usedParametersChanged;
	}

	/**
	 * @return true if the number of parameters changed
	 */
	private boolean updateParameters() { 
		// Call this after changing init or function
		// Fetch all parameters from main and init, and update parameters-map
		Log.v(TAG, "updateParameters()");
		
		Set<Var> vars = fn.expr.get().parameters(new TreeSet<Var>());
		
		for(int i = 0; i < initCount; i++) {
			inits.get(i).expr.get().parameters(vars);
		}

		vars.removeAll(ExprCompiler.predefinedVars);

		// Remove parameters that do not occur in vars
		boolean parameterCountChanged = usedParameters.retainAll(vars);

		// Add new parameters
		for(Var var : vars) {
			if(usedParameters.add(var)) {
				// Added a new parameter
				parameterCountChanged = true;
				
				if(!parameters.containsKey(var)) {
					// It is also not in the map yet.
					Log.v(TAG, "adding parameter " + var);
					parameters.put(var, new Parameter(var, new Labelled<Cplx>(new Cplx(), "0")));
				}				
			}
		}
		
		return parameterCountChanged;
	}
	
	private class EditorListener implements TextWatcher, OnEditorActionListener  {
		PackedExpr expr;
		
		EditorListener() {
		}
		
		public void setExpr(PackedExpr expr) {
			this.expr = expr;
		}

		@Override
		public boolean onEditorAction(TextView view, int actionId, KeyEvent evt) {
			Log.v(TAG, "onEditorAction " + view.getTag() + " == " + actionId);
			// Done-button was clicked on an expression, so update it
			return expr.acceptInput(); // accept input
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			expr.setInput(s);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// ignore
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// ignore
		}
	}

	private abstract class PackedExpr {
		
		String input;
		
		PackedExpr(String input) {
			this.input = input;
		}
		
		String input() {
			return input;
		}
		
		void setInput(CharSequence input) {
			this.input = input.toString();
		}

		abstract boolean acceptInput();
		
		abstract String label();
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(label());
			sb.append(input());
			return sb.toString();
		}
	}

	private class Fn extends PackedExpr {

		Labelled<Expr> expr;
		
		Fn(Labelled<Expr> function) {
			super(function.label());
			this.expr = function;
		}

		boolean acceptInput() {
			// Check whether current input is equal to input in expr
			if(input.equals(expr.label())) {
				Log.v(TAG, "Already at last changes");
				return true;
			}

			Parser parser = Parser.parse(input);
			Expr e = parser.get();
			
			if(e != null) {

				if(parser.hasErrors()) {
					// Show warnings
					showError("Parser warnings", parser.getErrorMessage());
				}					

				this.expr = new Labelled<Expr>(e, input);
				
				if(FunctionInputView.this.updateInits()) FunctionInputView.this.updateViews();
				
				return true;
			} else {
				Log.w(TAG, "Function was NOT modified.");
				
				if(parser.hasErrors()) {
					showError("Could not set z(n+1)", parser.getErrorMessage());
				} else {
					Log.e(TAG, "Could not set init-value but parser did not report an error");
					showError("Could not set z(n+1)", "Unknown error - please file a bug");
				}
				
				return false;
			}
		}
		
		@Override
		String input() {
			return expr.label();
		}
		
		@Override
		String label() {
			return "z(n+1) = ";
		}
	}

	private class Init extends PackedExpr {

		Labelled<Expr> expr;
		int index;

		Init(int index, Labelled<Expr> init) {
			super(init.label());
			this.index = index;
			this.expr = init;
		}
		
		boolean acceptInput() {
			if(input.equals(expr.label())) {
				Log.v(TAG, "Already at last changes");
				return true;
			}

			Parser parser = Parser.parse(input);
			Expr e = parser.get();
			
			if(e != null) {
				if(e.maxIndexZ() - 1 > index) {
					showError("Could not set z(" + index + ")", "Expression tries to access z(" + e.maxIndexZ() + ")");
					Log.v(TAG, index + " init was NOT updated: " + e + " has too high z-count: " + e.maxIndexZ());
					return false;
				} else {
					this.expr = new Labelled<Expr>(e, input);
					Log.v(TAG, "Init was updated to " + e);

					if(parser.hasErrors()) {
						// Show warnings
						showError("Parser warnings", parser.getErrorMessage());
					}					

					if(FunctionInputView.this.updateParameters()) FunctionInputView.this.updateViews();
	
					return true;
				}
			} else {
				if(parser.hasErrors()) {
					showError("Could not set z(" + index + ")", parser.getErrorMessage());
				} else {
					Log.e(TAG, "Could not set init-value but parser did not report an error");
					showError("Could not set z(" + index + ")", "Unknown error - please file a bug");
				}
				
				return false;
			}
		}

		@Override
		String label() {
			return "z(" + index + ") = ";
		}
	}

	private class Parameter extends PackedExpr {

		Var id;
		Labelled<Cplx> value;

		Parameter(Var id, Labelled<Cplx> value) {
			super(value.label());
			this.id = id;
			this.value = value;
		}

		boolean acceptInput() {
			if(input.equals(value.label())) {
				Log.v(TAG, "Already at last changes");
				return true;
			}

			Parser parser = Parser.parse(input);
			Expr e = parser.get();
			
			if(e != null && e.isNum()) {
				value = new Labelled<Cplx>(e.eval(null), input);
				
				Log.v(TAG, id + " parameter was updated: " + e);
				
				if(parser.hasErrors()) {
					// Show warnings
					showError("Parser warnings", parser.getErrorMessage());
				}
	
				// no update since no new rows were added.				
				return true;
			}

			showError("Invalid Parameter", "Parameter must be numeric value");
			
			Log.w(TAG, "Only numbers can be parameters: " + e);
			
			return false;
		}
		
		@Override
		String label() {
			return id + " = ";
		}
	}
}