package mezz.jei.gui.recipes;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.elements.DrawableNineSliceTexture;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.util.MathUtil;

/**
 * The area drawn on left side of the {@link RecipesGui} that shows which items can craft the current recipe category.
 */
public class RecipeCatalysts implements IShowsRecipeFocuses {
	private static final int ingredientSize = 16;
	private static final int ingredientBorderSize = 1;
	private static final int borderSize = 5;
	private static final int overlapSize = 6;

	private final DrawableNineSliceTexture backgroundTab;

	private final List<GuiIngredient<Object>> ingredients;
	private final DrawableNineSliceTexture slotBackground;
	private int left = 0;
	private int top = 0;
	private int width = 0;
	private int height = 0;

	public RecipeCatalysts() {
		ingredients = new ArrayList<>();

		GuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
		backgroundTab = guiHelper.getCatalystTab();
		slotBackground = guiHelper.getNineSliceSlot();
	}

	public boolean isEmpty() {
		return this.ingredients.isEmpty();
	}

	public int getWidth() {
		return width - overlapSize;
	}

	public void updateLayout(List<Object> ingredients, RecipesGui recipesGui) {
		this.ingredients.clear();

		if (!ingredients.isEmpty()) {
			int availableHeight = recipesGui.getYSize() - 8;
			int borderHeight = (2 * borderSize) + (2 * ingredientBorderSize);
			int maxIngredientsPerColumn = (availableHeight - borderHeight) / ingredientSize;
			int columnCount = MathUtil.divideCeil(ingredients.size(), maxIngredientsPerColumn);
			maxIngredientsPerColumn = MathUtil.divideCeil(ingredients.size(), columnCount);

			width = (2 * ingredientBorderSize) + (borderSize * 2) + (columnCount * ingredientSize);
			height = (2 * ingredientBorderSize) + (borderSize * 2) + (maxIngredientsPerColumn * ingredientSize);
			top = recipesGui.getGuiTop();
			left = recipesGui.getGuiLeft() - width + overlapSize; // overlaps the recipe gui slightly

			for (int i = 0; i < ingredients.size(); i++) {
				Object ingredientForSlot = ingredients.get(i);
				GuiIngredient<Object> guiIngredient = createGuiIngredient(ingredientForSlot, i, maxIngredientsPerColumn);
				this.ingredients.add(guiIngredient);
			}
		}
	}

	private <T> GuiIngredient<T> createGuiIngredient(T ingredient, int index, int maxIngredientsPerColumn) {
		IngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		IIngredientRenderer<T> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredient);
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		int column = index / maxIngredientsPerColumn;
		int row = index % maxIngredientsPerColumn;
		Rectangle rect = new Rectangle(
			left + borderSize + (column * ingredientSize) + ingredientBorderSize,
			top + borderSize + (row * ingredientSize) + ingredientBorderSize,
			ingredientSize,
			ingredientSize
		);
		GuiIngredient<T> guiIngredient = new GuiIngredient<>(index, true, ingredientRenderer, ingredientHelper, rect, 0, 0, 0);
		guiIngredient.set(Collections.singletonList(ingredient), null);
		return guiIngredient;
	}

	@Nullable
	public GuiIngredient draw(Minecraft minecraft, int mouseX, int mouseY) {
		int ingredientCount = ingredients.size();
		if (ingredientCount > 0) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			GlStateManager.disableDepth();
			GlStateManager.enableAlpha();
			{
				int slotWidth = width - (2 * borderSize);
				int slotHeight = height - (2 * borderSize);
				backgroundTab.draw(minecraft, this.left, this.top, width, height);
				slotBackground.draw(minecraft, this.left + borderSize, this.top + borderSize, slotWidth, slotHeight);
			}
			GlStateManager.disableAlpha();
			GlStateManager.enableDepth();

			GuiIngredient hovered = null;
			for (GuiIngredient guiIngredient : this.ingredients) {
				if (guiIngredient.isMouseOver(0, 0, mouseX, mouseY)) {
					hovered = guiIngredient;
				}
				guiIngredient.draw(minecraft, 0, 0);
			}
			return hovered;
		}
		return null;
	}

	@Nullable
	private GuiIngredient getHovered(int mouseX, int mouseY) {
		for (GuiIngredient guiIngredient : this.ingredients) {
			if (guiIngredient.isMouseOver(0, 0, mouseX, mouseY)) {
				return guiIngredient;
			}
		}
		return null;
	}

	@Nullable
	@Override
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
		GuiIngredient hovered = getHovered(mouseX, mouseY);
		if (hovered != null) {
			Object ingredientUnderMouse = hovered.getDisplayedIngredient();
			if (ingredientUnderMouse != null) {
				return ClickedIngredient.create(ingredientUnderMouse, hovered.getRect());
			}
		}
		return null;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return true;
	}
}
