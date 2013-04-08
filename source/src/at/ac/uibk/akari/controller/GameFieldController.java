package at.ac.uibk.akari.controller;

import android.util.Log;
import at.ac.uibk.akari.listener.GameFieldListener;
import at.ac.uibk.akari.listener.GameFieldTouchEvent;
import at.ac.uibk.akari.view.GameField;

public class GameFieldController extends AbstractController implements GameFieldListener {

	private GameField gameField;

	public GameFieldController(final GameField gameField) {
		this.gameField = gameField;

	}

	public void setGameField(final GameField gameField) {
		this.gameField = gameField;
	}

	@Override
	public void gameFieldTouched(final GameFieldTouchEvent event) {
		if (event.getSource().equals(this.gameField)) {
			Log.d(this.getClass().toString(), "GameField touched at " + event.getCellPosition().x + "x" + event.getCellPosition().y);

			switch (this.gameField.getModel().getCellState(event.getCellPosition())) {
			case BLANK:
				this.gameField.setLampAt(event.getCellPosition());
				break;
			case LAMP:
				this.gameField.removeLampAt(event.getCellPosition());
				break;
			}

		}
	}

	@Override
	public boolean start() {
		this.gameField.addGameFieldListener(this);
		return true;
	}

	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return false;
	}
}
