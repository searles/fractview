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
package com.fractview.tools;

public class Labelled<E> {
	private E e;
	private String label;
	
	@SuppressWarnings("unused")
	private Labelled() {} // For Gson
	
	public Labelled(E e, String label) {
		this.e = e;
		this.label = label;
	}
	
	public E get() {
		return e;
	}
	
	public String label() {
		return label;
	}
}