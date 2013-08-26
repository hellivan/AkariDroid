package at.ac.uibk.akari.listener;

public class MenuItemSeletedEvent {

	public enum ItemType {
		REPLAY("Replay"),

		NEXT("Next"),

		MAIN_MENU("Main Menu"),

		PAUSE("Pause"),

		HELP("Help"),

		CONTINUE("Continue"),

		RESET("Reset"),

		RANDOM_PUZZLE("Ramdom Puzzle"),

		SELECT_PUZZLE("Select Puzzle"),

		QUIT("Quit");

		private String itemText;

		private ItemType(final String itemText) {
			this.itemText = itemText;
		}

		public String getItemText() {
			return this.itemText;
		}
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
