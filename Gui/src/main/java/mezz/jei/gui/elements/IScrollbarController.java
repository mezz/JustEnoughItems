package mezz.jei.gui.elements;

/** Supplies scroll extents in matching units (rows or pixels) and a position from 0 to 1. */
public interface IScrollbarController {
	int getVisibleScrollAmount();

	int getHiddenScrollAmount();

	float getScrollOffsetY();

	void setScrollOffsetY(float scrollOffsetY);
}
