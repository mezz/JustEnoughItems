package mezz.jei.gui.bookmarks;

import mezz.jei.gui.overlay.elements.IElement;

public interface IBookmark {
	IElement<?> getElement();
	boolean isVisible();
	void setVisible(boolean visible);
}
