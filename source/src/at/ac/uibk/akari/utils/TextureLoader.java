package at.ac.uibk.akari.utils;

import java.util.HashMap;
import java.util.Map;

import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;

import android.content.Context;

public class TextureLoader {

	public enum TextureType {
		/**
		 * Tiled texture for the a animated lamp
		 */
		LAMP("lamp.png", 1536, 512, 3, 1),
		/**
		 * Tiled texture for a cell
		 */
		CELL("cell.png", 128, 1280, 1, 10);

		private String texturePath;
		private int textureWidth;
		private int textureHeight;
		private int tileColumns;
		private int tileRows;

		private TextureType(final String texturePath, final int textureWidth, final int textureHeight, final int tileColumns, final int tileRows) {
			this.texturePath = texturePath;
			this.textureWidth = textureWidth;
			this.textureHeight = textureHeight;
			this.tileColumns = tileColumns;
			this.tileRows = tileRows;
		}

		public String getTexturePath() {
			return this.texturePath;
		}

		public int getTextureHeight() {
			return this.textureHeight;
		}

		public int getTextureWidth() {
			return this.textureWidth;
		}

		public int getTileColumns() {
			return this.tileColumns;
		}

		public int getTileRows() {
			return this.tileRows;
		}

		public int getTileNumber(final int posX, final int posY) {
			return this.getTileColumns() * posY + posX;
		}
	}

	private Map<TextureType, TiledTextureRegion> texturesCache;

	private static TextureLoader singleton;

	private TextureLoader() {
		this.texturesCache = new HashMap<TextureType, TiledTextureRegion>();
	}

	public static TextureLoader getInstance() {
		if (TextureLoader.singleton == null) {
			TextureLoader.singleton = new TextureLoader();
		}
		return TextureLoader.singleton;
	}

	public void init(final TextureManager textureManager, final Context context) {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		for (TextureType textureType : TextureType.values()) {
			BitmapTextureAtlas textureAtlas = new BitmapTextureAtlas(textureManager, textureType.getTextureWidth(), textureType.getTextureHeight(), TextureOptions.BILINEAR);

			TiledTextureRegion textureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, context, textureType.getTexturePath(), 0, 0, textureType.getTileColumns(), textureType.getTileRows());
			textureAtlas.load();
			this.texturesCache.put(textureType, textureRegion);
		}

	}

	public ITextureRegion getTexture(final TextureType textureType, final int posX, final int posY) {
		return this.getTextureRegion(textureType).getTextureRegion(textureType.getTileNumber(posX, posY));
	}

	public ITiledTextureRegion getTextureRegion(final TextureType textureType) {
		return this.texturesCache.get(textureType);
	}
}
