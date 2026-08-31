package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.common.Internal;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IGuiInputLayer;
import mezz.jei.gui.input.IPinnedTooltipHolder;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.PinnedTooltipManager;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.overlay.elements.RecipeBookmarkElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BookmarkPreviewTooltipController implements IGuiInputLayer, IPinnedTooltipHolder {
	private final BookmarkOverlay bookmarkOverlay;
	private @Nullable BookmarkPreviewTooltip activeTooltip;
	private @Nullable Screen lastScreen;

	public BookmarkPreviewTooltipController(BookmarkOverlay bookmarkOverlay) {
		this.bookmarkOverlay = bookmarkOverlay;
	}

	public boolean isVisible() {
		return this.activeTooltip != null;
	}

	boolean isActive(BookmarkPreviewTooltip tooltip) {
		return this.activeTooltip == tooltip;
	}

	@Override
	public void hide() {
		if (this.activeTooltip != null) {
			this.activeTooltip = null;
			PinnedTooltipManager.closed(this);
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		BookmarkPreviewTooltip activeTooltip = this.activeTooltip;
		return activeTooltip != null && activeTooltip.isMouseOver(mouseX, mouseY);
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		update(mouseX, mouseY);
		BookmarkPreviewTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip != null) {
			activeTooltip.draw(guiGraphics, mouseX, mouseY);
		}
	}

	private void update(double mouseX, double mouseY) {
		if (!Internal.getKeyMappings().getPauseRecipeCycling().isDown() ||
			!bookmarkOverlay.isListDisplayed()
		) {
			hide();
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		if (screen != this.lastScreen) {
			this.lastScreen = screen;
			hide();
			return;
		}
		BookmarkPreviewTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip == null) {
			open(mouseX, mouseY);
		} else if (!activeTooltip.isSourceVisible()) {
			hide();
		}
	}

	private void open(double mouseX, double mouseY) {
		bookmarkOverlay.getIngredientUnderMouse(mouseX, mouseY)
			.map(IClickableIngredientInternal::getElement)
			.<RecipeBookmarkElement<?, ?>>mapMulti((element, consumer) -> {
				if (element instanceof RecipeBookmarkElement<?, ?> recipeBookmarkElement) {
					consumer.accept(recipeBookmarkElement);
				}
			})
			.findFirst()
			.ifPresent(element -> element.getInteractivePreview().ifPresent(component -> {
				hide();
				this.activeTooltip = new BookmarkPreviewTooltip(
					this,
					element,
					component,
					(int) mouseX,
					(int) mouseY
				);
				PinnedTooltipManager.opened(this);
			}));
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(
		Screen screen,
		UserInput input,
		IInternalKeyMappings keyBindings
	) {
		BookmarkPreviewTooltip activeTooltip = this.activeTooltip;
		if (activeTooltip == null) {
			return Optional.empty();
		}
		return activeTooltip.handleUserInput(screen, input, keyBindings);
	}
}
