package at.ac.uibk.akari.gameField.listener;

import java.util.EventListener;

import android.graphics.Point;
import at.ac.uibk.akari.gameField.controller.GameFieldController;

public interface GameFieldListener extends EventListener {

	public void lampPlaced(GameFieldController source, Point position);

	public void lampRemoved(GameFieldController source, Point position);

}
