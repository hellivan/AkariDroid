package at.ac.uibk.akari.core;

/**
 * Class that implements the model of an AKARI-game-field
 */
public class GameFieldModel {

	/**
	 * Enumeration that represents all possible states of a game-field-cell
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
		 * A cell that represents a lamp
		 */
		LAMP,
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
		BLOCK4,
	}

	/**
	 * Arrays that represents the cells of the game-field
	 */
	private CellState[][] cells;

	/**
	 * Initialize the game-field with a given width and height. After this, the
	 * game-field is empty. Therefore the state of all cells on the game-field
	 * is set to BLANK. The size of the game-field has to be at least 1x1
	 * 
	 * @param width
	 *            Width of the game-field
	 * @param height
	 *            Height of the game-field
	 */
	public GameFieldModel(final int width, final int height) {
		if (width < 1 || height < 1) {
			throw new RuntimeException("Invalid game-field-size " + width + "x" + height);
		}
		this.cells = new CellState[height][width];
		this.clear();
	}

	/**
	 * Clear the whole field. After this method is called the state of all cells
	 * of the game-field is set to BLANK
	 */
	public void clear() {
		for (int posY = 0; posY < this.getHeight(); posY++) {
			for (int posX = 0; posX < this.getWidth(); posX++) {
				this.setCellState(posX, posY, CellState.BLANK);
			}
		}
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
	public void setCellState(final int posX, final int posY, final CellState cellState) {
		if (posX < 0 || posY < 0 || posX > this.getWidth() || posY > this.getHeight()) {
			throw new RuntimeException("Illegal cell-position " + posX + "," + posY + " for " + this.getWidth() + "x" + this.getHeight() + " game-field");
		}
		this.cells[posY][posX] = cellState;
	}

	/**
	 * Getting the state of the cell at the given position
	 * 
	 * @param posX
	 *            Horizontal position of the cell, starting from 0
	 * @param posY
	 *            Vertical position of the cell, starting from 0
	 * @return Current state of the cell
	 */
	public CellState getCellState(final int posX, final int posY) {
		if (posX < 0 || posY < 0 || posX > this.getWidth() || posY > this.getHeight()) {
			throw new RuntimeException("Illegal cell-position " + posX + "," + posY + " for " + this.getWidth() + "x" + this.getHeight() + " game-field");
		}
		return this.cells[posY][posX];
	}

	/**
	 * Getting the with of the game-field
	 * 
	 * @return Width of the game-field
	 */
	public int getWidth() {
		return this.cells[0].length;
	}

	/**
	 * Getting the height of the game-field
	 * 
	 * @return Height of the game-field
	 */
	public int getHeight() {
		return this.cells.length;
	}
}
