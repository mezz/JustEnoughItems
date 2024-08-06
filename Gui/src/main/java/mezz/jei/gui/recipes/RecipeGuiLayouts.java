package mezz.jei.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
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

	public void updateLayout(ImmutableRect2i recipeLayoutsArea, final int recipesPerPage) {
		if (this.recipeLayoutsWithButtons.isEmpty()) {
			return;
		}
		RecipeLayoutWithButtons<?> firstLayout = this.recipeLayoutsWithButtons.get(0);
		ImmutableRect2i layoutAreaWithBorder = new ImmutableRect2i(firstLayout.recipeLayout().getRectWithBorder());
		final int recipeXOffset = getRecipeXOffset(layoutAreaWithBorder, recipeLayoutsArea);

		final int recipeHeight = layoutAreaWithBorder.getHeight();
		final int availableHeight = Math.max(recipeLayoutsArea.getHeight(), recipeHeight);
		final int remainingHeight = availableHeight - (recipesPerPage * recipeHeight);
		final int recipeSpacing = remainingHeight / (recipesPerPage + 1);

		final int spacingY = recipeHeight + recipeSpacing;
		int recipeYOffset = recipeLayoutsArea.getY() + recipeSpacing;
		for (RecipeLayoutWithButtons<?> recipeLayoutWithButtons : recipeLayoutsWithButtons) {
			IRecipeLayoutDrawable<?> recipeLayout = recipeLayoutWithButtons.recipeLayout();
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
			IRecipeLayoutDrawable<?> recipeLayout = recipeLayoutWithButtons.recipeLayout();
			Rect2i layoutArea = recipeLayout.getRect();

			{
				RecipeTransferButton button = recipeLayoutWithButtons.transferButton();
				Rect2i buttonArea = recipeLayout.getRecipeTransferButtonArea();
				buttonArea.setX(buttonArea.getX() + layoutArea.getX());
				buttonArea.setY(buttonArea.getY() + layoutArea.getY());
				button.updateBounds(buttonArea);
			}
			{
				RecipeBookmarkButton button = recipeLayoutWithButtons.bookmarkButton();
				Rect2i buttonArea = recipeLayout.getRecipeBookmarkButtonArea();
				buttonArea.setX(buttonArea.getX() + layoutArea.getX());
				buttonArea.setY(buttonArea.getY() + layoutArea.getY());
				button.updateBounds(buttonArea);
			}
		}
	}

	private int getRecipeXOffset(ImmutableRect2i layoutRect, ImmutableRect2i layoutsArea) {
		if (recipeLayoutsWithButtons.isEmpty()) {
			return layoutsArea.getX();
		}

		final int recipeWidth = layoutRect.getWidth();
		final int recipeWidthWithButtons = recipeLayoutsWithButtons.get(0).totalWidth();
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
				cachedInputHandler = new CombinedInputHandler("RecipeGuiLayouts", handlers);
			}
			return cachedInputHandler;
		});
	}

	public void tick(@Nullable AbstractContainerMenu parentContainer) {
		Player player = Minecraft.getInstance().player;
		for (RecipeLayoutWithButtons<?> recipeLayoutWithButtons : this.recipeLayoutsWithButtons) {
			recipeLayoutWithButtons.tick(parentContainer, player);
		}
	}

	public void setRecipeLayoutsWithButtons(List<RecipeLayoutWithButtons<?>> recipeLayoutsWithButtons) {
		this.recipeLayoutsWithButtons.clear();
		this.recipeLayoutsWithButtons.addAll(recipeLayoutsWithButtons);
		this.cachedInputHandler = null;
	}

	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		return this.recipeLayoutsWithButtons.stream()
			.map(RecipeLayoutWithButtons::recipeLayout)
			.map(recipeLayout -> recipeLayout.getSlotUnderMouse(mouseX, mouseY))
			.flatMap(Optional::stream)
			.map(RecipeGuiLayouts::getClickedIngredient)
			.flatMap(Optional::stream);
	}

	private static Optional<IClickableIngredientInternal<?>> getClickedIngredient(RecipeSlotUnderMouse slotUnderMouse) {
		return slotUnderMouse.slot().getDisplayedIngredient()
			.map(displayedIngredient -> {
				IElement<?> element = new IngredientElement<>(displayedIngredient);
				return new ClickableIngredientInternal<>(element, slotUnderMouse::isMouseOver, false, true);
			});
	}

	public boolean mouseDragged(double mouseX, double mouseY, InputConstants.Key input, double dragX, double dragY) {
		for (RecipeLayoutWithButtons<?> recipeLayoutWithButtons : recipeLayoutsWithButtons) {
			IRecipeLayoutDrawable<?> recipeLayout = recipeLayoutWithButtons.recipeLayout();
			if (mouseDragged(recipeLayout, mouseX, mouseY, input, dragX, dragY)) {
				return true;
			}
		}
		return false;
	}

	private <R> boolean mouseDragged(IRecipeLayoutDrawable<R> recipeLayout, double mouseX, double mouseY, InputConstants.Key input, double dragX, double dragY) {
		if (recipeLayout.isMouseOver(mouseX, mouseY)) {
			IJeiInputHandler inputHandler = recipeLayout.getInputHandler();
			return inputHandler.handleMouseDragged(mouseX, mouseY, input, dragX, dragY);
		}
		return false;
	}

	public void mouseMoved(double mouseX, double mouseY) {
		for (RecipeLayoutWithButtons<?> recipeLayoutWithButtons : recipeLayoutsWithButtons) {
			IRecipeLayoutDrawable<?> recipeLayout = recipeLayoutWithButtons.recipeLayout();
			if (recipeLayout.isMouseOver(mouseX, mouseY)) {
				IJeiInputHandler inputHandler = recipeLayout.getInputHandler();
				inputHandler.handleMouseMoved(mouseX, mouseY);
			}
		}
	}

	public Optional<IRecipeLayoutDrawable<?>> draw(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		IRecipeLayoutDrawable<?> hoveredLayout = null;

		Minecraft minecraft = Minecraft.getInstance();
		float partialTicks = minecraft.getDeltaFrameTime();

		for (RecipeLayoutWithButtons<?> recipeLayoutWithButtons : recipeLayoutsWithButtons) {
			IRecipeLayoutDrawable<?> recipeLayout = recipeLayoutWithButtons.recipeLayout();
			if (recipeLayout.isMouseOver(mouseX, mouseY)) {
				hoveredLayout = recipeLayout;
			}
			recipeLayout.drawRecipe(guiGraphics, mouseX, mouseY);

			RecipeTransferButton transferButton = recipeLayoutWithButtons.transferButton();
			transferButton.draw(guiGraphics, mouseX, mouseY, partialTicks);

			RecipeBookmarkButton bookmarkButton = recipeLayoutWithButtons.bookmarkButton();
			bookmarkButton.draw(guiGraphics, mouseX, mouseY, partialTicks);
		}
		RenderSystem.disableBlend();
		return Optional.ofNullable(hoveredLayout);
	}

	public void drawTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		for (RecipeLayoutWithButtons<?> recipeLayoutWithButtons : recipeLayoutsWithButtons) {
			recipeLayoutWithButtons.transferButton().drawTooltips(guiGraphics, mouseX, mouseY);
			recipeLayoutWithButtons.bookmarkButton().drawTooltips(guiGraphics, mouseX, mouseY);
		}
	}

	public int getWidth() {
		if (recipeLayoutsWithButtons.isEmpty()) {
			return 0;
		}
		RecipeLayoutWithButtons<?> first = this.recipeLayoutsWithButtons.get(0);
		return first.totalWidth();
	}
}
