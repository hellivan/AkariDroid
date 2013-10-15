package at.ac.uibk.akari.utils;

import java.util.HashMap;
import java.util.Map;

import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.FontManager;
import org.andengine.opengl.font.IFont;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;

import android.content.res.AssetManager;
import android.graphics.Color;

public class FontLoader {

	public enum FontType {
		/**
		 * Font called DROID with size 48 and color black
		 */
		DROID_48_BLACK("Droid.ttf", 48, Color.BLACK),
		/**
		 * Font called DROID with size 48 and color white
		 */
		DROID_48_WHITE("Droid.ttf", 48, Color.WHITE),
		/**
		 * Font called PLOK with size 48 and color black
		 */
		PLOK_48_BLACK("Plok.ttf", 48, Color.BLACK);

		private String fontPath;
		private float fontSize;
		private int fontColor;

		private FontType(final String fontPath, final float fontSize, final int fontColor) {
			this.fontPath = fontPath;
			this.fontSize = fontSize;
			this.fontColor = fontColor;
		}

		public String getFontPath() {
			return this.fontPath;
		}

		public float getFontSize() {
			return this.fontSize;
		}

		public int getFontColor() {
			return this.fontColor;
		}
	}

	private Map<FontType, Font> fontsCache;

	private static FontLoader singleton;

	private FontLoader() {
		this.fontsCache = new HashMap<FontType, Font>();
	}

	public static FontLoader getInstance() {
		if (FontLoader.singleton == null) {
			FontLoader.singleton = new FontLoader();
		}
		return FontLoader.singleton;
	}

	public void init(final TextureManager textureManager, final FontManager fontManager, final AssetManager assetManager) {

		FontFactory.setAssetBasePath("font/");

		for (FontType fontType : FontType.values()) {
			BitmapTextureAtlas textureAtlas = new BitmapTextureAtlas(textureManager, 256, 256, TextureOptions.BILINEAR);

			Font font = FontFactory.createFromAsset(fontManager, textureAtlas, assetManager, fontType.getFontPath(), fontType.getFontSize(), true, fontType.getFontColor());

			font.load();
			this.fontsCache.put(fontType, font);
		}
	}

	public IFont getFont(final FontType fontType) {
		return this.fontsCache.get(fontType);
	}
}
