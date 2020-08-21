package mezz.jei.gui.overlay.bookmarks;

import java.util.List;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.textures.Textures;
import net.minecraft.util.text.TranslationTextComponent;
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
	protected void getTooltips(List<ITextComponent> tooltip) {
		tooltip.add(new TranslationTextComponent("jei.tooltip.bookmarks"));
		KeyBinding bookmarkKey = KeyBindings.bookmark;
		if (bookmarkKey.getKey().getKeyCode() == GLFW.GLFW_KEY_UNKNOWN) {
			TranslationTextComponent noKey = new TranslationTextComponent("jei.tooltip.bookmarks.usage.nokey");
			tooltip.add(noKey.mergeStyle(TextFormatting.RED));
		} else if (!bookmarkOverlay.hasRoom()) {
			TranslationTextComponent notEnoughSpace = new TranslationTextComponent("jei.tooltip.bookmarks.not.enough.space");
			tooltip.add(notEnoughSpace.mergeStyle(TextFormatting.GOLD));
		} else {
			TranslationTextComponent key = new TranslationTextComponent("jei.tooltip.bookmarks.usage.key", new TranslationTextComponent(bookmarkKey.getTranslationKey()));
			tooltip.add(key.mergeStyle(TextFormatting.GRAY));
		}
	}

	@Override
	protected boolean isIconToggledOn() {
		return bookmarkOverlay.isListDisplayed();
	}

	@Override
	protected boolean onMouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (!bookmarkList.isEmpty() && bookmarkOverlay.hasRoom()) {
			worldConfig.toggleBookmarkEnabled();
			return true;
		}
		return false;
	}
}
