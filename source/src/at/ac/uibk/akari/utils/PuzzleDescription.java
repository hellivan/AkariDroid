package at.ac.uibk.akari.utils;

public class PuzzleDescription {

	private String md5;
	private String name;
	private String path;

	public PuzzleDescription(final String md5, final String name) {
		this.md5 = md5;
		this.name = name;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public String getMd5() {
		return this.md5;
	}

	public String getName() {
		return this.name;
	}

}
