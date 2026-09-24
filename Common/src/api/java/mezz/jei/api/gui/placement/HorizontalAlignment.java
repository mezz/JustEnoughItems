package mezz.jei.api.gui.placement;

/**
 * Represents a horizontal alignment of an element inside a larger area.
 * @since 19.19.1
 */
public enum HorizontalAlignment {
	LEFT {
		@Override
		public int getXPos(int availableWidth, int elementWidth) {
			return 0;
		}
	},
	CENTER {
		@Override
		public int getXPos(int availableWidth, int elementWidth) {
			return Math.round((availableWidth - elementWidth) / 2f);
		}
	},
	RIGHT {
		@Override
		public int getXPos(int availableWidth, int elementWidth) {
			return availableWidth - elementWidth;
		}
	};

	/**
	 * Calculate the x position needed to align an element with the given width inside the availableArea.
	 * @since 19.19.1
	 */
	public abstract int getXPos(int availableWidth, int elementWidth);
}
