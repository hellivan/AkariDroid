package at.ac.uibk.akari.utils;

import java.util.HashMap;
import java.util.Map;

import org.andengine.entity.scene.background.IBackground;
import org.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.content.Context;
import android.content.res.AssetManager;

public class TextureLoader {

	// @Todo change texture-size to a maximum of 1024x1024 because of
	// low-end-devices
	public enum TextureType {
		/**
		 * Tiled texture for the a animated lamp
		 */
		LAMP("lamp.png", 1536, 512, 3, 1),
		/**
		 * Tiled texture for a cell
		 */
		CELL("cell.png", 128, 1280, 1, 10),
		/**
		 * Tiled texture for a menu icons
		 */
		MENU_ICONS("menu.png", 64, 128, 1, 2),
		/**
		 * Texture for popup-backgrounds
		 */
		POPUP_BACKGROUND("popupBackground.png", 500, 300, 1, 1),
		/**
		 * Texture for game-HUD-background
		 */
		HUD_BACKGROUND("wood_1.png", 700, 700, 1, 1);

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

	public enum BackgroundType {
		/**
		 * Background for the Game-field
		 */
		GAME_FIELD_BACKGROUND("grey_wash_wall.png");

		private String texturePath;

		private BackgroundType(final String texturePath) {
			this.texturePath = texturePath;
		}

		public String getTexturePath() {
			return this.texturePath;
		}

	}

	private Map<TextureType, TiledTextureRegion> texturesCache;
	private Map<BackgroundType, IBackground> backgroundCache;

	private static TextureLoader singleton;

	private TextureLoader() {
		this.texturesCache = new HashMap<TextureType, TiledTextureRegion>();
		this.backgroundCache = new HashMap<BackgroundType, IBackground>();
	}

	public static TextureLoader getInstance() {
		if (TextureLoader.singleton == null) {
			TextureLoader.singleton = new TextureLoader();
		}
		return TextureLoader.singleton;
	}

	public void init(final TextureManager textureManager, final Context context, final AssetManager assetManager, final VertexBufferObjectManager vertexBufferObjectManager, final float cameraWidth, final float cameraHeight) {
		String assetPath = "gfx/";
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath(assetPath);

		for (TextureType textureType : TextureType.values()) {
			BitmapTextureAtlas textureAtlas = new BitmapTextureAtlas(textureManager, textureType.getTextureWidth(), textureType.getTextureHeight(), TextureOptions.BILINEAR);

			TiledTextureRegion textureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, context, textureType.getTexturePath(), 0, 0, textureType.getTileColumns(), textureType.getTileRows());

			// @ToDo: improve memory usage by unloading not needed textures
			textureAtlas.load();
			this.texturesCache.put(textureType, textureRegion);
		}

		for (BackgroundType backgroundType : BackgroundType.values()) {
			this.backgroundCache.put(backgroundType, new RepeatingSpriteBackground(cameraWidth, cameraHeight, textureManager, AssetBitmapTextureAtlasSource.create(assetManager, assetPath + backgroundType.getTexturePath()), vertexBufferObjectManager));
		}

	}

	public ITextureRegion getTexture(final TextureType textureType, final int posX, final int posY) {
		return this.getTextureRegion(textureType).getTextureRegion(textureType.getTileNumber(posX, posY));
	}

	public ITiledTextureRegion getTextureRegion(final TextureType textureType) {
		return this.texturesCache.get(textureType);
	}

	public IBackground getBackground(final BackgroundType backgroundType) {
		return this.backgroundCache.get(backgroundType);
	}
}
