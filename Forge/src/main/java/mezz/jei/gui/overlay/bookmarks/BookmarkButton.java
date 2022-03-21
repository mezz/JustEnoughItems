package mezz.jei.gui.overlay.bookmarks;

import java.util.List;

import mezz.jei.input.UserInput;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.textures.Textures;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

public class BookmarkButton extends GuiIconToggleButton {
	public static BookmarkButton create(BookmarkOverlay bookmarkOverlay, BookmarkList bookmarkList, Textures textures, IWorldConfig worldConfig) {
		IDrawableStatic offIcon = textures.getBookmarkButtonDisabledIcon();
		IDrawableStatic onIcon = textures.getBookmarkButtonEnabledIcon();
		return new BookmarkButton(offIcon, onIcon, bookmarkOverlay, bookmarkList, worldConfig);
	}

	private final BookmarkOverlay bookmarkOverlay;
	private final BookmarkList bookmarkList;
	private final IWorldConfig worldConfig;

	private BookmarkButton(IDrawable offIcon, IDrawable onIcon, BookmarkOverlay bookmarkOverlay, BookmarkList bookmarkList, IWorldConfig worldConfig) {
		super(offIcon, onIcon);
		this.bookmarkOverlay = bookmarkOverlay;
		this.bookmarkList = bookmarkList;
		this.worldConfig = worldConfig;
	}

	@Override
	protected void getTooltips(List<Component> tooltip) {
		tooltip.add(new TranslatableComponent("jei.tooltip.bookmarks"));
		KeyMapping bookmarkKey = KeyBindings.bookmark;
		if (bookmarkKey.getKey().getValue() == GLFW.GLFW_KEY_UNKNOWN) {
			TranslatableComponent noKey = new TranslatableComponent("jei.tooltip.bookmarks.usage.nokey");
			tooltip.add(noKey.withStyle(ChatFormatting.RED));
		} else if (!bookmarkOverlay.hasRoom()) {
			TranslatableComponent notEnoughSpace = new TranslatableComponent("jei.tooltip.bookmarks.not.enough.space");
			tooltip.add(notEnoughSpace.withStyle(ChatFormatting.GOLD));
		} else {
			TranslatableComponent key = new TranslatableComponent(
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
				worldConfig.toggleBookmarkEnabled();
			}
			return true;
		}
		return false;
	}
}
