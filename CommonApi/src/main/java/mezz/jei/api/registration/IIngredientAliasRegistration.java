package mezz.jei.api.registration;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

/**
 * Allows registration of search aliases for ingredients.
 * Search aliases allow mods to add alternative names for ingredients, to help players find them more easily.
 *
 * @since 19.10.0
 */
@ApiStatus.NonExtendable
public interface IIngredientAliasRegistration {
	/**
	 * Register a search alias for an ingredient.
	 * An alias may be a translation key.
	 *
	 * @since 21.2.0
	 */
	default void addAlias(ItemStack itemStack, String alias) {
		addAlias(VanillaTypes.ITEM_STACK, itemStack, alias);
	}

	/**
	 * Register a search alias for all subtypes of an item.
	 * An alias may be a translation key.
	 *
	 * @since 30.7.0
	 */
	default void addAlias(Item item, String alias) {
		addAlias(VanillaTypes.ITEM_STACK, item, alias);
	}

	/**
	 * Register a search alias for all subtypes of a fluid.
	 * An alias may be a translation key.
	 *
	 * @since 30.7.0
	 */
	void addAlias(Fluid fluid, String alias);

	/**
	 * Register a search alias for an ingredient.
	 * An alias may be a translation key.
	 *
	 * @since 19.10.0
	 */
	<I> void addAlias(IIngredientType<I> type, I ingredient, String alias);

	/**
	 * Register a search alias for all subtypes of a base ingredient.
	 * An alias may be a translation key.
	 *
	 * @since 30.7.0
	 */
	<B, I> void addAlias(IIngredientTypeWithSubtypes<B, I> type, B baseIngredient, String alias);

	/**
	 * Register a search alias for an ingredient.
	 * An alias may be a translation key.
	 *
	 * @since 19.10.0
	 */
	<I> void addAlias(ITypedIngredient<I> typedIngredient, String alias);

	/**
	 * Register multiple search aliases for an ingredient.
	 * An alias may be a translation key.
	 *
	 * @since 19.10.0
	 */
	<I> void addAliases(IIngredientType<I> type, I ingredient, Collection<String> aliases);

	/**
	 * Register multiple search aliases for all subtypes of an item.
	 * An alias may be a translation key.
	 *
	 * @since 30.7.0
	 */
	default void addAliases(Item item, Collection<String> aliases) {
		addAliases(VanillaTypes.ITEM_STACK, item, aliases);
	}

	/**
	 * Register multiple search aliases for all subtypes of a fluid.
	 * An alias may be a translation key.
	 *
	 * @since 30.7.0
	 */
	void addAliases(Fluid fluid, Collection<String> aliases);

	/**
	 * Register multiple search aliases for all subtypes of a base ingredient.
	 * An alias may be a translation key.
	 *
	 * @since 30.7.0
	 */
	<B, I> void addAliases(IIngredientTypeWithSubtypes<B, I> type, B baseIngredient, Collection<String> aliases);

	/**
	 * Register multiple search aliases for an ingredient.
	 * An alias may be a translation key.
	 *
	 * @since 19.10.0
	 */
	<I> void addAliases(ITypedIngredient<I> typedIngredient, Collection<String> aliases);

	/**
	 * Register a search aliases for multiple ingredients.
	 * An alias may be a translation key.
	 *
	 * @since 19.10.0
	 */
	<I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, String alias);

	/**
	 * Register a search aliases for multiple ingredients.
	 * An alias may be a translation key.
	 *
	 * @since 19.10.0
	 */
	<I> void addAliases(Collection<ITypedIngredient<I>> typedIngredients, String alias);

	/**
	 * Register multiple search aliases for multiple ingredients.
	 * An alias may be a translation key.
	 *
	 * @since 19.10.0
	 */
	<I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, Collection<String> aliases);

	/**
	 * Register multiple search aliases for multiple ingredients.
	 * An alias may be a translation key.
	 *
	 * @since 19.10.0
	 */
	<I> void addAliases(Collection<ITypedIngredient<I>> typedIngredients, Collection<String> aliases);
}
