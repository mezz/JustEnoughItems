package mezz.jei.api.gui.builder;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.List;

/**
 * A chainable interface that accepts typed ingredients.
 * Has convenience functions to make adding ingredients easier.
 *
 * @see IRecipeLayoutBuilder
 * @see IRecipeLayoutSlotBuilder
 *
 * @since JEI 9.3.0
 */
public interface IIngredientAcceptor<THIS extends IIngredientAcceptor<THIS>> {
	/**
	 * Add an ordered list of ingredients.
	 *
	 * @since JEI 9.3.0
	 */
	<I> THIS addIngredients(IIngredientType<I> ingredientType, List<I> ingredients);

	/**
	 * Add one ingredient.
	 *
	 * @since JEI 9.3.0
	 */
	<I> THIS addIngredient(IIngredientType<I> ingredientType, I ingredient);

	/**
	 * Convenience function to add an order list of {@link ItemStack}.
	 *
	 * @since JEI 9.3.0
	 */
	default THIS addIngredients(List<ItemStack> itemStacks) {
		return addIngredients(VanillaTypes.ITEM, itemStacks);
	}

	/**
	 * Convenience function to add one {@link ItemStack}.
	 *
	 * @since JEI 9.3.0
	 */
	default THIS addIngredient(ItemStack itemStack) {
		return addIngredient(VanillaTypes.ITEM, itemStack);
	}

	/**
	 * Convenience function to add an ordered list of {@link ItemStack}
	 * from an {@link Ingredient}.
	 *
	 * @since JEI 9.3.0
	 */
	default THIS addIngredients(Ingredient ingredient) {
		return addIngredients(VanillaTypes.ITEM, Arrays.asList(ingredient.getItems()));
	}
}
