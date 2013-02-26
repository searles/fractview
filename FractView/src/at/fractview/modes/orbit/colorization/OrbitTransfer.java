package at.fractview.modes.orbit.colorization;


public class OrbitTransfer {
	private boolean normalize;
	
	private float min;
	private float max;
	
	private CommonTransfer transfer;
	
	@SuppressWarnings("unused")
	private OrbitTransfer() {} // For GSon
	
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
		//return transfer.transfer((f - min) / (max - min));
	}
}
