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
 */package com.fractview.dialogs;

import com.fractview.EscapeTimeFragment;
import com.fractview.ImageViewFragment;
import com.fractview.UnsafeImageEditor;
import com.fractview.inputviews.FunctionInputView;
import com.fractview.modes.AbstractImgCache;
import com.fractview.modes.orbit.EscapeTime;
import com.fractview.modes.orbit.EscapeTimeCache;
import com.fractview.modes.orbit.functions.Function;

import android.os.Bundle;
import android.view.View;

public class FunctionDialogFragment extends InputViewDialogFragment {
	
	private EscapeTimeFragment taskFragment;
	private FunctionInputView functionView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		taskFragment = (EscapeTimeFragment) getFragmentManager().findFragmentByTag(ImageViewFragment.TASK_TAG);
		super.onCreate(savedInstanceState);	
	}
	
	@Override
	protected View createView() {
		this.functionView = FunctionInputView.create(getActivity(), ((EscapeTime) taskFragment.prefs()).function());
		return functionView.view();
	}

	protected boolean acceptInput() {
		final Function fn = functionView.acceptAndReturn();
		
		if(fn != null) {
			taskFragment.modifyImage(new UnsafeImageEditor() {
				@Override
				public void edit(AbstractImgCache cache) {
					EscapeTimeCache ch = (EscapeTimeCache) cache;
					ch.newFunction(fn);
				}
			}, true);
			return true;
		} else {
			return false;
		}
	}
	
	protected String title() {
		return "Function";
	}
}
