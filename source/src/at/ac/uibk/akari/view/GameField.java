package at.ac.uibk.akari.view;

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
import at.ac.uibk.akari.core.GameFieldModel.CellState;
import at.ac.uibk.akari.listener.GameFieldListener;
import at.ac.uibk.akari.listener.GameFieldTouchEvent;
import at.ac.uibk.akari.utils.ListenersList;

public class GameField extends Rectangle {

	private static final int CELL_WIDTH = 50;
	private static final int CELL_HEIGHT = 50;

	private GameFieldModel model;
	private ListenersList listenerList;

	private VertexBufferObjectManager vertexBufferObjectManager;

	public GameField(final float posX, final float posY, final int cellCountX, final int cellCountY, final VertexBufferObjectManager vertexBufferObjectManager) {
		this(posY, posY, new GameFieldModel(cellCountX, cellCountY), vertexBufferObjectManager);
	}

	public GameField(final float posX, final float posY, final GameFieldModel model, final VertexBufferObjectManager vertexBufferObjectManager) {
		super(posX, posY, model.getWidth() * GameField.CELL_WIDTH, model.getHeight() * GameField.CELL_HEIGHT, vertexBufferObjectManager, DrawType.STATIC);
		this.listenerList = new ListenersList();
		this.model = model;
		this.inifField(vertexBufferObjectManager);
	}

	private void inifField(final VertexBufferObjectManager vertexBufferObjectManager) {
		this.setColor(1, 1, 1, 0.85f);
		Color linesColor = new Color(0, 0, 0, 0.8f);
		this.addFieldLines(vertexBufferObjectManager, linesColor);

	}

	private void addFieldLines(final VertexBufferObjectManager vertexBufferObjectManager, final Color linesColor) {
		// add vertical lines
		Line firstLineVertical = new Line(0, 0, 0, this.getHeight(), vertexBufferObjectManager);
		firstLineVertical.setColor(linesColor);
		firstLineVertical.setLineWidth(5);
		this.attachChild(firstLineVertical);
		for (int cellX = 1; cellX < this.model.getWidth(); cellX++) {
			Line line = new Line(cellX * GameField.CELL_WIDTH, 0, cellX * GameField.CELL_WIDTH, this.getHeight(), vertexBufferObjectManager);
			line.setColor(linesColor);
			line.setLineWidth(3);
			this.attachChild(line);
		}
		Line lastLineVertical = new Line(this.model.getWidth() * GameField.CELL_WIDTH, 0, this.model.getWidth() * GameField.CELL_WIDTH, this.getHeight(), vertexBufferObjectManager);
		lastLineVertical.setColor(linesColor);
		lastLineVertical.setLineWidth(5);
		this.attachChild(lastLineVertical);

		// add horizontal lines
		Line firstLineHorizontal = new Line(0, 0, this.getWidth(), 0, vertexBufferObjectManager);
		firstLineHorizontal.setColor(linesColor);
		firstLineHorizontal.setLineWidth(5);
		this.attachChild(firstLineHorizontal);
		for (int cellY = 1; cellY < this.model.getHeight(); cellY++) {
			Line line = new Line(0, cellY * GameField.CELL_HEIGHT, this.getWidth(), cellY * GameField.CELL_HEIGHT, vertexBufferObjectManager);
			line.setColor(linesColor);
			line.setLineWidth(3);
			this.attachChild(line);
		}
		Line lastLineHorizontal = new Line(0, this.model.getHeight() * GameField.CELL_HEIGHT, this.getWidth(), this.model.getHeight() * GameField.CELL_HEIGHT, vertexBufferObjectManager);
		lastLineHorizontal.setColor(linesColor);
		lastLineHorizontal.setLineWidth(5);
		this.attachChild(lastLineHorizontal);
	}

	@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		Point touchedCell = this.positionToCell(pTouchAreaLocalX, pTouchAreaLocalY);
		Log.d(this.getClass().toString(), "Touched game-field at " + touchedCell.x + "x" + touchedCell.y + " with action " + pSceneTouchEvent.getAction());
		if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP) {
			this.fireGameFieldTouched(pTouchAreaLocalX, pTouchAreaLocalY);
		}
		return true;
	}

	public Point positionToCell(final float posX, final float posY) {
		int cellX = Math.max(0, (int) (posX / GameField.CELL_WIDTH));
		int cellY = Math.max(0, (int) (posY / GameField.CELL_HEIGHT));
		cellX = Math.min(this.model.getWidth() - 1, cellX);
		cellY = Math.min(this.model.getHeight() - 1, cellY);
		return new Point(cellX, cellY);
	}

	public PointF cellToPosition(final int cellX, final int cellY) {
		float posX = cellX * GameField.CELL_WIDTH;
		float posY = cellY * GameField.CELL_HEIGHT;
		return new PointF(posX, posY);
	}

	public void addGameFieldListener(final GameFieldListener listener) {
		this.listenerList.addListener(GameFieldListener.class, listener);
	}

	public void removeGameFieldListener(final GameFieldListener listener) {
		this.listenerList.removeListener(GameFieldListener.class, listener);
	}

	private void fireGameFieldTouched(final float posX, final float posY) {
		GameFieldTouchEvent event = new GameFieldTouchEvent(this, this.positionToCell(posX, posY), new PointF(posX, posY));
		for (GameFieldListener listener : this.listenerList.getListeners(GameFieldListener.class)) {
			listener.gameFieldTouched(event);
		}
	}

	public boolean setLampAt(final Point location) {
		return this.setLampAt(location.x, location.y);
	}

	public boolean setLampAt(final int posX, final int posY) {
		if (this.model.getCellState(posX, posY).equals(CellState.BLANK)) {
			this.model.setCellState(posX, posY, CellState.LAMP);
			this.attachChild(new Lamp(this.cellToPosition(posX, posY), GameField.CELL_WIDTH, GameField.CELL_HEIGHT, this.vertexBufferObjectManager));
			return true;
		}
		return false;
	}
}
