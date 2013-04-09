package at.ac.uibk.akari.controller;

import java.util.List;

import org.sat4j.specs.TimeoutException;


import android.graphics.Point;
import android.util.Log;
import at.ac.uibk.akari.listener.GameFieldListener;
import at.ac.uibk.akari.listener.GameFieldTouchEvent;
import at.ac.uibk.akari.solver.AkariSolver;
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
			Point cellPosition = event.getCellPosition();

			Log.d(this.getClass().toString(), "GameField touched at " + cellPosition.x + "x" + cellPosition.y);

			if (this.gameField.isLampAt(cellPosition)) {
				this.gameField.removeLampAt(cellPosition);
			} else {
				this.gameField.setLampAt(event.getCellPosition());
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
