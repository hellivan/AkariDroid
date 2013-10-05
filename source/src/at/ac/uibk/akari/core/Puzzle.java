package at.ac.uibk.akari.core;

import java.util.ArrayList;
import java.util.Arrays;

import android.graphics.Point;
import at.ac.uibk.akari.common.view.MenuItem;

/**
 * Class that represents the base construction of a puzzle. It only contains the
 * cells and their properties (can't contain lamps, marks or other informations)
 */
public class Puzzle {

	/**
	 * Enumeration that represents all possible states of a puzzle
	 */
	public enum CellState {
		/**
		 * An empty cell
		 */
		BLANK,
		/**
		 * A cell that represents a barrier
		 */
		BARRIER,
		/**
		 * A cell that may not have lamps around
		 */
		BLOCK0,
		/**
		 * A cell that must have 1 lamp around
		 */
		BLOCK1,
		/**
		 * A cell that must have 2 lamp around
		 */
		BLOCK2,
		/**
		 * A cell that must have 3 lamp around
		 */
		BLOCK3,
		/**
		 * A cell that must have 4 lamp around
		 */
		BLOCK4;

		public static CellState getBlockByNumber(final int number) {
			switch (number) {
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
				break;
			}
			return null;
		}
	}

	/**
	 * Arrays that represents the cells of the game-field
	 */
	private CellState[][] cells;

	/**
	 * Difficulty for the puzzle
	 */
	private Difficulty difficulty;

	/**
	 * Name of the puzzle
	 */
	private String name;

	/**
	 * Initialize the puzzle with a given width and height. After this, the
	 * game-field is empty. Therefore the state of all cells on the game-field
	 * is set to BLANK. The size of the game-field has to be at least 1x1
	 * 
	 * @param width
	 *            Width of the puzzle
	 * @param height
	 *            Height of the puzzle
	 */
	public Puzzle(final int width, final int height) {
		if ((width < 1) || (height < 1)) {
			throw new RuntimeException("Invalid puzzle-size " + width + "x" + height);
		}
		this.cells = new CellState[height][width];
		this.clear();
	}

	/**
	 * Clear the whole puzzle. After calling this method, the state of all cells
	 * of the game-field is set to BLANK, which means that the puzzle is
	 * complete empty.
	 */
	public synchronized void clear() {
		for (int posY = 0; posY < this.getHeight(); posY++) {
			for (int posX = 0; posX < this.getWidth(); posX++) {
				this.setCellState(posX, posY, CellState.BLANK);
			}
		}
	}

	/**
	 * Getting the with of the puzzle
	 * 
	 * @return Width of the puzzle
	 */
	public int getWidth() {
		return this.cells[0].length;
	}

	/**
	 * Getting the height of the puzzle
	 * 
	 * @return Height of the puzzle
	 */
	public int getHeight() {
		return this.cells.length;
	}

	/**
	 * Setting the state of the cell at the given position
	 * 
	 * @param location
	 *            Position of the cell, both coordinates starting from 0
	 * 
	 * @param cellState
	 *            New state of the cell
	 */
	public synchronized void setCellState(final Point location, final CellState cellState) {
		this.setCellState(location.x, location.y, cellState);
	}

	/**
	 * Setting the state of the cell at the given position
	 * 
	 * @param posX
	 *            Horizontal position of the cell, starting from 0
	 * @param posY
	 *            Vertical position of the cell, starting from 0
	 * @param cellState
	 *            New state of the cell
	 */
	public synchronized void setCellState(final int posX, final int posY, final CellState cellState) {
		if (!this.isCellValid(posX, posY)) {
			throw new RuntimeException("Illegal cell-position " + posX + "," + posY + " for " + this.getWidth() + "x" + this.getHeight() + " puzzle");
		}
		this.cells[posY][posX] = cellState;
	}

	/**
	 * Getting the state of the cell at the given position
	 * 
	 * @param location
	 *            Position of the cell, both coordinates starting from 0
	 * 
	 * @return Current state of the cell
	 */
	public synchronized CellState getCellState(final Point location) {
		return this.getCellState(location.x, location.y);
	}

	/**
	 * Getting the state of the cell at the given position
	 * 
	 * @param posX
	 *            Horizontal position of the cell, starting from 0
	 * @param posY
	 *            Vertical position of the cell, starting from 0
	 * 
	 * @return Current state of the cell
	 */
	public synchronized CellState getCellState(final int posX, final int posY) {
		if (!this.isCellValid(posX, posY)) {
			throw new RuntimeException("Illegal cell-position " + posX + "," + posY + " for " + this.getWidth() + "x" + this.getHeight() + " game-field");
		}
		return this.cells[posY][posX];
	}

	/**
	 * Check if the given cell-position is valid for this puzzle (not out of
	 * range)
	 * 
	 * @param location
	 *            Position of the cell, both coordinates starting from 0
	 * 
	 * @return True if the cell-position is valid, otherwise false
	 */
	public boolean isCellValid(final Point location) {
		return this.isCellValid(location.x, location.y);
	}

	/**
	 * Check if the given cell-position is valid for this puzzle (not out of
	 * range)
	 * 
	 * @param posX
	 *            Horizontal position of the cell, starting from 0
	 * @param posY
	 *            Vertical position of the cell, starting from 0
	 * 
	 * @return True if the cell-position is valid, otherwise false
	 */
	public boolean isCellValid(final int posX, final int posY) {
		if ((posX < 0) || (posY < 0) || (posX > (this.getWidth() - 1)) || (posY > (this.getHeight() - 1))) {
			return false;
		}
		return true;
	}

	@Override
	public synchronized Object clone() {
		Puzzle clone = new Puzzle(this.getWidth(), this.getHeight());
		for (int posX = 0; posX < this.getWidth(); posX++) {
			for (int posY = 0; posY < this.getHeight(); posY++) {
				clone.setCellState(posX, posY, this.getCellState(posX, posY));
			}
		}
		clone.setDifficulty(this.getDifficulty());
		return clone;
	}

	public ArrayList<Point> getNeightbors(final Point location, final CellState state) {
		ArrayList<Point> list = new ArrayList<Point>(4);

		if (((location.x + 1) < this.getWidth()) && (this.getCellState(location.x + 1, location.y).equals(state) || (state == null))) {
			list.add(new Point(location.x + 1, location.y));
		}

		if (((location.x - 1) >= 0) && (this.getCellState(location.x - 1, location.y).equals(state) || (state == null))) {
			list.add(new Point(location.x - 1, location.y));
		}

		if (((location.y + 1) < this.getHeight()) && (this.getCellState(location.x, location.y + 1).equals(state) || (state == null))) {
			list.add(new Point(location.x, location.y + 1));
		}

		if (((location.y - 1) >= 0) && (this.getCellState(location.x, location.y - 1).equals(state) || (state == null))) {
			list.add(new Point(location.x, location.y - 1));
		}

		return list;

	}

	/**
	 * Getting the difficulty of the puzzle
	 * 
	 * @return Difficulty of the puzzle
	 */
	public Difficulty getDifficulty() {
		return this.difficulty;
	}

	/**
	 * Setting the difficulty of the puzzle
	 * 
	 * @param difficulty
	 *            Difficulty of the puzzle
	 */
	public void setDifficulty(final Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	/**
	 * Getting the name of the puzzle
	 * 
	 * @return Name of the puzzle
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Setting the name of the puzzle
	 * 
	 * @param name
	 *            Name of the puzzle
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Class that is used to describe the difficulty of a level
	 * 
	 */
	public enum Difficulty implements MenuItem {

		EASY("Easy"),

		MEDIUM("Medium"),

		HARD("Hard");

		private String text;

		private Difficulty(final String text) {
			this.text = text;
		}

		@Override
		public String getText() {
			return this.text;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + Arrays.deepHashCode(this.cells);
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
		Puzzle other = (Puzzle) obj;
		if (!Arrays.deepEquals(this.cells, other.cells)) {
			return false;
		}
		return true;
	}

}
