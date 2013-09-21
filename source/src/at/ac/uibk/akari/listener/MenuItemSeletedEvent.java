package at.ac.uibk.akari.listener;

import at.ac.uibk.akari.common.menu.ItemType;


public class MenuItemSeletedEvent {

	private Object source;
	private ItemType itemType;

	public MenuItemSeletedEvent(final Object source, final ItemType itemType) {
		this.source = source;
		this.itemType = itemType;
	}

	public Object getSource() {
		return this.source;
	}

	public ItemType getItemType() {
		return this.itemType;
	}

}
