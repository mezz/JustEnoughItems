package mezz.jei.common.util;

import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.common.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickedIngredient;
import net.minecraft.world.item.ItemStack;

public class CheatUtil {
	public static <T> ItemStack getCheatItemStack(IClickedIngredient<T> clickedIngredient) {
		if (clickedIngredient.allowsCheating()) {
			IRegisteredIngredients registeredIngredients = Internal.getRegisteredIngredients();
			ITypedIngredient<T> value = clickedIngredient.getTypedIngredient();
			IIngredientHelper<T> ingredientHelper = registeredIngredients.getIngredientHelper(value.getType());
			return ingredientHelper.getCheatItemStack(value.getIngredient());
		}
		return ItemStack.EMPTY;
	}
}
