package at.fractview.modes.orbit.colorization;

import java.util.Scanner;

public class OrbitTransfer {
	private float min;
	private float max;
	private CommonTransfer transfer;
	
	public OrbitTransfer(float min, float max, CommonTransfer transfer) {
		this.min = min;
		this.max = max;
		
		this.transfer = transfer;
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
		
		sb.append(min).append(' ');
		sb.append(max).append(' ');
		sb.append(transfer);
		
		return sb.toString();
	}
	
	public static OrbitTransfer fromString(String s) {
		Scanner sc = new Scanner(s);
		
		float min = sc.nextFloat();
		float max = sc.nextFloat();
		
		CommonTransfer transfer = CommonTransfer.valueOf(sc.next());
		
		return new OrbitTransfer(min, max, transfer);
	}
}
