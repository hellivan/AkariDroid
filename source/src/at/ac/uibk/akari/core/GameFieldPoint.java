package at.ac.uibk.akari.core;

import android.graphics.Point;

public class GameFieldPoint {

	public enum Type {

		LAMP,

		MARK;
	}

	private Type type;
	private Point position;

	public GameFieldPoint(final Type type, final int x, final int y) {
		this(type, new Point(x, y));
	}

	public GameFieldPoint(final Type type, final Point position) {
		super();
		this.type = type;
		this.position = position;
	}

	public Type getType() {
		return this.type;
	}

	public int getX() {
		return this.position.x;
	}

	public int getY() {
		return this.position.y;
	}

	public Point toPoint() {
		return new Point(this.getX(), this.getY());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.position == null) ? 0 : this.position.hashCode());
		result = (prime * result) + ((this.type == null) ? 0 : this.type.hashCode());
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
		GameFieldPoint other = (GameFieldPoint) obj;
		if (this.position == null) {
			if (other.position != null) {
				return false;
			}
		} else if (!this.position.equals(other.position)) {
			return false;
		}
		if (this.type != other.type) {
			return false;
		}
		return true;
	}

}
