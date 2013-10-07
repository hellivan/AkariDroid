package at.ac.uibk.akari.gameField.view;

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
		BLANK(1, 0),
		/**
		 * A cell that represents a lamp
		 */
		LAMP(1, 1),
		/**
		 * A cell that is lighted by a lamp
		 */
		LIGHTED(1, 2),
		/**
		 * A cell that is marked by the user
		 */
		MARK(1, 3),
		/**
		 * A cell that is marked by the user and lighted by a lamp
		 */
		LIGHTED_MARK(1, 4),
		/**
		 * A cell that is a barrier (black cell)
		 */
		BARRIER(0, 0),
		/**
		 * A cell that may not have lamps around
		 */
		BLOCK0(0, 1),
		/**
		 * A cell that must have 1 lamp around
		 */
		BLOCK1(0, 2),
		/**
		 * A cell that must have 2 lamp around
		 */
		BLOCK2(0, 3),
		/**
		 * A cell that must have 3 lamp around
		 */
		BLOCK3(0, 4),
		/**
		 * A cell that must have 4 lamp around
		 */
		BLOCK4(0, 5);

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
