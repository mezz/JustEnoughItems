package mezz.jei.gui.util;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.input.IClickableIngredientInternal;
import net.minecraft.world.item.ItemStack;

public class CheatUtil {
	private final IIngredientManager ingredientManager;

	public CheatUtil(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	public <T> ItemStack getCheatItemStack(IClickableIngredientInternal<T> clickedIngredient) {
		if (clickedIngredient.allowsCheating()) {
			ITypedIngredient<T> value = clickedIngredient.getTypedIngredient();
			IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
			return ingredientHelper.getCheatItemStack(value.getIngredient());
		}
		return ItemStack.EMPTY;
	}
}
