package mezz.jei.api.ingredients.subtypes;

/**
 * A subtype interpreter tells JEI how to create unique ids for ingredients.
 *
 * For example, an ItemStack may have some NBT that is used to create many subtypes,
 * and other NBT that is used for electric charge that can be ignored.
 * You can tell JEI how to interpret these differences by implementing an
 * {@link IIngredientSubtypeInterpreter} and registering it with JEI in
 * {@link mezz.jei.api.registration.ISubtypeRegistration}
 *
 * @since JEI 7.6.2
 */
@FunctionalInterface
public interface IIngredientSubtypeInterpreter<T> {
    String NONE = "";

    /**
     * Get the data from an ingredient that is relevant to telling subtypes apart in the given context.
     * This should account for nbt, and anything else that's relevant.
     *
     * {@link UidContext} can be used to give different subtype information depending on the given context.
     * Most cases will return the same value for all contexts and it can usually be ignored.
     *
     * Return {@link #NONE} if there is no data used for subtypes.
     */
    String apply(T ingredient, UidContext context);
}
