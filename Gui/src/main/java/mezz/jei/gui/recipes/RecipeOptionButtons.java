package mezz.jei.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.common.Internal;
import mezz.jei.common.config.RecipeSorterStage;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.handlers.CombinedInputHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnegative;
import java.util.List;

public class RecipeOptionButtons {
	private static final int buttonSize = 16;
	private static final int buttonBorderSize = 1;
	private static final int borderSize = 5;
	private static final int overlapSize = 6;

	private final List<GuiIconToggleButton> buttons;

	private final DrawableNineSliceTexture backgroundTab;
	private ImmutableRect2i area;

	public RecipeOptionButtons(Runnable onValueChanged) {
		Textures textures = Internal.getTextures();
		GuiIconToggleButton bookmarksFirstButton = new RecipeSortStateButton(
			RecipeSorterStage.BOOKMARKED,
			textures.getBookmarksFirst(),
			textures.getBookmarksFirst(),
			Component.translatable("jei.tooltip.recipe.sort.bookmarks.first.disabled"),
			Component.translatable("jei.tooltip.recipe.sort.bookmarks.first.enabled"),
			onValueChanged
		);
		GuiIconToggleButton craftableFirstButton = new RecipeSortStateButton(
			RecipeSorterStage.CRAFTABLE,
			textures.getCraftableFirst(),
			textures.getCraftableFirst(),
			Component.translatable("jei.tooltip.recipe.sort.craftable.first.disabled"),
			Component.translatable("jei.tooltip.recipe.sort.craftable.first.enabled"),
			onValueChanged
		);

		buttons = List.of(bookmarksFirstButton, craftableFirstButton);
		backgroundTab = textures.getRecipeOptionsTab();
	}

	public void tick() {
		for (GuiIconToggleButton button : buttons) {
			button.tick();
		}
	}

	public void updateLayout(ImmutableRect2i recipeArea) {
		int width = (2 * buttonBorderSize) + (borderSize * 2) + buttonSize;
		int height = (2 * buttonBorderSize) + (borderSize * 2) + (buttons.size() * buttonSize);
		int y = recipeArea.getY() + recipeArea.getHeight() - height;
		int x = recipeArea.getX() - width + overlapSize; // overlaps the recipe gui slightly

		this.area = new ImmutableRect2i(
			x,
			y,
			width,
			height
		);

		final int buttonX = x + borderSize + buttonBorderSize;
		for (int i = 0; i < buttons.size(); i++) {
			GuiIconToggleButton button = buttons.get(i);
			int buttonY = y + borderSize + (i * buttonSize) + buttonBorderSize;
			button.updateBounds(new ImmutableRect2i(buttonX, buttonY, buttonSize, buttonSize));
		}
	}

	public ImmutableRect2i getArea() {
		return area;
	}

	public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		RenderSystem.disableDepthTest();
		{
			backgroundTab.draw(guiGraphics, this.area);
		}
		RenderSystem.enableDepthTest();

		for (GuiIconToggleButton button : buttons) {
			button.draw(guiGraphics, mouseX, mouseY, partialTicks);
		}
	}

	@Nonnegative
	public int getWidth() {
		return Math.max(0, area.getWidth() - overlapSize);
	}

	public IUserInputHandler createInputHandler() {
		List<IUserInputHandler> handlers = buttons.stream()
			.map(GuiIconToggleButton::createInputHandler)
			.toList();
		return new CombinedInputHandler("RecipeOptionButtons", handlers);
	}

	public void drawTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		for (GuiIconToggleButton button : buttons) {
			button.drawTooltips(guiGraphics, mouseX, mouseY);
		}
	}
}
