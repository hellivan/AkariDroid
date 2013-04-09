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
	private AkariSolver solver;

	public GameFieldController(final GameField gameField,AkariSolver solver) {
		this.gameField = gameField;
		this.solver = solver;

	}

	public void setGameField(final GameField gameField,AkariSolver solver) {
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
			
			try {
				List<Point> list = solver.getWrongPlacedLamps();
				
				if(list!=null)
				for(Point p :list)
				{
					gameField.removeLampAt(p);
				}
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
