package mezz.jei.gui.recipes;

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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The area drawn on left side of the {@link RecipesGui} that shows which items can craft the current recipe category.
 */
public class RecipeCatalysts implements IShowsRecipeFocuses {
	private static final int ingredientSize = 16;
	private static final int borderSize = 6;
	private static final int overlapSize = 6;

	private final DrawableNineSliceTexture backgroundTab;

	private final List<GuiIngredient<Object>> ingredients;
	private final DrawableNineSliceTexture slotBackground;
	private int left = 0;
	private int top = 0;

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
		return ingredientSize + borderSize; // hard-coded for now, may be dynamic in the future if we have multiple columns
	}

	public void updateLayout(List<Object> ingredients, RecipesGui recipesGui) {
		this.ingredients.clear();

		if (!ingredients.isEmpty()) {
			int totalHeight = (2 * borderSize) + ingredientSize;
			int ingredientCount = 1;

			final int extraBoxHeight = ingredientSize;
			for (int i = 1; i < ingredients.size(); i++) {
				if (totalHeight + extraBoxHeight <= (recipesGui.getYSize() - 8)) {
					totalHeight += extraBoxHeight;
					ingredientCount++;
				} else {
					break;
				}
			}

			top = recipesGui.getGuiTop();
			left = recipesGui.getGuiLeft() - (ingredientSize + (borderSize * 2)) + overlapSize; // overlaps the recipe gui slightly

			List<Object> ingredientsForSlots = new ArrayList<>();
			for (int i = 0; i < ingredients.size() && i < ingredientCount; i++) {
				Object ingredient = ingredients.get(i);
				ingredientsForSlots.add(ingredient);
			}

			for (int i = 0; i < ingredientCount; i++) {
				Object ingredientForSlot = ingredientsForSlots.get(i);
				GuiIngredient<Object> guiIngredient = createGuiIngredient(ingredientForSlot, i);
				this.ingredients.add(guiIngredient);
			}
		}
	}

	private <T> GuiIngredient<T> createGuiIngredient(T ingredient, int index) {
		IngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		IIngredientRenderer<T> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredient);
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		Rectangle rect = new Rectangle(left + borderSize, top + borderSize + (index * ingredientSize), ingredientSize, ingredientSize);
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
				int width = (2 * borderSize) + ingredientSize;
				int height = (2 * borderSize) + (ingredientSize * ingredientCount);
				backgroundTab.draw(minecraft, this.left, this.top, width, height);
				int slotBorderSize = borderSize - 1;
				slotBackground.draw(minecraft, this.left + slotBorderSize, this.top + slotBorderSize, width - (2 * slotBorderSize), height - (2 * slotBorderSize));
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
