package at.ac.uibk.akari.utils;

import java.util.HashMap;
import java.util.Map;

import org.andengine.entity.scene.background.IBackground;
import org.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.content.res.AssetManager;

public class BackgroundLoader {

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

	private Map<BackgroundType, IBackground> backgroundCache;

	private static BackgroundLoader singleton;

	private BackgroundLoader() {
		this.backgroundCache = new HashMap<BackgroundType, IBackground>();
	}

	public static BackgroundLoader getInstance() {
		if (BackgroundLoader.singleton == null) {
			BackgroundLoader.singleton = new BackgroundLoader();
		}
		return BackgroundLoader.singleton;
	}

	public void init(final TextureManager textureManager, final AssetManager assetManager, final VertexBufferObjectManager vertexBufferObjectManager, final float cameraWidth, final float cameraHeight) {
		String assetPath = "gfx/";
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath(assetPath);

		for (BackgroundType backgroundType : BackgroundType.values()) {
			this.backgroundCache.put(backgroundType, new RepeatingSpriteBackground(cameraWidth, cameraHeight, textureManager, AssetBitmapTextureAtlasSource.create(assetManager, assetPath + backgroundType.getTexturePath()), vertexBufferObjectManager));
		}

	}

	public IBackground getBackground(final BackgroundType backgroundType) {
		return this.backgroundCache.get(backgroundType);
	}

}
