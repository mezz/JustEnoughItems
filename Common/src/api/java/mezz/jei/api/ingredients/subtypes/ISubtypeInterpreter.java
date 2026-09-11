package mezz.jei.api.ingredients.subtypes;

import mezz.jei.api.registration.ISubtypeRegistration;
import org.jetbrains.annotations.Nullable;

/**
 * A subtype interpreter tells JEI how to create unique ids for ingredients.
 *
 * For example, an ItemStack may have some NBT that is used to create many subtypes,
 * and other NBT that is used for electric charge that can be ignored.
 * You can tell JEI how to interpret these differences by implementing an
 * {@link ISubtypeInterpreter} and registering it with JEI in {@link ISubtypeRegistration}
 *
 * @since 19.9.0
 */
public interface ISubtypeInterpreter<T> {
	/**
	 * Get the data from an ingredient that is relevant to telling subtypes of a given ingredient apart.
	 * This should account for components, and anything else that's relevant.
	 *
	 * The returned value must implement {@link Object#equals} and {@link Object#hashCode}
	 * for use as map keys and for comparisons with other objects.
	 *
	 * {@link UidContext} can be used to give different subtype information depending on the given context.
	 * Most cases will return the same value for all contexts, and it can usually be ignored.
	 *
	 * Return null if there is no data used for subtypes.
	 *
	 * @since 19.9.0
	 */
	@Nullable
	Object getSubtypeData(T ingredient, UidContext context);

	/**
	 * Provide a legacy string uid like from {@link IIngredientSubtypeInterpreter#apply},
	 * used for loading old config files that used string UIDs for serialization.
	 *
	 * Return an empty string if there is no data used for subtypes or if you have no legacy data to support.
	 *
	 * @since 19.9.0
	 */
	@SuppressWarnings({"removal"})
	@Deprecated(since = "19.9.0")
	String getLegacyStringSubtypeInfo(T ingredient, UidContext context);
}
