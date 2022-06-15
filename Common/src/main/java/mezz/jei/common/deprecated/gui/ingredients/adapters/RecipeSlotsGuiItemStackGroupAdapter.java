package mezz.jei.common.deprecated.gui.ingredients.adapters;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.gui.ingredients.GuiIngredientProperties;
import mezz.jei.common.gui.ingredients.RecipeSlots;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.render.ItemStackRenderer;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings({"removal"})
@Deprecated
public class RecipeSlotsGuiItemStackGroupAdapter extends RecipeSlotsGuiIngredientGroupAdapter<ItemStack> implements IGuiItemStackGroup {
	private static final ItemStackRenderer renderer = new ItemStackRenderer();

	public RecipeSlotsGuiItemStackGroupAdapter(RecipeSlots recipeSlots, RegisteredIngredients registeredIngredients, IIngredientVisibility ingredientVisibility, int cycleOffset) {
		super(recipeSlots, registeredIngredients, VanillaTypes.ITEM_STACK, ingredientVisibility, cycleOffset);
	}

	@Override
	public void init(int ingredientIndex, boolean input, int xPosition, int yPosition) {
		init(ingredientIndex, input, renderer, xPosition, yPosition, GuiIngredientProperties.getWidth(1), GuiIngredientProperties.getHeight(1), 1, 1);
	}
}
