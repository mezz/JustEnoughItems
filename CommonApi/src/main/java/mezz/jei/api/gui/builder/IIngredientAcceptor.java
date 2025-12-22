package mezz.jei.api.gui.builder;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A chainable interface that accepts typed ingredients.
 * Has convenience functions to make adding ingredients easier.
 *
 * @see IRecipeLayoutBuilder
 * @see IRecipeSlotBuilder
 *
 * @since 9.3.0
 */
@ApiStatus.NonExtendable
public interface IIngredientAcceptor<THIS extends IIngredientAcceptor<THIS>> {
	/**
	 * Add a slot display.
	 *
	 * @since 20.0.0
	 */
	THIS add(SlotDisplay slotDisplay);

	/**
	 * Add one {@link ItemStack}.
	 *
	 * @since 20.0.0
	 */
	default THIS add(ItemStack itemStack) {
		return add(VanillaTypes.ITEM_STACK, itemStack);
	}

	/**
	 * Add one {@link ItemLike}.
	 *
	 * @since 20.0.0
	 */
	default THIS add(ItemLike itemLike) {
		return add(VanillaTypes.ITEM_STACK, itemLike.asItem().getDefaultInstance());
	}

	/**
	 * Convenience helper to add one Fluid ingredient with the default amount (one bucket).
	 *
	 * To add multiple Fluid ingredients, you can call this multiple times.
	 *
	 * @see #add(Fluid, long) to add a Fluid with an amount.
	 * @see #add(Fluid, long, DataComponentPatch) to add a Fluid with a {@link DataComponentPatch}.
	 * @since 20.0.0
	 */
	THIS add(Fluid fluid);

	/**
	 * Convenience helper to add one Fluid ingredient.
	 *
	 * To add multiple Fluid ingredients, you can call this multiple times.
	 *
	 * @see #add(Fluid, long) to add a Fluid with the default amount.
	 * @see #add(Fluid, long, DataComponentPatch) to add a Fluid with a {@link DataComponentPatch}.
	 * @since 20.0.0
	 */
	THIS add(Fluid fluid, long amount);

	/**
	 * Convenience helper to add one Fluid ingredient with a {@link DataComponentPatch}.
	 *
	 * To add multiple Fluid ingredients, you can call this multiple times.
	 *
	 * @see #add(Fluid, long) to add a Fluid with the default amount.
	 * @see #add(Fluid, long) to add a Fluid without a {@link DataComponentPatch}.
	 * @since 20.0.0
	 */
	THIS add(Fluid fluid, long amount, DataComponentPatch component);

	/**
	 * Convenience function to add an ordered list of {@link ItemStack} from an {@link Ingredient}.
	 *
	 * @since 20.0.0
	 */
	THIS add(Ingredient ingredient);

	/**
	 * Add one typed ingredient.
	 *
	 * @since 20.0.0
	 */
	default <I> THIS add(ITypedIngredient<I> typedIngredient) {
		return add(typedIngredient.getType(), typedIngredient.getIngredient());
	}

	/**
	 * Add one ingredient with a custom {@link IIngredientType}.
	 *
	 * @since 20.0.0
	 */
	<I> THIS add(IIngredientType<I> ingredientType, I ingredient);

	/**
	 * Add an ordered list of ingredients.
	 *
	 * @since 9.3.0
	 */
	<I> THIS addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients);

	/**
	 * Add an ordered list of ingredients.
	 * The type of ingredients can be mixed, as long as they are all valid ingredient types.
	 * Prefer using {@link #addIngredients(IIngredientType, List)} for type safety.
	 *
	 * @since 9.3.0
	 */
	THIS addIngredientsUnsafe(List<?> ingredients);

	/**
	 * Convenience function to add an ordered non-null list of typed ingredients.
	 *
	 * @param ingredients a non-null list of ingredients for the slot
	 *
	 * @since 19.6.0
	 */
	THIS addTypedIngredients(List<ITypedIngredient<?>> ingredients);

	/**
	 * Convenience function to add an ordered non-null list of typed ingredients.
	 * {@link Optional#empty()} ingredients will be shown as blank in the rotation.
	 *
	 * @param ingredients a non-null list of optional ingredients for the slot
	 *
	 * @since 19.6.0
	 */
	THIS addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients);

	/**
	 * Convenience function to add an order list of {@link ItemStack}.
	 *
	 * @since 9.3.0
	 */
	default THIS addItemStacks(List<ItemStack> itemStacks) {
		return addIngredients(VanillaTypes.ITEM_STACK, itemStacks);
	}

	/**
	 * Add one ingredient.
	 *
	 * @since 9.3.0
	 * @deprecated use {@link #add(IIngredientType, Object)}
	 */
	@Deprecated(forRemoval = true, since = "20.0.0")
	default <I> THIS addIngredient(IIngredientType<I> ingredientType, I ingredient) {
		return add(ingredientType, ingredient);
	}

	/**
	 * Convenience function to add an ordered list of {@link ItemStack} from an {@link Ingredient}.
	 *
	 * @since 9.3.0
	 * @deprecated use {@link #add(Ingredient)}
	 */
	@Deprecated(forRemoval = true, since = "20.0.0")
	default THIS addIngredients(Ingredient ingredient) {
		return add(ingredient);
	}

	/**
	 * Add one typed ingredient.
	 *
	 * @since 19.6.0
	 * @deprecated use {@link #add(ITypedIngredient)}
	 */
	@Deprecated(forRemoval = true, since = "20.0.0")
	default <I> THIS addTypedIngredient(ITypedIngredient<I> typedIngredient) {
		return add(typedIngredient);
	}

	/**
	 * Convenience function to add one {@link ItemStack}.
	 *
	 * @since 9.3.0
	 * @deprecated use {@link #add(ItemStack)}
	 */
	@Deprecated(forRemoval = true, since = "20.0.0")
	default THIS addItemStack(ItemStack itemStack) {
		return add(itemStack);
	}

	/**
	 * Convenience function to add one {@link ItemLike}.
	 *
	 * @since 19.18.1
	 * @deprecated use {@link #add(ItemLike)}
	 */
	@Deprecated(forRemoval = true, since = "20.0.0")
	default THIS addItemLike(ItemLike itemLike) {
		return add(itemLike);
	}

	/**
	 * Convenience helper to add one Fluid ingredient with the default amount (one bucket).
	 *
	 * To add multiple Fluid ingredients, you can call this multiple times.
	 *
	 * @see #add(Fluid, long) to add a Fluid with an amount.
	 * @see #add(Fluid, long, DataComponentPatch) to add a Fluid with a {@link DataComponentPatch}.
	 * @deprecated use {@link #add(Fluid)}
	 */
	@Deprecated(forRemoval = true, since = "20.0.0")
	default THIS addFluidStack(Fluid fluid) {
		return add(fluid);
	}

	/**
	 * Convenience helper to add one Fluid ingredient.
	 *
	 * To add multiple Fluid ingredients, you can call this multiple times.
	 *
	 * @see #add(Fluid, long) to add a Fluid with the default amount.
	 * @see #add(Fluid, long, DataComponentPatch) to add a Fluid with a {@link DataComponentPatch}.
	 * @since 11.1.0
	 * @deprecated use {@link #add(ItemLike)}
	 */
	@Deprecated(forRemoval = true, since = "20.0.0")
	default THIS addFluidStack(Fluid fluid, long amount) {
		return add(fluid, amount);
	}

	/**
	 * Convenience helper to add one Fluid ingredient with a {@link DataComponentPatch}.
	 *
	 * To add multiple Fluid ingredients, you can call this multiple times.
	 *
	 * @see #add(Fluid, long) to add a Fluid with the default amount.
	 * @see #add(Fluid, long) to add a Fluid without a {@link DataComponentPatch}.
	 * @since 18.0.0
	 * @deprecated use {@link #add(ItemLike)}
	 */
	@Deprecated(forRemoval = true, since = "20.0.0")
	default THIS addFluidStack(Fluid fluid, long amount, DataComponentPatch component) {
		return add(fluid, amount, component);
	}
}
