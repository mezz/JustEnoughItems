package mezz.jei.api.ingredients;

/**
 * A type of ingredient (i.e. ItemStack, FluidStack, etc) handled by JEI that has subtypes and a base type.
 *
 * For example, the base type of ItemStack is Item and the base type of FluidStack is Fluid.
 * Items like the Enchanted Book have subtypes that are determined by NBT on the ItemStack.
 *
 * @since 9.7.0
 */
public interface IIngredientTypeWithSubtypes<B, I> extends IIngredientType<I> {
	/**
	 * @return The class of the ingredient for this type. (For example, ItemStack.class)
	 *
	 * @since 9.7.0
	 */
	@Override
	Class<? extends I> getIngredientClass();

	/**
	 * @return The class of the base ingredient for this type. (For example, Item.class)
	 * Base ingredients must be unique, comparable using ==.
	 *
	 * @since 9.7.0
	 */
	Class<? extends B> getIngredientBaseClass();

	/**
	 * @return the base ingredient for the given ingredient.
	 * For example, the base of ItemStack returns an Item, and the base of FluidStack returns a Fluid.
	 *
	 * @since 9.7.0
	 */
	B getBase(I ingredient);
}
