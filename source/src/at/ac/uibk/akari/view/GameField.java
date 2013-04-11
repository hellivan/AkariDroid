package at.ac.uibk.akari.view;

import java.util.ArrayList;
import java.util.List;

import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.DrawType;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;
import at.ac.uibk.akari.core.GameFieldModel;
import at.ac.uibk.akari.listener.GameFieldDragEvent;
import at.ac.uibk.akari.listener.GameFieldInputListener;
import at.ac.uibk.akari.listener.GameFieldTouchEvent;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.view.Cell.State;

public class GameField extends Rectangle {

	private static final int CELL_WIDTH = 50;
	private static final int CELL_HEIGHT = 50;

	private GameFieldModel puzzle;
	private ListenerList listenerList;

	private VertexBufferObjectManager vertexBufferObjectManager;

	private List<Line> gameFieldLines;
	private Cell[][] gameFieldCells;

	private long lastMultiTouched;
	private Point lastDragPoint;

	public GameField(final float posX, final float posY, final int cellCountX, final int cellCountY, final VertexBufferObjectManager vertexBufferObjectManager) {
		this(posY, posY, vertexBufferObjectManager);
	}

	public GameField(final float posX, final float posY, final VertexBufferObjectManager vertexBufferObjectManager) {
		super(posX, posY, 0, 0, vertexBufferObjectManager, DrawType.STATIC);
		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.listenerList = new ListenerList();
		this.gameFieldLines = new ArrayList<Line>();
	}

	public void setPuzzle(final GameFieldModel puzzle) {
		this.puzzle = puzzle;
		this.setSize(this.puzzle.getWidth() * GameField.CELL_WIDTH, this.puzzle.getHeight() * GameField.CELL_HEIGHT);
		this.lastMultiTouched = 0;
		this.initfField();
		this.adaptFieldToModel();
	}

	private void initfField() {
		this.setColor(1, 1, 1, 0.85f);
		Color gridColor = new Color(0.5f, 0.5f, 0.5f, 1.0f);
		Color borderColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);

		this.addFieldCells();
		// this.addFieldLines(borderColor, gridColor, 5, 3);

	}

	public GameFieldModel getModel() {
		return this.puzzle;
	}

	private void addFieldCells() {
		// remove old cells if there were any before
		if (this.gameFieldCells != null) {
			for (int posY = 0; posY < this.gameFieldCells.length; posY++) {
				for (int posX = 0; posX < this.gameFieldCells[0].length; posX++) {
					this.detachChild(this.gameFieldCells[posY][posX]);
				}
			}
		}

		// adding new cells
		this.gameFieldCells = new Cell[this.getModel().getHeight()][this.getModel().getWidth()];

		for (int posY = 0; posY < this.getModel().getHeight(); posY++) {
			for (int posX = 0; posX < this.getModel().getWidth(); posX++) {
				this.gameFieldCells[posY][posX] = new Cell(posX * GameField.CELL_WIDTH, posY * GameField.CELL_HEIGHT, GameField.CELL_WIDTH, GameField.CELL_HEIGHT, this.vertexBufferObjectManager);
				this.attachChild(this.gameFieldCells[posY][posX]);
			}
		}

	}

	private void addFieldLines(final Color borderColor, final Color gridColor, final int borderWidth, final int gridWidth) {
		// Remove old lines if there were any before
		for (Line line : this.gameFieldLines) {
			this.detachChild(line);
		}

		// add vertical lines
		for (int cellX = 1; cellX < this.getModel().getWidth(); cellX++) {
			Line line = new Line(cellX * GameField.CELL_WIDTH, 0, cellX * GameField.CELL_WIDTH, this.getHeight(), this.vertexBufferObjectManager);
			line.setColor(gridColor);
			line.setLineWidth(gridWidth);
			this.gameFieldLines.add(line);
			this.attachChild(line);
		}

		// add horizontal lines
		for (int cellY = 1; cellY < this.getModel().getHeight(); cellY++) {
			Line line = new Line(0, cellY * GameField.CELL_HEIGHT, this.getWidth(), cellY * GameField.CELL_HEIGHT, this.vertexBufferObjectManager);
			line.setColor(gridColor);
			line.setLineWidth(gridWidth);
			this.gameFieldLines.add(line);
			this.attachChild(line);
		}

		// Adding border for the game-field
		Line firstLineVertical = new Line(0, 0, 0, this.getHeight(), this.vertexBufferObjectManager);
		firstLineVertical.setColor(borderColor);
		firstLineVertical.setLineWidth(borderWidth);
		this.gameFieldLines.add(firstLineVertical);
		this.attachChild(firstLineVertical);
		Line lastLineVertical = new Line(this.getModel().getWidth() * GameField.CELL_WIDTH, 0, this.getModel().getWidth() * GameField.CELL_WIDTH, this.getHeight(), this.vertexBufferObjectManager);
		lastLineVertical.setColor(borderColor);
		lastLineVertical.setLineWidth(borderWidth);
		this.gameFieldLines.add(lastLineVertical);
		this.attachChild(lastLineVertical);
		Line firstLineHorizontal = new Line(0, 0, this.getWidth(), 0, this.vertexBufferObjectManager);
		firstLineHorizontal.setColor(borderColor);
		firstLineHorizontal.setLineWidth(borderWidth);
		this.gameFieldLines.add(firstLineHorizontal);
		this.attachChild(firstLineHorizontal);
		Line lastLineHorizontal = new Line(0, this.getModel().getHeight() * GameField.CELL_HEIGHT, this.getWidth(), this.getModel().getHeight() * GameField.CELL_HEIGHT, this.vertexBufferObjectManager);
		lastLineHorizontal.setColor(borderColor);
		lastLineHorizontal.setLineWidth(borderWidth);
		this.gameFieldLines.add(lastLineHorizontal);
		this.attachChild(lastLineHorizontal);

	}

	@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		if (pSceneTouchEvent.getMotionEvent().getPointerCount() > 1) {
			this.lastMultiTouched = System.currentTimeMillis();
		}

		int moutiTouchTolleranceMs = 200;

		if (System.currentTimeMillis() - this.lastMultiTouched < moutiTouchTolleranceMs) {
			return false;
		}

		Point touchedCell = this.positionToCell(pTouchAreaLocalX, pTouchAreaLocalY);
		Log.d(this.getClass().getName(), "Touched game-field at " + touchedCell.toString() + " with action " + pSceneTouchEvent.getAction());

		if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_MOVE) {
			Point newDragCell = this.positionToCell(pTouchAreaLocalX, pTouchAreaLocalY);
			Point oldDragCell = this.lastDragPoint;

			if (oldDragCell == null || !oldDragCell.equals(newDragCell)) {
				if (oldDragCell == null) {
					oldDragCell = newDragCell;
				}
				this.fireGameFieldDragged(newDragCell, oldDragCell);
				this.lastDragPoint = newDragCell;
			}
		}

		if (pSceneTouchEvent.getMotionEvent().getPointerCount() < 2 && pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP) {
			this.lastDragPoint = null;
			this.fireGameFieldTouched(pTouchAreaLocalX, pTouchAreaLocalY);
		}
		return true;
	}

	public Point positionToCell(final float posX, final float posY) {
		int cellX = Math.max(0, (int) (posX / GameField.CELL_WIDTH));
		int cellY = Math.max(0, (int) (posY / GameField.CELL_HEIGHT));
		cellX = Math.min(this.getModel().getWidth() - 1, cellX);
		cellY = Math.min(this.getModel().getHeight() - 1, cellY);
		return new Point(cellX, cellY);
	}

	public PointF cellToPosition(final int cellX, final int cellY) {
		float posX = cellX * GameField.CELL_WIDTH;
		float posY = cellY * GameField.CELL_HEIGHT;
		return new PointF(posX, posY);
	}

	public void addGameFieldInputListener(final GameFieldInputListener listener) {
		this.listenerList.addListener(GameFieldInputListener.class, listener);
	}

	public void removeGameFieldInputListener(final GameFieldInputListener listener) {
		this.listenerList.removeListener(GameFieldInputListener.class, listener);
	}

	private void fireGameFieldTouched(final float posX, final float posY) {
		GameFieldTouchEvent event = new GameFieldTouchEvent(this, this.positionToCell(posX, posY), new PointF(posX, posY));
		for (GameFieldInputListener listener : this.listenerList.getListeners(GameFieldInputListener.class)) {
			listener.gameFieldTouched(event);
		}
	}

	private void fireGameFieldDragged(final Point currentCell, final Point previousCell) {
		GameFieldDragEvent event = new GameFieldDragEvent(this, previousCell, currentCell);
		for (GameFieldInputListener listener : this.listenerList.getListeners(GameFieldInputListener.class)) {
			listener.gameFieldDragged(event);
		}
	}

	public void adaptFieldToModel() {
		for (int posY = 0; posY < this.getModel().getHeight(); posY++) {
			for (int posX = 0; posX < this.getModel().getWidth(); posX++) {
				if (this.getModel().isBarrierAt(posX, posY)) {
					this.setGameFieldState(posX, posY, State.BARRIER);
				}

				else if (this.getModel().isCellEmpty(posX, posY, false)) {
					this.setGameFieldState(posX, posY, State.BLANK);
				}

				else if (this.getModel().isBlock0At(posX, posY)) {
					this.setGameFieldState(posX, posY, State.BLOCK0);
				}

				else if (this.getModel().isBlock1At(posX, posY)) {
					this.setGameFieldState(posX, posY, State.BLOCK1);
				}

				else if (this.getModel().isBlock2At(posX, posY)) {
					this.setGameFieldState(posX, posY, State.BLOCK2);
				}

				else if (this.getModel().isBlock3At(posX, posY)) {
					this.setGameFieldState(posX, posY, State.BLOCK3);
				}

				else if (this.getModel().isBlock4At(posX, posY)) {
					this.setGameFieldState(posX, posY, State.BLOCK4);
				}
			}
		}

		for (int posY = 0; posY < this.getModel().getHeight(); posY++) {
			for (int posX = 0; posX < this.getModel().getWidth(); posX++) {
				if (this.isLampAt(posX, posY)) {
					this.setGameFieldState(posX, posY, State.LAMP);
					this.lightCellsWithLamp(posX, posY);
				}
			}
		}

	}

	private void lightCellsWithLamp(final int posX, final int posY) {
		for (int lightX = posX + 1; lightX < this.getModel().getWidth(); lightX++) {
			if (!this.getModel().isCellEmpty(lightX, posY, false)) {
				break;
			}
			this.setGameFieldState(lightX, posY, State.LIGHTED);
		}
		for (int lightX = posX - 1; lightX >= 0; lightX--) {
			if (!this.getModel().isCellEmpty(lightX, posY, false)) {
				break;
			}
			this.setGameFieldState(lightX, posY, State.LIGHTED);
		}

		for (int lightY = posY + 1; lightY < this.getModel().getHeight(); lightY++) {
			if (!this.getModel().isCellEmpty(posX, lightY, false)) {
				break;
			}
			this.setGameFieldState(posX, lightY, State.LIGHTED);
		}
		for (int lightY = posY - 1; lightY >= 0; lightY--) {
			if (!this.getModel().isCellEmpty(posX, lightY, false)) {
				break;
			}
			this.setGameFieldState(posX, lightY, State.LIGHTED);
		}
	}

	public boolean setLampAt(final Point location) {
		boolean placed = false;
		if (placed = this.puzzle.setLampAt(location)) {
			this.adaptFieldToModel();
		}
		return placed;
	}

	private void setGameFieldState(final int posX, final int posY, final State cellState) {
		this.gameFieldCells[posY][posX].setCellState(cellState);
	}

	public boolean removeLampAt(final Point location) {
		boolean removed = false;
		if (removed = this.getModel().removeLampAt(location)) {
			this.adaptFieldToModel();
		}
		return removed;
	}

	public boolean isLampAt(final int posX, final int posY) {
		return this.puzzle.isLampAt(posX, posY);

	}

	public boolean isLampAt(final Point location) {
		return this.isLampAt(location.x, location.y);
	}

}
