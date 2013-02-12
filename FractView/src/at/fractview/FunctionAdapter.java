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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import android.app.Activity;
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
public class FunctionAdapter {

	private static final String TAG = "FunctionAdapter";
	
	public static FunctionAdapter create(Activity activity, Specification spec) {
		// TODO
		LayoutInflater inflater = activity.getLayoutInflater();
		View v = inflater.inflate(R.layout.function_parameters, null);
		
		return new FunctionAdapter(v, spec);
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
	
	// TODO: change to context and layout.
	private FunctionAdapter(View view, Specification spec) {
		
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
		
		updateView();
	}
	
	public View view() {
		return view;
	}
	
	private void updateView() {
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
	
	/** Creates a Specification (out of which we can create a function) out
	 * of the data inside this ArrayAdapter
	 * @return
	 */
	public Specification spec() {
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
	
	public void acceptAllInput() {
		Log.v(TAG, "acceptAllInput");

		fn.acceptInput();
		
		for(int i = 0; i < initCount; i++) {
			inits.get(i).acceptInput();
		}
		
		for(Var v : usedParameters) {
			parameters.get(v).acceptInput();
		}
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
			
			// TODO: What to do with errors?
			// TODO: Test in particular diff of non-diff functions.
			
			if(e != null) {
				Log.v(TAG, "Function was modified: " + input + " -> " + e);
				
				this.expr = new Labelled<Expr>(e, input);
				
				if(FunctionAdapter.this.updateInits()) FunctionAdapter.this.updateView();
				
				return true;
			} else {
				Log.w(TAG, "Function was NOT modified.");
				// TODO: Error message!
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
					// TODO Alert
					Log.v(TAG, index + " init was NOT updated: " + e + " has too high z-count: " + e.maxIndexZ());
					return false;
				} else {
					this.expr = new Labelled<Expr>(e, input);
					Log.v(TAG, "Init was updated to " + e);
	
					if(FunctionAdapter.this.updateParameters()) FunctionAdapter.this.updateView();
	
					return true;
				}
			} else {
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
	
				// no update since no new rows were added.				
				return true;
			}

			// TODO: Error
			Log.v(TAG, "Only numbers can be parameters: " + e);
			
			return false;
		}
		
		@Override
		String label() {
			return id + " = ";
		}
	}
}