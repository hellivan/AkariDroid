package at.ac.uibk.akari.controller;

import android.graphics.Point;
import android.util.Log;
import at.ac.uibk.akari.listener.GameFieldDragEvent;
import at.ac.uibk.akari.listener.GameFieldInputListener;
import at.ac.uibk.akari.listener.GameFieldListener;
import at.ac.uibk.akari.listener.GameFieldTouchEvent;
import at.ac.uibk.akari.utils.ListenerList;
import at.ac.uibk.akari.view.GameField;

public class GameFieldController extends AbstractController implements GameFieldInputListener {

	private GameField gameField;

	private ListenerList listenerList;

	public GameFieldController(final GameField gameField) {
		this.gameField = gameField;
		this.listenerList = new ListenerList();
	}

	@Override
	public void gameFieldTouched(final GameFieldTouchEvent event) {
		if (event.getSource().equals(this.gameField)) {
			Point cellPosition = event.getTouchCell();

			Log.d(this.getClass().toString(), "GameField touched at " + cellPosition.toString());
			if (this.gameField.isLampAt(cellPosition)) {
				if (this.gameField.removeLampAt(cellPosition)) {
					this.fireLampRemoved(cellPosition);
				}
			} else {
				if (this.gameField.setLampAt(cellPosition)) {
					this.fireLampPlaced(cellPosition);
				}
			}

		}
	}

	@Override
	public boolean start() {
		this.gameField.addGameFieldInputListener(this);
		return true;
	}

	@Override
	public boolean stop() {
		this.gameField.removeGameFieldInputListener(this);
		return true;
	}

	@Override
	public void gameFieldDragged(final GameFieldDragEvent event) {
		if (event.getSource().equals(this.gameField)) {
			Point lastCell = event.getLastCell();
			Point currentCell = event.getCurrentCell();
			Log.d(this.getClass().toString(), "GameField dragged from  " + lastCell.toString() + " to " + currentCell.toString());

			// this.gameField.removeLampAt(lastCell);
			// this.gameField.setLampAt(currentCell);
		}
	}

	private void fireLampPlaced(final Point location) {
		for (GameFieldListener listener : this.listenerList.getListeners(GameFieldListener.class)) {
			listener.lampPlaced(this, location);
		}
	}

	private void fireLampRemoved(final Point location) {
		for (GameFieldListener listener : this.listenerList.getListeners(GameFieldListener.class)) {
			listener.lampRemoved(this, location);
		}
	}

	public void addGameFieldListener(final GameFieldListener listener) {
		this.listenerList.addListener(GameFieldListener.class, listener);
	}

	public void removeGameFieldListener(final GameFieldListener listener) {
		this.listenerList.removeListener(GameFieldListener.class, listener);
	}
	
}
