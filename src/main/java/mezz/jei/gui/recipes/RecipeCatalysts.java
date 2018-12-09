package mezz.jei.gui.recipes;

import mezz.jei.Internal;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.Constants;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The area drawn on left side of the {@link RecipesGui} that shows which items can craft the current recipe category.
 */
public class RecipeCatalysts implements IShowsRecipeFocuses {
	private final IDrawable topDrawable;
	private final IDrawable middleDrawable;
	private final IDrawable bottomDrawable;

	private final List<GuiIngredient<Object>> ingredients;
	private int left = 0;
	private int top = 0;

	public RecipeCatalysts() {
		ingredients = new ArrayList<>();

		ResourceLocation recipeBackgroundResource = Constants.RECIPE_BACKGROUND;

		IGuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
		topDrawable = guiHelper.createDrawable(recipeBackgroundResource, 196, 65, 26, 6);
		middleDrawable = guiHelper.createDrawable(recipeBackgroundResource, 196, 71, 26, 16);
		bottomDrawable = guiHelper.createDrawable(recipeBackgroundResource, 196, 87, 26, 6);
	}

	public boolean isEmpty() {
		return this.ingredients.isEmpty();
	}

	public int getWidth() {
		return 22; // hard-coded for now, may be dynamic in the future if we have multiple columns
	}

	public void updateLayout(List<Object> ingredients, RecipesGui recipesGui) {
		this.ingredients.clear();

		if (!ingredients.isEmpty()) {
			int totalHeight = topDrawable.getHeight() + middleDrawable.getHeight() + bottomDrawable.getHeight();
			int ingredientCount = 1;

			final int extraBoxHeight = middleDrawable.getHeight();
			for (int i = 1; i < ingredients.size(); i++) {
				if (totalHeight + extraBoxHeight <= (recipesGui.getYSize() - 8)) {
					totalHeight += extraBoxHeight;
					ingredientCount++;
				} else {
					break;
				}
			}

			top = recipesGui.getGuiTop();
			left = recipesGui.getGuiLeft() - topDrawable.getWidth() + 4; // overlaps the recipe gui slightly

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
		Rectangle rect = new Rectangle(left + 6, top + 6 + (index * middleDrawable.getHeight()), 16, 16);
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
				int top = this.top;
				topDrawable.draw(minecraft, this.left, top);
				top += topDrawable.getHeight();

				while (ingredientCount-- > 0) {
					middleDrawable.draw(minecraft, this.left, top);
					top += middleDrawable.getHeight();
				}

				bottomDrawable.draw(minecraft, this.left, top);
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
