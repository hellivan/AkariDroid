package at.ac.uibk.akari.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;
import at.ac.uibk.akari.core.GameFieldModel;
import at.ac.uibk.akari.core.JsonTools;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class PuzzleLoader {

	private static final Pattern patternPuzzleDescription = Pattern.compile("name=(.*?);md5=(.*?);link=(.*?);;", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	private static List<PuzzleDescription> fetchPuzzleList(final String puzzleURL) throws ClientProtocolException, IOException {
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(new HttpGet(puzzleURL));
		StatusLine statusLine = response.getStatusLine();
		List<PuzzleDescription> puzzleDescriptions = new ArrayList<PuzzleDescription>();
		if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			out.close();
			Matcher descriptionMatcher = PuzzleLoader.patternPuzzleDescription.matcher(out.toString());
			while (descriptionMatcher.find()) {
				PuzzleDescription description = new PuzzleDescription(descriptionMatcher.group(2), descriptionMatcher.group(1));
				description.setPath(descriptionMatcher.group(3));
				puzzleDescriptions.add(description);
			}
		} else {
			Log.i(PuzzleLoader.class.getName(), "Puzzle-list returned status " + statusLine.getStatusCode() + " for " + puzzleURL);
		}
		return puzzleDescriptions;
	}

	private static List<PuzzleDescription> getLocalPuzzleList(final String puzzlePath) throws NoSuchAlgorithmException, IOException {
		List<PuzzleDescription> puzzleDescriptions = new ArrayList<PuzzleDescription>();
		File levelDir = new File(puzzlePath);
		if (!levelDir.exists()) {
			levelDir.mkdirs();
		}
		for (File puzzleFile : levelDir.listFiles()) {
			PuzzleDescription description = new PuzzleDescription(PuzzleLoader.generateMD5Sum(puzzleFile), puzzleFile.getName());
			description.setPath(puzzleFile.getAbsolutePath());
			puzzleDescriptions.add(description);
		}
		return puzzleDescriptions;
	}

	public static String generateMD5Sum(final File file) throws NoSuchAlgorithmException, IOException {
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		DigestInputStream inputStream = new DigestInputStream(new FileInputStream(file), messageDigest);
		byte[] buffer = new byte[100];
		while (inputStream.read(buffer, 0, 100) > -1) {

		}
		inputStream.close();
		return PuzzleLoader.bytesToHex(messageDigest.digest());

	}

	public static String bytesToHex(final byte[] bytes) {
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		StringBuffer buf = new StringBuffer();
		for (int j = 0; j < bytes.length; j++) {
			buf.append(hexDigit[(bytes[j] >> 4) & 0x0f]);
			buf.append(hexDigit[bytes[j] & 0x0f]);
		}
		return buf.toString();
	}

	public static int synchronizePuzzleList(final String puzzleURL, final String puzzlesPath) throws ClientProtocolException, IOException, NoSuchAlgorithmException {
		List<PuzzleDescription> fetchedDescriptions = PuzzleLoader.fetchPuzzleList(puzzleURL);
		List<PuzzleDescription> localDescriptions = PuzzleLoader.getLocalPuzzleList(puzzlesPath);

		Log.d(PuzzleLoader.class.getName(), "Got " + localDescriptions.size() + " local puzzles");
		Log.d(PuzzleLoader.class.getName(), "Got " + fetchedDescriptions.size() + " remote puzzles");

		List<PuzzleDescription> puzzlesToDelete = new ArrayList<PuzzleDescription>();
		List<PuzzleDescription> puzzlesToFetch = new ArrayList<PuzzleDescription>();

		for (PuzzleDescription localDescription : localDescriptions) {
			if (!PuzzleLoader.containsPuzzle(fetchedDescriptions, localDescription)) {
				puzzlesToDelete.add(localDescription);
			}
		}

		for (PuzzleDescription fetchedDescription : fetchedDescriptions) {
			if (!PuzzleLoader.containsPuzzle(localDescriptions, fetchedDescription)) {
				puzzlesToFetch.add(fetchedDescription);
			}
		}
		Log.d(PuzzleLoader.class.getName(), "Puzzles to delete: " + puzzlesToDelete.size());
		Log.d(PuzzleLoader.class.getName(), "Puzzles to fetch: " + puzzlesToFetch.size());

		List<PuzzleDescription> deletedPuzzles = PuzzleLoader.deleteLocalPuzzles(puzzlesToDelete);
		Log.i(PuzzleLoader.class.getName(), "Deleted " + deletedPuzzles.size() + " of " + puzzlesToDelete.size() + " puzzels from " + puzzlesPath);
		List<PuzzleDescription> fetchedPuzzles = PuzzleLoader.fetchPuzzles(puzzlesToFetch, puzzlesPath);
		Log.i(PuzzleLoader.class.getName(), "Fetched " + fetchedPuzzles.size() + " of " + puzzlesToFetch.size() + " puzzels from " + puzzleURL);

		return localDescriptions.size() - deletedPuzzles.size() + fetchedPuzzles.size();
	}

	private static List<PuzzleDescription> fetchPuzzles(final List<PuzzleDescription> puzzles, final String puzzlesPath) throws IOException {
		List<PuzzleDescription> fetched = new ArrayList<PuzzleDescription>();
		for (PuzzleDescription puzzle : puzzles) {
			GameFieldModel puzzleData = PuzzleLoader.fetchPuzzle(puzzle);
			if (puzzleData != null) {
				PuzzleLoader.savePuzzle(puzzlesPath, puzzle.getName(), puzzleData);
				fetched.add(puzzle);
			}
		}
		return fetched;
	}

	private static GameFieldModel fetchPuzzle(final PuzzleDescription puzzle) throws IOException {
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(new HttpGet(puzzle.getPath()));
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			out.close();
			return JsonTools.getInstance().fromJson(GameFieldModel.class, out.toString());
		} else {
			Log.i(PuzzleLoader.class.getName(), "Puzzle-data returned status " + statusLine.getStatusCode() + " for " + puzzle.getPath());
		}
		return null;
	}

	private static List<PuzzleDescription> deleteLocalPuzzles(final List<PuzzleDescription> puzzles) {
		List<PuzzleDescription> deleted = new ArrayList<PuzzleDescription>();
		for (PuzzleDescription puzzle : puzzles) {
			File puzzleFile = new File(puzzle.getPath());
			if (puzzleFile.exists()) {
				if (puzzleFile.delete()) {
					deleted.add(puzzle);
				}
			}
		}
		return deleted;
	}

	private static boolean containsPuzzle(final List<PuzzleDescription> puzzles, final PuzzleDescription puzzle) {
		for (PuzzleDescription fetchedDescription : puzzles) {
			if (fetchedDescription.getMd5().toLowerCase(Locale.GERMAN).equals(puzzle.getMd5().toLowerCase(Locale.GERMAN)) && fetchedDescription.getName().toLowerCase(Locale.GERMAN).equals(puzzle.getName().toLowerCase(Locale.GERMAN))) {
				return true;
			}
		}
		return false;
	}

	public static void savePuzzle(final String path, final String puzzleName, final GameFieldModel puzzle) throws IOException {
		File levelsDir = new File(path);
		if (!levelsDir.exists()) {
			levelsDir.mkdirs();
		}
		FileWriter write = new FileWriter(levelsDir.getAbsolutePath() + File.separator + puzzleName);
		write.write(JsonTools.getInstance().toJson(puzzle, true));
		write.close();
	}

	public static List<GameFieldModel> loadPuzzles(final String puzzlesPath) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		List<GameFieldModel> levels = new ArrayList<GameFieldModel>();
		File levelDir = new File(puzzlesPath);
		for (File levelFile : PuzzleLoader.sortFiles(levelDir.listFiles())) {

			levels.add(JsonTools.getInstance().fromJson(GameFieldModel.class, levelFile));
		}
		return levels;
	}

	private static List<File> sortFiles(final File[] files) {
		List<File> sortedFiles = Arrays.asList(files);

		Collections.sort(sortedFiles, new Comparator<File>() {

			@Override
			public int compare(final File lhs, final File rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}
		});
		return sortedFiles;
	}

}
