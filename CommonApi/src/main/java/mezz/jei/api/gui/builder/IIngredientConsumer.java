package mezz.jei.api.gui.builder;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A chainable interface that accepts typed ingredients.
 * Has convenience functions to make adding ingredients easier.
 *
 * @see IRecipeLayoutBuilder
 * @see IRecipeSlotBuilder
 *
 * @apiNote this is meant to replace {@link IIngredientAcceptor} in future versions
 *
 * @since 15.12.1
 */
@ApiStatus.NonExtendable
public interface IIngredientConsumer {
	/**
	 * Add an ordered list of ingredients.
	 *
	 * @since 15.12.1
	 */
	<I> IIngredientConsumer addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients);

	/**
	 * Add one ingredient.
	 *
	 * @since 15.12.1
	 */
	<I> IIngredientConsumer addIngredient(IIngredientType<I> ingredientType, I ingredient);

	/**
	 * Add an ordered list of ingredients.
	 * The type of ingredients can be mixed, as long as they are all valid ingredient types.
	 * Prefer using {@link #addIngredients(IIngredientType, List)} for type safety.
	 *
	 * @since 15.12.1
	 */
	IIngredientConsumer addIngredientsUnsafe(List<?> ingredients);

	/**
	 * Convenience function to add an ordered list of {@link ItemStack} from an {@link Ingredient}.
	 *
	 * @since 15.12.1
	 */
	default IIngredientConsumer addIngredients(Ingredient ingredient) {
		return addIngredients(VanillaTypes.ITEM_STACK, List.of(ingredient.getItems()));
	}

	/**
	 * Add one typed ingredient.
	 *
	 * @since 15.12.1
	 */
	default <I> IIngredientConsumer addTypedIngredient(ITypedIngredient<I> typedIngredient) {
		return addIngredient(typedIngredient.getType(), typedIngredient.getIngredient());
	}

	/**
	 * Convenience function to add an ordered non-null list of typed ingredients.
	 *
	 * @param ingredients a non-null list of ingredients for the slot
	 *
	 * @since 15.12.1
	 */
	IIngredientConsumer addTypedIngredients(List<ITypedIngredient<?>> ingredients);

	/**
	 * Convenience function to add an ordered non-null list of typed ingredients.
	 * {@link Optional#empty()} ingredients will be shown as blank in the rotation.
	 *
	 * @param ingredients a non-null list of optional ingredients for the slot
	 *
	 * @since 15.12.1
	 */
	IIngredientConsumer addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients);

	/**
	 * Convenience function to add an order list of {@link ItemStack}.
	 *
	 * @since 15.12.1
	 */
	default IIngredientConsumer addItemStacks(List<ItemStack> itemStacks) {
		return addIngredients(VanillaTypes.ITEM_STACK, itemStacks);
	}

	/**
	 * Convenience function to add one {@link ItemStack}.
	 *
	 * @since 15.12.1
	 */
	default IIngredientConsumer addItemStack(ItemStack itemStack) {
		return addIngredient(VanillaTypes.ITEM_STACK, itemStack);
	}

	/**
	 * Convenience helper to add one Fluid ingredient.
	 *
	 * To add multiple Fluid ingredients, you can call this multiple times.
	 *
	 * @see #addFluidStack(Fluid, long, DataComponentPatch) to add a Fluid with a {@link DataComponentPatch}.
	 * @since 15.12.1
	 */
	IIngredientConsumer addFluidStack(Fluid fluid, long amount);

	/**
	 * Convenience helper to add one Fluid ingredient with a {@link DataComponentPatch}.
	 *
	 * To add multiple Fluid ingredients, you can call this multiple times.
	 *
	 * @see #addFluidStack(Fluid, long) to add a Fluid without a {@link DataComponentPatch}.
	 * @since 15.12.1
	 */
	IIngredientConsumer addFluidStack(Fluid fluid, long amount, DataComponentPatch component);
}
