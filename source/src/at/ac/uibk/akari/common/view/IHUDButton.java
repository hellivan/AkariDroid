package at.ac.uibk.akari.common.view;

import org.andengine.entity.IEntity;
import org.andengine.entity.scene.ITouchArea;

import at.ac.uibk.akari.common.listener.TouchListener;

public interface IHUDButton extends ITouchArea, IEntity {

	public void setEnabled(final boolean enable);

	public void addTouchListener(final TouchListener listener);

	public void removeTouchListener(final TouchListener listener);

}
