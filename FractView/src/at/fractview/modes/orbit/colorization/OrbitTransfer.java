package at.fractview.modes.orbit.colorization;

import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class OrbitTransfer {
	private boolean normalize;
	
	private float min;
	private float max;
	
	private CommonTransfer transfer;
	
	public OrbitTransfer(boolean normalize, float min, float max, CommonTransfer transfer) {
		this.normalize = normalize;
		
		this.min = min;
		this.max = max;
		
		this.transfer = transfer;
	}
	
	public boolean normalize() {
		return normalize;
	}
	
	public float min() {
		return min;
	}
	
	public float max() {
		return max;
	}
	
	public CommonTransfer transfer() {
		return transfer;
	}
	
	public float value(float f) {
		// TODO: Should it be the other way?
		// First transfer, then normalize
		float f1 = transfer.transfer(f);
		return (f1 - min) / (max - min);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(normalize).append(' ');
		sb.append(min).append(' ');
		sb.append(max).append(' ');
		sb.append(transfer);
		
		return sb.toString();
	}
	
	public static OrbitTransfer fromString(String s) throws NoSuchElementException, InputMismatchException {
		Scanner sc = new Scanner(s);
		
		boolean normalize = sc.nextBoolean();
		float min = sc.nextFloat();
		float max = sc.nextFloat();
		
		CommonTransfer transfer = CommonTransfer.valueOf(sc.next());
		
		return new OrbitTransfer(normalize, min, max, transfer);
	}
}
