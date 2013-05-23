package at.ac.uibk.akari.view;

import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.graphics.PointF;
import at.ac.uibk.akari.utils.TextureLoader;
import at.ac.uibk.akari.utils.TextureLoader.TextureType;

public class Cell extends AnimatedSprite {
	/**
	 * 
	 * Enumeration that represents all possible states of a cell
	 */
	public enum State {
		/**
		 * An empty cell
		 */
		BLANK(0, 0),
		/**
		 * A cell that represents a lamp
		 */
		LAMP(0, 1),
		/**
		 * A cell that is lighted by a lamp
		 */
		LIGHTED(0, 2),
		/**
		 * A cell that is marked by the user
		 */
		BARRIER(0, 4),
		/**
		 * A cell that may not have lamps around
		 */
		BLOCK0(0, 5),
		/**
		 * A cell that must have 1 lamp around
		 */
		BLOCK1(0, 6),
		/**
		 * A cell that must have 2 lamp around
		 */
		BLOCK2(0, 7),
		/**
		 * A cell that must have 3 lamp around
		 */
		BLOCK3(0, 8),
		/**
		 * A cell that must have 4 lamp around
		 */
		BLOCK4(0, 9);

		private int texturePosX;
		private int texturePosY;

		private State(final int texturePosX, final int texturePosY) {
			this.texturePosX = texturePosX;
			this.texturePosY = texturePosY;
		}

		public int getTexturePosX() {
			return this.texturePosX;
		}

		public int getTexturePosY() {
			return this.texturePosY;
		}

	};

	private static TextureType textureType = TextureType.CELL;

	public Cell(final PointF location, final int width, final int height, final VertexBufferObjectManager vertexBufferObjectManager) {
		this(location.x, location.y, width, height, vertexBufferObjectManager);
	}

	public Cell(final float posX, final float posY, final int width, final int height, final VertexBufferObjectManager vertexBufferObjectManager) {
		super(posX, posY, width, height, TextureLoader.getInstance().getTextureRegion(Cell.textureType), vertexBufferObjectManager);
		this.setCellState(State.BLANK);
	}

	public void setCellState(final State cellState) {
		this.setCurrentTileIndex(Cell.textureType.getTileNumber(cellState.getTexturePosX(), cellState.texturePosY));
	}
}
