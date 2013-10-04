package at.ac.uibk.akari.common.controller;

public abstract class AbstractController {

	public abstract boolean start();

	public abstract boolean stop();

	public abstract void onGameStop();

	public abstract void onBackKeyPressed();

}
