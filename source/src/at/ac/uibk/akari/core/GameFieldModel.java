package at.ac.uibk.akari.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.graphics.Point;
import at.ac.uibk.akari.core.annotations.JsonIgnorePermanent;

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

		public static CellState getBlockByNumber(int number) {
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
	 * List of lamps that are placed on the game-field
	 */
	@JsonIgnorePermanent
	private List<Point> lamps;

	/**
	 * Initialize the game-field with a given width and height. After this, the game-field is empty. Therefore the state of all cells on the game-field is set to BLANK. The size of the game-field has to be at least 1x1
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
		this.setLamps(null);
		this.clear();
	}

	/**
	 * Clear the whole field. After this method is called the state of all cells of the game-field is set to BLANK, which means that the game-field is complete empty (lamps are removed too).
	 */
	public synchronized void clear() {
		for (int posY = 0; posY < this.getHeight(); posY++) {
			for (int posX = 0; posX < this.getWidth(); posX++) {
				this.setCellState(posX, posY, CellState.BLANK);
			}
		}
		this.setLamps(null);
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
		if (!this.isFieldValid(posX, posY)) {
			throw new RuntimeException("Illegal cell-position " + posX + "," + posY + " for " + this.getWidth() + "x" + this.getHeight() + " game-field");
		}
		this.cells[posY][posX] = cellState;
	}
	
	/**
	 * Setting the state of the cell at the given position
	 * 
	 * @param cellState
	 *            New state of the cell
	 */
	public synchronized void setCellState(Point p, final CellState cellState) {
		setCellState(p.x, p.y, cellState);
	}
	
	

	/**
	 * Getting the state of the cell at the given position. Important is that this method only returns the basic-cell-state, which means that lamps are ignored (in this case the method returns BLANK)
	 * 
	 * @param location
	 *            Position of the cell, both coordinates starting from 0
	 * @return Current state of the cell
	 */
	public synchronized CellState getCellState(final Point location) {
		return this.getCellState(location.x, location.y);
	}

	/**
	 * Getting the state of the cell at the given position. Important is that this method only returns the basic-cell-state, which means that lamps are ignored (in this case the method returns BLANK)
	 * 
	 * @param posX
	 *            Horizontal position of the cell, starting from 0
	 * @param posY
	 *            Vertical position of the cell, starting from 0
	 * @return Current state of the cell
	 */
	public synchronized CellState getCellState(final int posX, final int posY) {
		if (!this.isFieldValid(posX, posY)) {
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

	/**
	 * Clears the lamps of the field. After this method is called the model will represent a puzzle without lamps
	 * 
	 */
	public synchronized void clearLamps() {
		this.setLamps(null);
	}

	/**
	 * Place a list of lamps on the game-field. Calling this method, will discard all old lamps. Passing an empty-list or NULL to this method, will delete all lamps.
	 * 
	 * @param lamps
	 *            Position of the cell, both coordinates starting from 0
	 */
	public synchronized void setLamps(final List<Point> lamps) {
		if (lamps == null) {
			this.lamps = new ArrayList<Point>();
		} else {
			for (Point point : lamps) {
				if (!this.isFieldValid(point.x, point.y)) {
					throw new RuntimeException("Illegal cell-position " + point.x + "," + point.y + " for " + this.getWidth() + "x" + this.getHeight() + " game-field");
				}
			}
			this.lamps = lamps;
		}
	}

	/**
	 * Check if the given cell position is valid for this game-field (not out of range)
	 * 
	 * @param posX
	 *            Horizontal position of the cell, starting from 0
	 * @param posY
	 *            Vertical position of the cell, starting from 0
	 * 
	 * @return True if the cell-position is valid, otherwise false
	 */
	private boolean isFieldValid(final int posX, final int posY) {
		if (posX < 0 || posY < 0 || posX > this.getWidth() - 1 || posY > this.getHeight() - 1) {
			return false;
		}
		return true;
	}

	/**
	 * Getting the list of the location of all lamps, that where placed on the game-field. The minimal location for a lamp is 0,0. The returned list is a deep-copy of the actual list of lamps on the game-field. If there are not placed any lamps on
	 * the game-field, the method will return an empty list (never returns null)
	 * 
	 * @return Cloned locations of lamps
	 */
	public synchronized List<Point> getLamps() {
		if (this.lamps == null) {
			this.lamps = new ArrayList<Point>();
		}
		List<Point> clone = new ArrayList<Point>();
		for (Point lamp : this.lamps) {
			clone.add(new Point(lamp));
		}
		return clone;
	}

	/**
	 * Returns true if there is placed a lamp at the given cell on the game-field
	 * 
	 * @param location
	 *            Position of the cell, both coordinates starting from 0
	 * 
	 * @return True if there is a cell at the given position, otherwise false
	 */
	public synchronized boolean isLampAt(final Point location) {
		return this.isLampAt(location.x, location.y);
	}

	/**
	 * Returns true if there is placed a lamp at the given cell on the game-field
	 * 
	 * @param posX
	 *            Horizontal position of the cell, starting from 0
	 * @param posY
	 *            Vertical position of the cell, starting from 0
	 * 
	 * @return True if there is a cell at the given position, otherwise false
	 */
	public synchronized boolean isLampAt(final int posX, final int posY) {
		if (!this.isFieldValid(posX, posY)) {
			throw new RuntimeException("Illegal cell-position " + posX + "," + posY + " for " + this.getWidth() + "x" + this.getHeight() + " game-field");
		}
		for (Point lamp : this.lamps) {
			if (lamp.x == posX && lamp.y == posY) {
				return true;
			}
		}
		return false;
	}

	@Override
	public synchronized Object clone() {
		GameFieldModel clone = new GameFieldModel(this.getWidth(), this.getHeight());
		for (int posX = 0; posX < this.getWidth(); posX++) {
			for (int posY = 0; posY < this.getHeight(); posY++) {
				clone.setCellState(posX, posY, this.getCellState(posX, posY));
			}
		}
		clone.setLamps(this.getLamps());
		return clone;
	}

	/**
	 * Setting a lamp at the given cell-position of the game-field. If it is not possible to place a lamp at the given position (cell-state was not BLANK), the method will return false. This condition does not affect cells, that already contain a
	 * lamp. In this case the method will return true
	 * 
	 * @param posX
	 *            Horizontal position of the cell, starting from 0
	 * @param posY
	 *            Vertical position of the cell, starting from 0
	 * 
	 * @return True if it was possible to place the lamp at the given position, otherwise false
	 */
	public synchronized boolean setLampAt(final int posX, final int posY) {
		if (!this.isFieldValid(posX, posY)) {
			throw new RuntimeException("Illegal cell-position " + posX + "," + posY + " for " + this.getWidth() + "x" + this.getHeight() + " game-field");
		}
		if (!this.getCellState(posX, posY).equals(CellState.BLANK)) {
			return false;
		}

		if (!this.isLampAt(posX, posY)) {
			this.lamps.add(new Point(posX, posY));
		}
		return true;
	}

	public ArrayList<Point> getNeightbors(Point p, CellState state) {
		ArrayList<Point> list = new ArrayList<Point>(4);

		if ((p.x + 1) < this.getWidth() && (getCellState(p.x + 1, p.y).equals(state) || state == null))
			list.add(new Point(p.x + 1, p.y));

		if ((p.x - 1) >= 0 && (getCellState(p.x - 1, p.y).equals(state) || state == null))
			list.add(new Point(p.x - 1, p.y));

		if ((p.y + 1) < this.getHeight() && (getCellState(p.x, p.y + 1).equals(state) || state == null))
			list.add(new Point(p.x, p.y + 1));

		if ((p.y - 1) >= 0 && (getCellState(p.x , p.y - 1).equals(state) || state == null))
			list.add(new Point(p.x, p.y - 1));

		return list;

	}

	public ArrayList<Point> getLampNeightbors(Point p) {
		ArrayList<Point> list = new ArrayList<Point>(4);

		
		if ((p.x + 1) < this.getWidth() && isLampAt(p.x + 1, p.y))
			list.add(new Point(p.x + 1, p.y));

		if ((p.x - 1) >= 0 && isLampAt(p.x - 1, p.y))
			list.add(new Point(p.x - 1, p.y));

		if ((p.y + 1) < this.getHeight() && isLampAt(p.x, p.y + 1))
			list.add(new Point(p.x, p.y + 1));

		if ((p.y - 1) >= 0 && isLampAt(p.x, p.y - 1))
			list.add(new Point(p.x, p.y - 1));

		return list;

	}

	/**
	 * Setting a lamp at the given cell-position of the game-field. If it is not possible to place a lamp at the given position (cell-state was not BLANK), the method will return false. This condition does not affect cells, that already contain a
	 * lamp. In this case the method will return true.
	 * 
	 * @param location
	 *            Position of the cell, both coordinates starting from 0
	 * 
	 * @return True if it was possible to place the lamp at the given position, otherwise false
	 */
	public synchronized boolean setLampAt(final Point location) {
		return this.setLampAt(location.x, location.y);
	}

	public synchronized boolean removeLampAt(final Point location) {
		return this.lamps.remove(location);
	}
}
