package at.fractview.tools;

public class Labelled<E> {
	private E e;
	private String label;
	
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