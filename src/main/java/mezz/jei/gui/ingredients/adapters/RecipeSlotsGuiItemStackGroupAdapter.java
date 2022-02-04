package mezz.jei.gui.ingredients.adapters;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.ingredients.GuiIngredientProperties;
import mezz.jei.gui.ingredients.RecipeSlots;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;
import net.minecraft.world.item.ItemStack;

public class RecipeSlotsGuiItemStackGroupAdapter extends RecipeSlotsGuiIngredientGroupAdapter<ItemStack> implements IGuiItemStackGroup {
	private static final ItemStackRenderer renderer = new ItemStackRenderer();

	public RecipeSlotsGuiItemStackGroupAdapter(RecipeSlots recipeSlots, IIngredientManager ingredientManager, int cycleOffset) {
		super(recipeSlots, ingredientManager, VanillaTypes.ITEM, cycleOffset);
	}

	@Override
	public void init(int ingredientIndex, boolean input, int xPosition, int yPosition) {
		init(ingredientIndex, input, renderer, xPosition, yPosition, GuiIngredientProperties.getWidth(1), GuiIngredientProperties.getHeight(1), 1, 1);
	}
}
