package mezz.jei.library.deprecated.gui.ingredients.adapters;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.library.gui.ingredients.RecipeSlots;
import mezz.jei.library.render.ItemStackRenderer;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("removal")
@Deprecated
public class RecipeSlotsGuiItemStackGroupAdapter extends RecipeSlotsGuiIngredientGroupAdapter<ItemStack> implements IGuiItemStackGroup {
	private static final ItemStackRenderer RENDERER = new ItemStackRenderer();

	public RecipeSlotsGuiItemStackGroupAdapter(
		RecipeSlots recipeSlots,
		IIngredientManager ingredientManager,
		IIngredientVisibility ingredientVisibility,
		int ingredientCycleOffset
	) {
		super(recipeSlots, ingredientManager, VanillaTypes.ITEM_STACK, ingredientVisibility, ingredientCycleOffset);
	}

	@Override
	public void init(int ingredientIndex, boolean input, int xPosition, int yPosition) {
		init(ingredientIndex, input, RENDERER, xPosition, yPosition, 18, 18, 1, 1);
	}
}
