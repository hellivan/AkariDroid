package at.ac.uibk.akari.common.menu;

public enum DefaultMenuItem implements MenuItem {
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

	BACK("Back"),

	FOREWARD("Foreward"),

	BACKWARD("Backward");

	private String text;

	private DefaultMenuItem(final String text) {
		this.text = text;
	}

	@Override
	public String getText() {
		return this.text;
	}

}