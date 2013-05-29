package at.ac.uibk.akari.solver;

import android.graphics.Point;

public class GameFieldVarManager {

	public enum VarBlocks {
		LAMP(0), LIGHT(1), BARRIER(2), BLOCK0(3), BLOCK1(4), BLOCK2(5), BLOCK3(6), BLOCK4(7), LEFTRAY(8), UPRAY(9), RIGHTRAY(10), DOWNRAY(11),PLACEABLE(12);

		private int value;

		private VarBlocks(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static VarBlocks getBlockN(int n) {
			switch (n) {
			case 0:
				return BLOCK0;
			case 1:
				return BLOCK1;
			case 2:
				return BLOCK2;
			case 3:
				return BLOCK3;
			case 4:
				return BLOCK4;

			default:
				return null;
			}
		}

		public static VarBlocks getEnumByValue(int value) {

			for (VarBlocks b : VarBlocks.values()) {
				if (b.getValue() == value)
					return b;
			}

			return null;

		}
	}

	private int width;
	private int height;

	private int blocksize;

	private final int CONSTANT_COUNT = 2;

	public GameFieldVarManager(int width, int height) {
		this.width = width;
		this.height = height;
		this.blocksize = width * height;
	}

	public int lastVar() {
		return getVar(VarBlocks.UPRAY.value, width - 1, height - 1);
	}

	public int falseVar() {
		return 1;
	}

	public int trueVar() {
		return 2;
	}

	public int getVar(int block, int x, int y) {
		if (x >= this.width || y >= this.height || x < 0 || y < 0) {
			return falseVar();
		}

		return x + y * this.width + blocksize * block + CONSTANT_COUNT + 1;
	}

	public int reverseVarBlockNum(int variable) {
		if (variable <= CONSTANT_COUNT)
			return -1;

		return (variable - CONSTANT_COUNT - 1) / blocksize;
	}

	public VarBlocks reverseVarBlock(int variable) {
		return VarBlocks.getEnumByValue(reverseVarBlockNum(variable));
	}

	public Point reverseVarPoint(int variable) {
		int block = reverseVarBlockNum(variable);

		Point res = new Point((variable - CONSTANT_COUNT - 1 - block * blocksize) % this.width, (variable - CONSTANT_COUNT - 1 - block * blocksize) / this.width);

		return res;
	}

	public int lampAt(final Point location) {
		return this.lampAt(location.x, location.y);
	}

	public int lampAt(int x, int y) {
		return getVar(VarBlocks.LAMP.getValue(), x, y);
	}

	public int lightAt(final Point location) {
		return this.lightAt(location.x, location.y);
	}

	public int lightAt(int x, int y) {
		return getVar(VarBlocks.LIGHT.getValue(), x, y);
	}

	public int barrierAt(final Point location) {
		return this.barrierAt(location.x, location.y);
	}

	public int barrierAt(int x, int y) {
		return getVar(VarBlocks.BARRIER.getValue(), x, y);
	}

	public int blockAt(int blockNr, final Point location) {
		return this.blockAt(blockNr, location.x, location.y);
	}

	public int blockAt(int blockNr, int x, int y) {
		return getVar(VarBlocks.getBlockN(blockNr).getValue(), x, y);
	}

	public int blankAt(final Point location) {
		return this.blankAt(location.x, location.y);
	}

	public int blankAt(int x, int y) {
		//Blank is the same as Light
		return getVar(VarBlocks.LIGHT.getValue(), x, y);
	}

	public int leftRayAt(int x, int y) {
		return getVar(VarBlocks.LEFTRAY.getValue(), x, y);
	}

	public int rightRayAt(int x, int y) {
		return getVar(VarBlocks.RIGHTRAY.getValue(), x, y);
	}

	public int upRayAt(int x, int y) {
		return getVar(VarBlocks.UPRAY.getValue(), x, y);
	}

	public int downRayAt(int x, int y) {
		return getVar(VarBlocks.DOWNRAY.getValue(), x, y);
	}
	
	public int placeableAt(int x, int y) {
		return getVar(VarBlocks.PLACEABLE.getValue(), x, y);
	}

}
