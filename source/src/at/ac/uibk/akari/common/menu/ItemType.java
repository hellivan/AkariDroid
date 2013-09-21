package at.ac.uibk.akari.common.menu;


public enum ItemType implements MenuItem{
	REPLAY("Replay"),

	NEXT("Next"),

	MAIN_MENU("Main Menu"),

	PAUSE("Pause"),

	HELP("Help"),

	CONTINUE("Continue"),

	RESET("Reset"),

	RANDOM_PUZZLE("Ramdom Puzzle"),

	SELECT_PUZZLE("Select Puzzle"),

	QUIT("Quit"),

	BACK("Back");

	private String text;

	private ItemType(final String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

}