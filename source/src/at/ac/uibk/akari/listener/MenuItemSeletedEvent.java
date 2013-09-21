package at.ac.uibk.akari.listener;

import at.ac.uibk.akari.common.menu.ItemType;
import at.ac.uibk.akari.common.menu.MenuItem;


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
