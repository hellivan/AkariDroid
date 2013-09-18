package at.ac.uibk.akari.common.view;

public class Insets {

	private float north;
	private float south;
	private float east;
	private float west;

	public Insets(final float north, final float west, final float south, final float east) {
		super();
		this.north = north;
		this.west = west;
		this.south = south;
		this.east = east;
	}

	public float getNorth() {
		return this.north;
	}

	public float getSouth() {
		return this.south;
	}

	public float getEast() {
		return this.east;
	}

	public float getWest() {
		return this.west;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + Float.floatToIntBits(this.east);
		result = (prime * result) + Float.floatToIntBits(this.north);
		result = (prime * result) + Float.floatToIntBits(this.south);
		result = (prime * result) + Float.floatToIntBits(this.west);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		Insets other = (Insets) obj;
		if (Float.floatToIntBits(this.east) != Float.floatToIntBits(other.east)) {
			return false;
		}
		if (Float.floatToIntBits(this.north) != Float.floatToIntBits(other.north)) {
			return false;
		}
		if (Float.floatToIntBits(this.south) != Float.floatToIntBits(other.south)) {
			return false;
		}
		if (Float.floatToIntBits(this.west) != Float.floatToIntBits(other.west)) {
			return false;
		}
		return true;
	}

}
