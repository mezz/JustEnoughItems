package mezz.jei.input;

import mezz.jei.api.ingredients.IIngredientRegistry;
import net.minecraft.item.ItemStack;

public interface IClickedIngredient<V> {

	V getValue();

	ItemStack getCheatItemStack(IIngredientRegistry ingredientRegistry);
}
