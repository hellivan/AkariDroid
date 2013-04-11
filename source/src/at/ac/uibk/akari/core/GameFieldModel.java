package at.ac.uibk.akari.core;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;
import at.ac.uibk.akari.core.Puzzle.CellState;
import at.ac.uibk.akari.core.annotations.JsonIgnorePermanent;

/**
 * Class that implements the model of an AKARI-game-field
 */
public class GameFieldModel {

	/**
	 * Puzzle for this gamefield
	 */
	private Puzzle puzzle;

	/**
	 * List of lamps that are placed on the game-field
	 */
	@JsonIgnorePermanent
	private List<Point> lamps;

	/**
	 * Initialize an new empty game-field with a given width and height. After
	 * this, the game-field is completely empty (all cells are empty, no lamps
	 * are on the game-field, ...). The size of the game-field has to be at
	 * least 1x1
	 * 
	 * @param width
	 *            Width of the game-field
	 * @param height
	 *            Height of the game-field
	 */
	public GameFieldModel(final int width, final int height) {
		this(new Puzzle(width, height));
	}

	/**
	 * Initialize the game-field with a given puzzle. Apart from that cells that
	 * are set in the puzzle, the game-field is empty (no lamps, marks, ... are
	 * on the game-field)
	 * 
	 * @param puzzle
	 *            Puzzle that is used as base for the game-field
	 */
	public GameFieldModel(final Puzzle puzzle) {
		this.puzzle = puzzle;
		this.clear();
	}

	/**
	 * Clear the whole field. After this method is called the state of all cells
	 * of the game-field is set to BLANK, which means that the game-field is
	 * complete empty (lamps are removed too).
	 */
	public synchronized void clear() {
		this.clearLamps();
	}

	/**
	 * Getting the with of the game-field
	 * 
	 * @return Width of the game-field
	 */
	public int getWidth() {
		return this.puzzle.getWidth();
	}

	/**
	 * Getting the height of the game-field
	 * 
	 * @return Height of the game-field
	 */
	public int getHeight() {
		return this.puzzle.getHeight();
	}

	/**
	 * Clears the lamps of the field. After this method is called the model will
	 * represent a puzzle without lamps
	 * 
	 */
	public synchronized void clearLamps() {
		this.setLamps(null);
	}

	/**
	 * Place a list of lamps on the game-field. Calling this method, will
	 * discard all old lamps. Passing an empty-list or NULL to this method, will
	 * delete all lamps.
	 * 
	 * @param lamps
	 *            Position of the cell, both coordinates starting from 0
	 */
	public synchronized void setLamps(final List<Point> lamps) {
		if (lamps == null) {
			this.lamps = new ArrayList<Point>();
		} else {
			for (Point location : lamps) {
				if (!this.isCellEmpty(location, true)) {
					throw new RuntimeException("Cant't set lamp at cell-position " + location + " because the cell is not empty");
				}
			}
			this.lamps = lamps;
		}
	}

	/**
	 * Setting a lamp at the given cell-position of the game-field. If it is not
	 * possible to place a lamp at the given position (cell-state was not
	 * BLANK), the method will return false. This condition does not affect
	 * cells, that already contain a lamp. In this case the method will return
	 * true
	 * 
	 * @param posX
	 *            Horizontal position of the cell, starting from 0
	 * @param posY
	 *            Vertical position of the cell, starting from 0
	 * 
	 * @return True if it was possible to place the lamp at the given position,
	 *         otherwise false
	 */
	public synchronized boolean setLampAt(final int posX, final int posY) {
		if (this.isCellEmpty(posX, posY, false)) {
			if (this.lamps == null) {
				this.lamps = new ArrayList<Point>();
			}
			this.lamps.add(new Point(posX, posY));
			return true;
		}
		if (this.isLampAt(posX, posY)) {
			return true;

		}
		return false;
	}

	/**
	 * Getting the list of the location of all lamps, that where placed on the
	 * game-field. The minimal location for a lamp is 0,0. The returned list is
	 * a deep-copy of the actual list of lamps on the game-field. If there are
	 * not placed any lamps on the game-field, the method will return an empty
	 * list (never returns null)
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
	 * Returns true if there is placed a lamp at the given cell on the
	 * game-field
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
	 * Returns true if there is placed a lamp at the given cell on the
	 * game-field
	 * 
	 * @param posX
	 *            Horizontal position of the cell, starting from 0
	 * @param posY
	 *            Vertical position of the cell, starting from 0
	 * 
	 * @return True if there is a cell at the given position, otherwise false
	 */
	public synchronized boolean isLampAt(final int posX, final int posY) {
		if (!this.puzzle.isCellValid(posX, posY)) {
			throw new RuntimeException("Illegal cell-position " + posX + "," + posY + " for " + this.getWidth() + "x" + this.getHeight() + " game-field");
		}
		if (this.lamps != null) {
			for (Point lamp : this.lamps) {
				if (lamp.x == posX && lamp.y == posY) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public synchronized Object clone() {
		Puzzle puzzleClone = (Puzzle) this.puzzle.clone();
		GameFieldModel gamefieldClone = new GameFieldModel(puzzleClone);
		gamefieldClone.setLamps(this.getLamps());
		return gamefieldClone;
	}

	public ArrayList<Point> getLampNeightbors(final Point location) {
		ArrayList<Point> list = new ArrayList<Point>(4);

		if ((location.x + 1) < this.getWidth() && this.isLampAt(location.x + 1, location.y)) {
			list.add(new Point(location.x + 1, location.y));
		}

		if ((location.x - 1) >= 0 && this.isLampAt(location.x - 1, location.y)) {
			list.add(new Point(location.x - 1, location.y));
		}

		if ((location.y + 1) < this.getHeight() && this.isLampAt(location.x, location.y + 1)) {
			list.add(new Point(location.x, location.y + 1));
		}

		if ((location.y - 1) >= 0 && this.isLampAt(location.x, location.y - 1)) {
			list.add(new Point(location.x, location.y - 1));
		}

		return list;

	}

	/**
	 * Setting a lamp at the given cell-position of the game-field. If it is not
	 * possible to place a lamp at the given position (cell-state was not
	 * BLANK), the method will return false. This condition does not affect
	 * cells, that already contain a lamp. In this case the method will return
	 * true.
	 * 
	 * @param location
	 *            Position of the cell, both coordinates starting from 0
	 * 
	 * @return True if it was possible to place the lamp at the given position,
	 *         otherwise false
	 */
	public synchronized boolean setLampAt(final Point location) {
		return this.setLampAt(location.x, location.y);
	}

	/**
	 * Remove a lamp at the given cell-position of the game-field. If there was
	 * not placed a lamp before, the method will return false, otherwise true
	 * 
	 * @param location
	 *            Position of the cell, both coordinates starting from 0
	 * 
	 * @return True if the lamp was removed from the given position, otherwise
	 *         false
	 */
	public synchronized boolean removeLampAt(final Point location) {
		if (this.lamps == null) {
			return false;
		}
		return this.lamps.remove(location);
	}

	public boolean isCellEmpty(final int posX, final int posY, final boolean ignoreLamps) {
		if (ignoreLamps) {
			return this.getPuzzleCellState(posX, posY).equals(CellState.BLANK);
		}
		return this.getPuzzleCellState(posX, posY).equals(CellState.BLANK) && !this.isLampAt(posX, posY);
	}

	public boolean isCellEmpty(final Point location, final boolean ignoreLamps) {
		return this.isCellEmpty(location.x, location.y, ignoreLamps);
	}

	public CellState getPuzzleCellState(final Point location) {
		return this.getPuzzleCellState(location.x, location.y);
	}

	public CellState getPuzzleCellState(final int posX, final int posY) {
		return this.puzzle.getCellState(posX, posY);
	}

	public boolean isBarrierAt(final int posX, final int posY) {
		return this.getPuzzleCellState(posX, posY).equals(CellState.BARRIER);
	}

	public boolean isBlock0At(final int posX, final int posY) {
		return this.getPuzzleCellState(posX, posY).equals(CellState.BLOCK0);
	}

	public boolean isBlock1At(final int posX, final int posY) {
		return this.getPuzzleCellState(posX, posY).equals(CellState.BLOCK1);
	}

	public boolean isBlock2At(final int posX, final int posY) {
		return this.getPuzzleCellState(posX, posY).equals(CellState.BLOCK2);
	}

	public boolean isBlock3At(final int posX, final int posY) {
		return this.getPuzzleCellState(posX, posY).equals(CellState.BLOCK3);
	}

	public boolean isBlock4At(final int posX, final int posY) {
		return this.getPuzzleCellState(posX, posY).equals(CellState.BLOCK4);
	}
}
