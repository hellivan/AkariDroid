package at.ac.uibk.akari.common.listener;

import at.ac.uibk.akari.common.view.MenuItem;

public class MenuItemSeletedEvent {

	private Object source;
	private MenuItem menuItem;

	public MenuItemSeletedEvent(final Object source, final MenuItem menuItem) {
		this.source = source;
		this.menuItem = menuItem;
	}

	public Object getSource() {
		return this.source;
	}

	public MenuItem getMenuItem() {
		return this.menuItem;
	}

}
