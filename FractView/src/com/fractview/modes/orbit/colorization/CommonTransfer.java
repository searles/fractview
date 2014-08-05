package com.fractview.modes.orbit.colorization;

public enum CommonTransfer implements Transfer {	
	None {
		@Override
		public float transfer(float value) {
			return value;
		}
	}, 
	Log { // Log + 1
		@Override
		public float transfer(float value) {
			// TODO: Should return value between 0 and 1 if value is between 0 and 1...
			return (float) (Math.log(value + 1.) / LOG_2);
		}
	};
	private static final double LOG_2 = Math.log(2);
}
