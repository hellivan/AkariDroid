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
		LAMP("lamp.png", 512, 1536, 3);

		private String texturePath;
		private int textureWidth;
		private int textureHeight;
		private int textureCount;

		private TextureType(final String texturePath, final int textureWidth, final int textureHeight, final int textureCount) {
			this.texturePath = texturePath;
			this.textureWidth = textureWidth;
			this.textureHeight = textureHeight;
			this.textureCount = textureCount;
		}

		public String getTexturePath() {
			return this.texturePath;
		}

		public int getTextureCount() {
			return this.textureCount;
		}

		public int getTextureHeight() {
			return this.textureHeight;
		}

		public int getTextureWidth() {
			return this.textureWidth;
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
			BitmapTextureAtlas textureAtlas = new BitmapTextureAtlas(textureManager, textureType.getTextureCount() * textureType.getTextureWidth(), textureType.getTextureHeight(), TextureOptions.BILINEAR);

			TiledTextureRegion textureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, context, textureType.getTexturePath(), 0, 0, textureType.getTextureCount(), 1);
			textureAtlas.load();
			this.texturesCache.put(textureType, textureRegion);
		}

	}

	public ITextureRegion getTexture(final TextureType textureType, final int textureNumber) {
		return this.getTextureRegion(textureType).getTextureRegion(textureNumber);
	}

	public ITiledTextureRegion getTextureRegion(final TextureType textureType) {
		return this.texturesCache.get(textureType);
	}
}
