package mezz.jei.api.ingredients.subtypes;

import mezz.jei.api.registration.ISubtypeRegistration;

/**
 * A subtype interpreter tells JEI how to create unique ids for ingredients.
 *
 * For example, an ItemStack may have some NBT that is used to create many subtypes,
 * and other NBT that is used for electric charge that can be ignored.
 * You can tell JEI how to interpret these differences by implementing an
 * {@link IIngredientSubtypeInterpreter} and registering it with JEI in
 * {@link ISubtypeRegistration}
 *
 * @since 7.6.2
 * @deprecated use {@link ISubtypeInterpreter} instead.
 */
@Deprecated(since = "19.9.0", forRemoval = true)
@SuppressWarnings("DeprecatedIsStillUsed")
@FunctionalInterface
public interface IIngredientSubtypeInterpreter<T> {
	@Deprecated(since = "19.9.0", forRemoval = true)
	String NONE = "";

	/**
	 * Get the data from an ingredient that is relevant to telling subtypes apart in the given context.
	 * This should account for nbt, and anything else that's relevant.
	 *
	 * {@link UidContext} can be used to give different subtype information depending on the given context.
	 * Most cases will return the same value for all contexts, and it can usually be ignored.
	 *
	 * Return {@link #NONE} if there is no data used for subtypes.
	 *
	 * @deprecated use {@link ISubtypeInterpreter#getSubtypeData(Object, UidContext)} instead.
	 */
	@Deprecated(since = "19.9.0", forRemoval = true)
	String apply(T ingredient, UidContext context);
}
