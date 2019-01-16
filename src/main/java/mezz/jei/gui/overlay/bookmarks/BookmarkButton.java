package mezz.jei.gui.overlay.bookmarks;

import java.util.List;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.Config;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.util.Translator;

public class BookmarkButton extends GuiIconToggleButton {
	public static BookmarkButton create(BookmarkOverlay bookmarkOverlay, BookmarkList bookmarkList, GuiHelper guiHelper) {
		IDrawableStatic offIcon = guiHelper.getBookmarkButtonDisabledIcon();
		IDrawableStatic onIcon = guiHelper.getBookmarkButtonEnabledIcon();
		return new BookmarkButton(offIcon, onIcon, bookmarkOverlay, bookmarkList);
	}

	private final BookmarkOverlay bookmarkOverlay;
	private final BookmarkList bookmarkList;

	private BookmarkButton(IDrawable offIcon, IDrawable onIcon, BookmarkOverlay bookmarkOverlay, BookmarkList bookmarkList) {
		super(offIcon, onIcon);
		this.bookmarkOverlay = bookmarkOverlay;
		this.bookmarkList = bookmarkList;
	}

	@Override
	protected void getTooltips(List<String> tooltip) {
		tooltip.add(Translator.translateToLocal("jei.tooltip.bookmarks"));
		KeyBinding bookmarkKey = KeyBindings.bookmark;
		if (bookmarkKey.getKeyCode() == 0) {
			tooltip.add(TextFormatting.RED + Translator.translateToLocal("jei.tooltip.bookmarks.usage.nokey"));
		} else if (!bookmarkOverlay.hasRoom()) {
			tooltip.add(TextFormatting.GOLD + Translator.translateToLocal("jei.tooltip.bookmarks.not.enough.space"));
		} else {
			tooltip.add(TextFormatting.GRAY + Translator.translateToLocalFormatted("jei.tooltip.bookmarks.usage.key", bookmarkKey.getDisplayName()));
		}
	}

	@Override
	protected boolean isIconToggledOn() {
		return bookmarkOverlay.isListDisplayed();
	}

	@Override
	protected boolean onMouseClicked(int mouseX, int mouseY) {
		if (!bookmarkList.isEmpty() && bookmarkOverlay.hasRoom()) {
			Config.toggleBookmarkEnabled();
			return true;
		}
		return false;
	}
}
