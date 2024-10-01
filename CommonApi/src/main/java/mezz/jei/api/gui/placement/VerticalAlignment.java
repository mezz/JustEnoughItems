package mezz.jei.api.gui.placement;

/**
 * Represents a vertical alignment of an element inside a larger box (availableArea).
 * @since 15.20.1
 */
public enum VerticalAlignment {
	TOP {
		@Override
		public int getYPos(int availableHeight, int elementHeight) {
			return 0;
		}
	},
	CENTER {
		@Override
		public int getYPos(int availableHeight, int elementHeight) {
			return Math.round((availableHeight - elementHeight) / 2f);
		}
	},
	BOTTOM {
		@Override
		public int getYPos(int availableHeight, int elementHeight) {
			return availableHeight - elementHeight;
		}
	};

	/**
	 * Calculate the y position needed to align an element with the given height inside the availableArea.
	 * @since 15.20.1
	 */
	public abstract int getYPos(int availableHeight, int elementHeight);
}
