package com.fractview.modes.orbit.colorization;

public class OrbitTransfer {
	private CommonTransfer transfer;
	private Stats stats;
	
	@SuppressWarnings("unused")
	private OrbitTransfer() {} // For GSon
	
	public OrbitTransfer(CommonTransfer transfer, Stats stats) {
		this.transfer = transfer;
		this.stats = stats; // Stats may be null
	}
	
	public boolean customStats() {
		return stats != null;
	}
	
	public float value(float value, Stats updateStats, Stats defaultStats) {
		float f = transfer.transfer(value);
		
		updateStats.nextValue(f);
		
		if(stats != null) {
			return stats.normalize(f);
		} else {
			return defaultStats.normalize(f);
		}
	}
	
	public Stats stats() {
		return stats;
	}

	public CommonTransfer transfer() {
		return transfer;
	}
	
	public static class Stats {
		private volatile float minValue;
		private volatile float maxValue;
		
		public Stats() {
			minValue = Float.POSITIVE_INFINITY;
			maxValue = Float.NEGATIVE_INFINITY;
		}
		
		public Stats(float minValue, float maxValue) {
			this.minValue = minValue;
			this.maxValue = maxValue;
		}
		
		public float minValue() {
			return minValue;
		}
		
		public float maxValue() {
			return maxValue;
		}
		
		public float normalize(float f) {
			return (f - minValue) / (maxValue - minValue);
		}
		
		public void nextValue(float v) {
			if(v < minValue) minValue = v;
			if(v > maxValue) maxValue = v;
		}
		
		public void update(Stats stats) {
			if(stats.minValue < this.minValue) {
				this.minValue = stats.minValue;
			}

			if(stats.maxValue > this.maxValue) {
				this.maxValue = stats.maxValue;
			}
		}
	}


}
