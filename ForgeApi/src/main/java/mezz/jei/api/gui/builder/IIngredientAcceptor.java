package mezz.jei.api.gui.builder;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A chainable interface that accepts typed ingredients.
 * Has convenience functions to make adding ingredients easier.
 *
 * @see IRecipeLayoutBuilder
 * @see IRecipeSlotBuilder
 *
 * @since 9.3.0
 */
public interface IIngredientAcceptor<THIS extends IIngredientAcceptor<THIS>> {
	/**
	 * Add an ordered list of ingredients.
	 *
	 * @since 9.3.0
	 */
	<I> THIS addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients);

	/**
	 * Add one ingredient.
	 *
	 * @since 9.3.0
	 */
	<I> THIS addIngredient(IIngredientType<I> ingredientType, I ingredient);

	/**
	 * Add an ordered list of ingredients.
	 * The type of ingredients can be mixed, as long as they are all valid ingredient types.
	 * Prefer using {@link #addIngredients(IIngredientType, List)} for type safety.
	 *
	 * @since 9.3.0
	 */
	THIS addIngredientsUnsafe(List<?> ingredients);

	/**
	 * Convenience function to add an ordered list of {@link ItemStack} from an {@link Ingredient}.
	 *
	 * @since 9.3.0
	 */
	default THIS addIngredients(Ingredient ingredient) {
		return addIngredients(VanillaTypes.ITEM_STACK, List.of(ingredient.getItems()));
	}

	/**
	 * Convenience function to add an order list of {@link ItemStack}.
	 *
	 * @since 9.3.0
	 */
	default THIS addItemStacks(List<ItemStack> itemStacks) {
		return addIngredients(VanillaTypes.ITEM_STACK, itemStacks);
	}

	/**
	 * Convenience function to add one {@link ItemStack}.
	 *
	 * @since 9.3.0
	 */
	default THIS addItemStack(ItemStack itemStack) {
		return addIngredient(VanillaTypes.ITEM_STACK, itemStack);
	}
}
