package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.bookmarks.IBookmark;

public interface IBookmarkDragTarget {
	ImmutableRect2i getArea();
	void accept(IBookmark bookmark);
}
