package at.ac.uibk.akari.listener;

public class MenuItemSeletedEvent {

	public enum ItemType {
		REPLAY, NEXT, STOP, PAUSE, HELP

	}

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
