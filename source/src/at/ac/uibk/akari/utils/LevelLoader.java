package at.ac.uibk.akari.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.ac.uibk.akari.core.GameFieldModel;
import at.ac.uibk.akari.core.GameFieldModel.CellState;
import at.ac.uibk.akari.core.JsonTools;
import browser.Browser;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class LevelLoader {

	public static void saveGameLevels(final List<GameFieldModel> levels, final String path) throws IOException {
		File levelsDir = new File(path);
		if (!levelsDir.exists()) {
			levelsDir.mkdirs();
		}
		int levelNumber = 0;
		for (GameFieldModel level : levels) {
			String number = String.format(Locale.GERMANY, "%05d", levelNumber++);
			FileWriter write = new FileWriter(levelsDir.getAbsolutePath() + File.separator + "level_" + number + ".json");
			write.write(JsonTools.getInstance().toJson(level, true));
			write.close();
		}
	}

	public static List<GameFieldModel> readGameLevels(final String levelPath) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		List<GameFieldModel> levels = new ArrayList<GameFieldModel>();
		File levelDir = new File(levelPath);
		for (File levelFile : levelDir.listFiles()) {
			levels.add(JsonTools.getInstance().fromJson(GameFieldModel.class, levelFile));
		}
		return levels;
	}

	public static List<GameFieldModel> fetchLevels(final Browser browser) throws IOException, URISyntaxException {
		List<GameFieldModel> models = new ArrayList<GameFieldModel>();
		int levelNumber = 1;
		GameFieldModel model = null;

		do {
			String levelString = String.format(Locale.GERMANY, "%04d", levelNumber++);
			model = LevelLoader.fetchLevel(browser, "http://www.nikoli.com/nfp/bj-" + levelString + ".nfp");
			if (model != null) {
				models.add(model);
			}
			System.out.println("Level " + models.size());
		} while (model != null);

		levelNumber = 1;
		do {
			String levelString = String.format(Locale.GERMANY, "%02d", levelNumber++);
			model = LevelLoader.fetchLevel(browser, "http://www.nikoli.com//nfp/bj-one_layout0907-" + levelString + ".nfp");
			if (model != null) {
				models.add(model);
			}
			System.out.println("Level " + models.size());
		} while (model != null);

		return models;
	}

	private static GameFieldModel fetchLevel(final Browser browser, final String url) throws IOException, URISyntaxException {

		if (browser.sendGET(url, 80, false, true) == Browser.HTTP_KEY_OK) {

			String dataString = new URI(new String(browser.getDownloadedData())).getPath();

			Pattern levelPattern = Pattern.compile("dataquestion=(.*?)&", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

			Matcher levelMatcher = levelPattern.matcher(dataString);

			if (levelMatcher.find()) {
				return LevelLoader.parseLevel(levelMatcher.group(1));
			}
		}
		return null;
	}

	private static GameFieldModel parseLevel(final String rawLevel) {
		String[] levelLines = rawLevel.split("\r\n|\r|\n");
		for (int i = 0; i < levelLines.length; i++) {
			levelLines[i] = levelLines[i].replace("+", "");
		}

		GameFieldModel model = new GameFieldModel(levelLines[0].length(), levelLines.length);
		for (int posY = 0; posY < model.getHeight(); posY++) {
			for (int posX = 0; posX < model.getWidth(); posX++) {
				switch (levelLines[posY].charAt(posX)) {
				case '-':
					model.setCellState(posX, posY, CellState.BLANK);
					break;
				case 'X':
				case '#':
					model.setCellState(posX, posY, CellState.BARRIER);
					break;
				case '0':
					model.setCellState(posX, posY, CellState.BLOCK0);
					break;
				case '1':
					model.setCellState(posX, posY, CellState.BLOCK1);
					break;
				case '2':
					model.setCellState(posX, posY, CellState.BLOCK2);
					break;
				case '3':
					model.setCellState(posX, posY, CellState.BLOCK3);
					break;
				case '4':
					model.setCellState(posX, posY, CellState.BLOCK4);
					break;

				}
			}
		}
		return model;
	}
}
