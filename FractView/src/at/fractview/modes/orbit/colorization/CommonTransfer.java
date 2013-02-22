package at.fractview.modes.orbit.colorization;

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
			return (float) Math.log(value + 1f);
		}
	};
}
