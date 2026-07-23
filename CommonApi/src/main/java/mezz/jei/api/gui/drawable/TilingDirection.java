package mezz.jei.api.gui.drawable;

/**
 * Controls which direction a texture is tiled from when rendering partial tiles.
 *
 * @since 19.40.0
 */
public enum TilingDirection {
	/**
	 * Tile from top-left to bottom-right.
	 */
	DOWN_RIGHT,
	/**
	 * Tile from top-right to bottom-left.
	 */
	DOWN_LEFT,
	/**
	 * Tile from bottom-left to top-right.
	 */
	UP_RIGHT,
	/**
	 * Tile from bottom-right to top-left.
	 */
	UP_LEFT
}
