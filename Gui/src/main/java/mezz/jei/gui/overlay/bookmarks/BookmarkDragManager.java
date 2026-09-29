package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.MouseDrag;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.overlay.elements.IElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class BookmarkDragManager {
	private final BookmarkOverlay bookmarkOverlay;
	private @Nullable BookmarkDrag<?> bookmarkDrag;

	public BookmarkDragManager(BookmarkOverlay bookmarkOverlay) {
		this.bookmarkOverlay = bookmarkOverlay;
	}

	public void updateDrag(int mouseX, int mouseY) {
		if (bookmarkDrag != null) {
			bookmarkDrag.update(mouseX, mouseY);
		}
	}

	boolean hasDrag() {
		return bookmarkDrag != null;
	}

	boolean isDragging() {
		return bookmarkDrag != null && bookmarkDrag.isDragging();
	}

	public boolean drawDraggedItem(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (bookmarkDrag != null) {
			return bookmarkDrag.drawItem(guiGraphics, mouseX, mouseY);
		}
		return false;
	}

	public void stopDrag() {
		if (this.bookmarkDrag != null) {
			this.bookmarkDrag.stop();
			this.bookmarkDrag = null;
		}
	}

	private <V> boolean startDrag(IDraggableIngredientInternal<V> clicked, UserInput input) {
		IElement<V> element = clicked.getElement();
		return element
			.getBookmark()
			.map(bookmark -> {
				ITypedIngredient<V> ingredient = clicked.getTypedIngredient();
				IIngredientType<V> type = ingredient.getType();

				IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
				IIngredientRenderer<V> ingredientRenderer = ingredientManager.getIngredientRenderer(type);
				ImmutableRect2i clickedArea = clicked.getArea();
				this.bookmarkDrag = new BookmarkDrag<>(
					bookmarkOverlay,
					ingredientRenderer,
					ingredient,
					bookmark,
					input.getMouseX(),
					input.getMouseY(),
					clickedArea
				);
				return true;
			})
			.orElse(false);
	}

	public Optional<IInputInteraction> beginDrag(Screen ignoredScreen, UserInput input) {
		IClientConfig clientConfig = Internal.getClientConfigs().getClientConfig();
		if (!clientConfig.dragToRearrangeBookmarksEnabled().get()) {
			stopDrag();
			return Optional.empty();
		}

		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return Optional.empty();
		}

		return bookmarkOverlay.getDraggableIngredientUnderMouse(input.getMouseX(), input.getMouseY())
			.findFirst()
			.flatMap(clicked -> {
				ItemStack mouseItem = player.containerMenu.getCarried();
				if (mouseItem.isEmpty() &&
					startDrag(clicked, input)
				) {
					return Optional.of(MouseDrag.forIngredient(this::finishDrag, this::stopDrag));
				}
				return Optional.empty();
			});
	}

	private void finishDrag(UserInput input) {
		if (bookmarkDrag == null) {
			return;
		}
		bookmarkDrag.complete(input);
		bookmarkDrag = null;
	}
}
