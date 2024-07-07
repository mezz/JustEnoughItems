package mezz.jei.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.ClickableIngredientInternal;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.handlers.CombinedInputHandler;
import mezz.jei.gui.input.handlers.NullInputHandler;
import mezz.jei.gui.input.handlers.ProxyInputHandler;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RecipeGuiLayouts {
	private final List<RecipeLayoutWithButtons<?>> recipeLayoutsWithButtons = new ArrayList<>();
	@Nullable
	private IUserInputHandler cachedInputHandler;

	public RecipeGuiLayouts() {
		this.cachedInputHandler = NullInputHandler.INSTANCE;
	}

	public void updateLayout(ImmutableRect2i recipeLayoutsArea) {
		ImmutableRect2i layoutAreaWithBorder = this.recipeLayoutsWithButtons.stream()
			.findFirst()
			.map(RecipeLayoutWithButtons::getRecipeLayout)
			.map(IRecipeLayoutDrawable::getRectWithBorder)
			.map(ImmutableRect2i::new)
			.orElse(null);
		if (layoutAreaWithBorder == null) {
			return;
		}
		final int recipeHeight = layoutAreaWithBorder.getHeight();
		int availableHeight = recipeLayoutsArea.getHeight();
		availableHeight = Math.max(availableHeight, recipeHeight);

		final int recipesPerPage = this.recipeLayoutsWithButtons.size();
		final int recipeXOffset = getRecipeXOffset(layoutAreaWithBorder, recipeLayoutsArea);
		final int recipeHeightTotal = recipesPerPage * layoutAreaWithBorder.getHeight();
		final int remainingHeight = availableHeight - recipeHeightTotal;
		final int recipeSpacing = remainingHeight / (recipesPerPage + 1);

		final int spacingY = layoutAreaWithBorder.getHeight() + recipeSpacing;
		int recipeYOffset = recipeLayoutsArea.getY() + recipeSpacing;
		for (RecipeLayoutWithButtons<?> recipeLayoutWithButtons : recipeLayoutsWithButtons) {
			IRecipeLayoutDrawable<?> recipeLayout = recipeLayoutWithButtons.getRecipeLayout();
			Rect2i rectWithBorder = recipeLayout.getRectWithBorder();
			Rect2i rect = recipeLayout.getRect();
			recipeLayout.setPosition(
				recipeXOffset - rectWithBorder.getX() + rect.getX(),
				recipeYOffset - rectWithBorder.getY() + rect.getY()
			);
			recipeYOffset += spacingY;
		}

		updateRecipeButtonPositions();
	}

	private void updateRecipeButtonPositions() {
		for (RecipeLayoutWithButtons<?> recipeLayoutWithButtons : recipeLayoutsWithButtons) {
			IRecipeLayoutDrawable<?> recipeLayout = recipeLayoutWithButtons.getRecipeLayout();
			Rect2i layoutArea = recipeLayout.getRect();

			{
				RecipeTransferButton button = recipeLayoutWithButtons.getTransferButton();
				Rect2i buttonArea = recipeLayout.getRecipeTransferButtonArea();
				buttonArea.setX(buttonArea.getX() + layoutArea.getX());
				buttonArea.setY(buttonArea.getY() + layoutArea.getY());
				button.updateBounds(buttonArea);
			}
			{
				RecipeBookmarkButton button = recipeLayoutWithButtons.getBookmarkButton();
				Rect2i buttonArea = recipeLayout.getRecipeBookmarkButtonArea();
				buttonArea.setX(buttonArea.getX() + layoutArea.getX());
				buttonArea.setY(buttonArea.getY() + layoutArea.getY());
				button.updateBounds(buttonArea);
			}
		}
	}

	private int getRecipeXOffset(ImmutableRect2i layoutRect, ImmutableRect2i layoutsArea) {
		final int recipeWidth = layoutRect.getWidth();
		final int recipeWidthWithButtons;
		if (recipeLayoutsWithButtons.isEmpty()) {
			recipeWidthWithButtons = layoutRect.getWidth();
		} else {
			recipeWidthWithButtons = recipeLayoutsWithButtons.get(0).totalWidth();
		}

		final int buttonSpace = recipeWidthWithButtons - recipeWidth;

		final int availableArea = layoutsArea.getWidth();
		if (availableArea > recipeWidth + (2 * buttonSpace)) {
			// we have enough room to nicely draw the recipe centered with the buttons off to the side
			return layoutsArea.getX() + (layoutsArea.getWidth() - recipeWidth) / 2;
		} else {
			// we can just barely fit, center the recipe and buttons all together in the available area
			return layoutsArea.getX() + (layoutsArea.getWidth() - recipeWidthWithButtons) / 2;
		}
	}

	public IUserInputHandler createInputHandler() {
		return new ProxyInputHandler(() -> {
			if (cachedInputHandler == null) {
				List<IUserInputHandler> handlers = this.recipeLayoutsWithButtons.stream()
					.map(RecipeLayoutWithButtons::createUserInputHandler)
					.toList();
				cachedInputHandler = new CombinedInputHandler(handlers);
			}
			return cachedInputHandler;
		});
	}

	public void tick(@Nullable AbstractContainerMenu parentContainer) {
		Player player = Minecraft.getInstance().player;
		for (RecipeLayoutWithButtons<?> recipeLayoutWithButtons : this.recipeLayoutsWithButtons) {
			RecipeTransferButton button = recipeLayoutWithButtons.getTransferButton();
			button.update(parentContainer, player);

			RecipeBookmarkButton bookmarkButton = recipeLayoutWithButtons.getBookmarkButton();
			bookmarkButton.tick();
		}
	}

	public void setRecipeLayoutsWithButtons(List<RecipeLayoutWithButtons<?>> recipeLayoutsWithButtons) {
		this.recipeLayoutsWithButtons.clear();
		this.recipeLayoutsWithButtons.addAll(recipeLayoutsWithButtons);
		this.cachedInputHandler = null;
	}

	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		return this.recipeLayoutsWithButtons.stream()
			.map(RecipeLayoutWithButtons::getRecipeLayout)
			.map(recipeLayout -> getRecipeLayoutIngredientUnderMouse(recipeLayout, mouseX, mouseY))
			.flatMap(Optional::stream);
	}

	private static Optional<IClickableIngredientInternal<?>> getRecipeLayoutIngredientUnderMouse(IRecipeLayoutDrawable<?> recipeLayout, double mouseX, double mouseY) {
		return recipeLayout.getRecipeSlotUnderMouse(mouseX, mouseY)
			.flatMap(recipeSlot -> getClickedIngredient(recipeLayout, recipeSlot));
	}

	private static Optional<IClickableIngredientInternal<?>> getClickedIngredient(IRecipeLayoutDrawable<?> recipeLayout, IRecipeSlotDrawable recipeSlot) {
		return recipeSlot.getDisplayedIngredient()
			.map(displayedIngredient -> {
				ImmutableRect2i area = absoluteClickedArea(recipeLayout, recipeSlot.getRect());
				IElement<?> element = new IngredientElement<>(displayedIngredient);
				return new ClickableIngredientInternal<>(element, area, false, true);
			});
	}

	/**
	 * Converts from relative recipeLayout coordinates to absolute screen coordinates
	 */
	private static ImmutableRect2i absoluteClickedArea(IRecipeLayoutDrawable<?> recipeLayout, Rect2i area) {
		Rect2i layoutArea = recipeLayout.getRect();

		return new ImmutableRect2i(
			area.getX() + layoutArea.getX(),
			area.getY() + layoutArea.getY(),
			area.getWidth(),
			area.getHeight()
		);
	}

	public Optional<IRecipeLayoutDrawable<?>> draw(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		IRecipeLayoutDrawable<?> hoveredLayout = null;

		Minecraft minecraft = Minecraft.getInstance();
		float partialTicks = minecraft.getDeltaFrameTime();

		for (RecipeLayoutWithButtons<?> recipeLayoutWithButtons : recipeLayoutsWithButtons) {
			IRecipeLayoutDrawable<?> recipeLayout = recipeLayoutWithButtons.getRecipeLayout();
			if (recipeLayout.isMouseOver(mouseX, mouseY)) {
				hoveredLayout = recipeLayout;
			}
			recipeLayout.drawRecipe(guiGraphics, mouseX, mouseY);

			RecipeTransferButton transferButton = recipeLayoutWithButtons.getTransferButton();
			transferButton.draw(guiGraphics, mouseX, mouseY, partialTicks);

			RecipeBookmarkButton bookmarkButton = recipeLayoutWithButtons.getBookmarkButton();
			bookmarkButton.draw(guiGraphics, mouseX, mouseY, partialTicks);
		}
		RenderSystem.disableBlend();
		return Optional.ofNullable(hoveredLayout);
	}

	public void drawTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		for (RecipeLayoutWithButtons<?> recipeLayoutWithButtons : recipeLayoutsWithButtons) {
			recipeLayoutWithButtons.getTransferButton().drawTooltips(guiGraphics, mouseX, mouseY);
			recipeLayoutWithButtons.getBookmarkButton().drawTooltips(guiGraphics, mouseX, mouseY);
		}
	}

	public int getWidth() {
		RecipeLayoutWithButtons<?> first = this.recipeLayoutsWithButtons.get(0);
		if (first == null) {
			return 0;
		}
		return first.totalWidth();
	}
}
