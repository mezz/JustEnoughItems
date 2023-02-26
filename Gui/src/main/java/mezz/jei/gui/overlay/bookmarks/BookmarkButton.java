package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.UserInput;
import mezz.jei.common.config.IClientToggleState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class BookmarkButton extends GuiIconToggleButton {
	public static BookmarkButton create(BookmarkOverlay bookmarkOverlay, BookmarkList bookmarkList, Textures textures, IClientToggleState toggleState, IInternalKeyMappings keyBindings) {
		IDrawableStatic offIcon = textures.getBookmarkButtonDisabledIcon();
		IDrawableStatic onIcon = textures.getBookmarkButtonEnabledIcon();
		return new BookmarkButton(offIcon, onIcon, textures, bookmarkOverlay, bookmarkList, toggleState, keyBindings);
	}

	private final BookmarkOverlay bookmarkOverlay;
	private final BookmarkList bookmarkList;
	private final IClientToggleState toggleState;
	private final IInternalKeyMappings keyBindings;

	private BookmarkButton(IDrawable offIcon, IDrawable onIcon, Textures textures, BookmarkOverlay bookmarkOverlay, BookmarkList bookmarkList, IClientToggleState toggleState, IInternalKeyMappings keyBindings) {
		super(offIcon, onIcon, textures);
		this.bookmarkOverlay = bookmarkOverlay;
		this.bookmarkList = bookmarkList;
		this.toggleState = toggleState;
		this.keyBindings = keyBindings;
	}

	@Override
	protected void getTooltips(List<Component> tooltip) {
		tooltip.add(Component.translatable("jei.tooltip.bookmarks"));
		IJeiKeyMapping bookmarkKey = keyBindings.getBookmark();
		if (bookmarkKey.isUnbound()) {
			MutableComponent noKey = Component.translatable("jei.tooltip.bookmarks.usage.nokey");
			tooltip.add(noKey.withStyle(ChatFormatting.RED));
		} else if (!bookmarkOverlay.hasRoom()) {
			MutableComponent notEnoughSpace = Component.translatable("jei.tooltip.bookmarks.not.enough.space");
			tooltip.add(notEnoughSpace.withStyle(ChatFormatting.GOLD));
		} else {
			MutableComponent key = Component.translatable(
				"jei.tooltip.bookmarks.usage.key",
				bookmarkKey.getTranslatedKeyMessage()
			);
			tooltip.add(key.withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	protected boolean isIconToggledOn() {
		return bookmarkOverlay.isListDisplayed();
	}

	@Override
	protected boolean onMouseClicked(UserInput input) {
		if (!bookmarkList.isEmpty() && bookmarkOverlay.hasRoom()) {
			if (!input.isSimulate()) {
				toggleState.toggleBookmarkEnabled();
			}
			return true;
		}
		return false;
	}
}
