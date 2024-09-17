package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.input.UserInput;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class BookmarkButton extends GuiIconToggleButton {
	public static BookmarkButton create(BookmarkOverlay bookmarkOverlay, BookmarkList bookmarkList, IWorldConfig worldConfig, IInternalKeyMappings keyBindings) {
		Textures textures = Internal.getTextures();
		IDrawableStatic offIcon = textures.getBookmarkButtonDisabledIcon();
		IDrawableStatic onIcon = textures.getBookmarkButtonEnabledIcon();
		return new BookmarkButton(offIcon, onIcon, bookmarkOverlay, bookmarkList, worldConfig, keyBindings);
	}

	private final BookmarkOverlay bookmarkOverlay;
	private final BookmarkList bookmarkList;
	private final IWorldConfig worldConfig;
	private final IInternalKeyMappings keyBindings;

	private BookmarkButton(IDrawable offIcon, IDrawable onIcon, BookmarkOverlay bookmarkOverlay, BookmarkList bookmarkList, IWorldConfig worldConfig, IInternalKeyMappings keyBindings) {
		super(offIcon, onIcon);
		this.bookmarkOverlay = bookmarkOverlay;
		this.bookmarkList = bookmarkList;
		this.worldConfig = worldConfig;
		this.keyBindings = keyBindings;
	}

	@Override
	protected void getTooltips(JeiTooltip tooltip) {
		tooltip.add(Component.translatable("jei.tooltip.bookmarks"));
		IJeiKeyMapping bookmarkKey = keyBindings.getBookmark();
		if (bookmarkKey.isUnbound()) {
			MutableComponent noKey = Component.translatable("jei.tooltip.bookmarks.usage.nokey");
			tooltip.add(noKey.withStyle(ChatFormatting.RED));
		} else if (!bookmarkOverlay.hasRoom()) {
			MutableComponent notEnoughSpace = Component.translatable("jei.tooltip.bookmarks.not.enough.space");
			tooltip.add(notEnoughSpace.withStyle(ChatFormatting.GOLD));
		} else {
			tooltip.addKeyUsageComponent(
				"jei.tooltip.bookmarks.usage.key",
				bookmarkKey
			);
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
				worldConfig.toggleBookmarkEnabled();
			}
			return true;
		}
		return false;
	}
}
