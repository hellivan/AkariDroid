package at.ac.uibk.akari.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.graphics.Point;
import android.util.Log;
import at.ac.uibk.akari.core.GameFieldPoint.Type;
import at.ac.uibk.akari.core.Puzzle.CellState;

/**
 * Class that implements the model of an AKARI-game-field
 */
public class GameFieldModel {

	/**
	 * Puzzle for this game-field
	 */
	private Puzzle puzzle;

	/**
	 * List of points on the game-field that have a special meaning
	 */
	private Set<GameFieldPoint> gameFieldPoints;

	private int[][] lightRays;

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
		this.lightRays = new int[puzzle.getHeight()][puzzle.getWidth()];
	}

	/**
	 * Clear the whole field. After this method is called, all lamps, marks, ...
	 * are removed. The puzzle itself is not influenced by this method
	 */
	public synchronized void clear() {
		this.getGameFieldPoints().clear();
		this.lightRays = new int[this.puzzle.getHeight()][this.puzzle.getWidth()];
	}

	/**
	 * Getting the set of all points on the game-field, that do have a special
	 * meaning
	 * 
	 * @return Set of points
	 */
	private Set<GameFieldPoint> getGameFieldPoints() {
		if (this.gameFieldPoints == null) {
			this.gameFieldPoints = new HashSet<GameFieldPoint>();
		}
		return this.gameFieldPoints;
	}

	/**
	 * Getting the set of all points on the game-field, that do have a special
	 * meaning and do match the given type
	 * 
	 * @param type
	 *            Type of points, for that should be searched for
	 * @return Set of points
	 */
	private Set<Point> getGameFieldPoints(final Type type) {
		Set<Point> points = new HashSet<Point>();
		for (GameFieldPoint point : this.getGameFieldPoints()) {
			if (point.getType().equals(type)) {
				points.add(point.toPoint());
			}
		}
		return points;
	}

	private boolean addGameFieldPoint(final Type type, final int posX, final int posY) {
		if (type.equals(Type.LAMP)) {
			this.onLampChanged(posX, posY, true);
		}
		return this.getGameFieldPoints().add(new GameFieldPoint(type, new Point(posX, posY)));
	}

	/**
	 * Remove all points from the game-field, that do have a special
	 * meaning and do match the given type
	 * 
	 * @param type
	 *            Type of points, that should be removed or null if all points
	 *            should be removed
	 * 
	 * @param points
	 *            Position of the points that should be removed or null, if all
	 *            points should be removed
	 * 
	 * @return True if at least on of the points was removed otherwise false
	 */
	private boolean removeGameFieldPoints(final Type type, final Set<Point> points) {
		Set<GameFieldPoint> pointsToRemove = new HashSet<GameFieldPoint>();
		// search for points that should be removed
		for (GameFieldPoint point : this.getGameFieldPoints()) {
			if (((type == null) || point.getType().equals(type)) && ((points == null) || points.contains(point.toPoint()))) {
				pointsToRemove.add(point);
			}
		}

		boolean retValue = false;

		// removing points and update light-rays
		for (GameFieldPoint point : pointsToRemove) {
			boolean tmpValue = this.getGameFieldPoints().remove(point);
			if (tmpValue && point.getType().equals(Type.LAMP)) {
				this.onLampChanged(point.getX(), point.getY(), false);
			}
			retValue |= tmpValue;
		}

		return retValue;
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
	 * Place a set of lamps on the game-field. Calling this method, will
	 * discard all old lamps.
	 * 
	 * @param lamps
	 *            Position of the cell, both coordinates starting from 0
	 */
	public synchronized void setLamps(final Set<Point> lamps) {
		this.removeGameFieldPoints(Type.LAMP, null);
		// check if lamps can be placed
		for (Point location : lamps) {
			if (!this.isCellCompleteEmpty(location)) {
				throw new RuntimeException("Cant't set lamp at cell-position " + location + " because the cell is not empty");
			}
		}
		for (Point location : lamps) {
			this.setLampAt(location);
		}
	}

	/**
	 * Place a set of marks on the game-field. Calling this method, will
	 * discard all old marks.
	 * 
	 * @param lamps
	 *            Position of the cell, both coordinates starting from 0
	 */
	public synchronized void setMarks(final Set<Point> marks) {
		this.removeGameFieldPoints(Type.MARK, null);
		// check if lamps can be placed
		for (Point location : marks) {
			if (!this.isCellCompleteEmpty(location)) {
				throw new RuntimeException("Cant't set mark at cell-position " + location + " because the cell is not empty");
			}
		}
		for (Point location : marks) {
			this.setMarkAt(location);
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
		Point point = new Point(posX, posY);
		if (this.isCellCompleteEmpty(point)) {
			return this.addGameFieldPoint(Type.LAMP, posX, posY);
		}
		if (this.isLampAt(point)) {
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
	public synchronized Set<Point> getLamps() {
		return this.getGameFieldPoints(Type.LAMP);
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
	 * @return True if there is a lamp at the given position, otherwise false
	 */
	public synchronized boolean isLampAt(final int posX, final int posY) {
		for (Point lamp : this.getGameFieldPoints(Type.LAMP)) {
			if ((lamp.x == posX) && (lamp.y == posY)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if there is placed a mark at the given cell on the
	 * game-field
	 * 
	 * @param posX
	 *            Horizontal position of the cell, starting from 0
	 * @param posY
	 *            Vertical position of the cell, starting from 0
	 * 
	 * @return True if there is a mark at the given position, otherwise false
	 */
	public synchronized boolean isMarkAt(final int posX, final int posY) {
		for (Point mark : this.getGameFieldPoints(Type.MARK)) {
			if ((mark.x == posX) && (mark.y == posY)) {
				return true;
			}
		}
		return false;
	}

	public synchronized boolean isMarkAt(final Point location) {
		return this.isMarkAt(location.x, location.y);
	}

	private boolean isGameFieldPointAt(final Type type, final Point point) {
		for (GameFieldPoint gfPoint : this.getGameFieldPoints()) {
			if (((type == null) || gfPoint.getType().equals(type)) && (point.equals(gfPoint.toPoint()))) {
				return true;
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

		if (((location.x + 1) < this.getWidth()) && this.isLampAt(location.x + 1, location.y)) {
			list.add(new Point(location.x + 1, location.y));
		}

		if (((location.x - 1) >= 0) && this.isLampAt(location.x - 1, location.y)) {
			list.add(new Point(location.x - 1, location.y));
		}

		if (((location.y + 1) < this.getHeight()) && this.isLampAt(location.x, location.y + 1)) {
			list.add(new Point(location.x, location.y + 1));
		}

		if (((location.y - 1) >= 0) && this.isLampAt(location.x, location.y - 1)) {
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

	public synchronized boolean clearCellAt(final Point location) {
		return this.removeGameFieldPoints(null, new HashSet<Point>(Arrays.asList(location)));
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
		return this.removeGameFieldPoints(Type.LAMP, new HashSet<Point>(Arrays.asList(location)));
	}

	public synchronized boolean setMarkAt(final Point location) {
		if (this.isCellCompleteEmpty(location)) {
			return this.addGameFieldPoint(Type.MARK, location.x, location.y);
		}
		if (this.isMarkAt(location)) {
			return true;
		}
		return false;
	}

	public synchronized boolean removeMarkAt(final Point location) {
		return this.removeGameFieldPoints(Type.MARK, new HashSet<Point>(Arrays.asList(location)));
	}

	public void printLightRaysArray() {
		StringBuffer buffer = new StringBuffer("LightRays:\n");
		for (int posY = 0; posY < this.lightRays.length; posY++) {
			buffer.append(Arrays.toString(this.lightRays[posY]) + "\n");
		}
		Log.d(this.getClass().getName(), buffer.toString());
	}

	public boolean isCellEmpty(final int posX, final int posY) {
		return this.getPuzzleCellState(posX, posY).equals(CellState.BLANK);
	}

	public boolean isCellEmpty(final Point location) {
		return this.isCellEmpty(location.x, location.y);
	}

	public boolean isCellCompleteEmpty(final int posX, final int posY) {
		return this.isCellCompleteEmpty(new Point(posX, posY));
	}

	public boolean isCellCompleteEmpty(final Point point) {
		if (this.isCellEmpty(point) && (!this.isGameFieldPointAt(null, point))) {
			return true;
		} else {
			return false;
		}
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

	public Puzzle getPuzzle() {
		return (Puzzle) this.puzzle.clone();
	}

	private void onLampChanged(final int posX, final int posY, final boolean added) {

		int value = (added ? 1 : -1);

		// lamp lights its own position
		this.lightRays[posY][posX] += value;

		// lamps sends horizontal light-rays until a block or barrier blocks
		// them
		for (int lightX = posX + 1; lightX < this.getWidth(); lightX++) {
			if (!this.isCellEmpty(lightX, posY)) {
				break;
			}
			this.lightRays[posY][lightX] += value;
		}
		for (int lightX = posX - 1; lightX >= 0; lightX--) {
			if (!this.isCellEmpty(lightX, posY)) {
				break;
			}
			this.lightRays[posY][lightX] += value;
		}

		// lamps sends vertical light-rays until a block or barrier blocks
		// them
		for (int lightY = posY + 1; lightY < this.getHeight(); lightY++) {
			if (!this.isCellEmpty(posX, lightY)) {
				break;
			}
			this.lightRays[lightY][posX] += value;
		}
		for (int lightY = posY - 1; lightY >= 0; lightY--) {
			if (!this.isCellEmpty(posX, lightY)) {
				break;
			}
			this.lightRays[lightY][posX] += value;
		}
	}

	public boolean isCellLighted(final int posX, final int posY) {
		return this.lightRays[posY][posX] > 0;
	}

	public synchronized Set<Point> getMarks() {
		return this.getGameFieldPoints(Type.MARK);
	}
}
