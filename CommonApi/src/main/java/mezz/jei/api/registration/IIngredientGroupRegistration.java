package mezz.jei.api.registration;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.resources.Identifier;

import java.util.SequencedCollection;
import java.util.function.Predicate;

/**
 * The registration interface for ingredient groups in jei overlay
 */
public interface IIngredientGroupRegistration {

	/**
	 * Add ingredients to an ingredient group.
	 * Group with same id will be merged.
	 *
	 * @param id          the group id
	 * @param ingredients the ingredients in the group
	 * @implNote Group name translation keys will be {@code jei.group.<namespace>.<path>},
	 * with {@code /} in the path replaced by {@code .}.
	 */
	<T> void addGroup(Identifier id, SequencedCollection<? extends ITypedIngredient<? extends T>> ingredients);

	/**
	 * Add a dynamic ingredient selector to collect ingredients for an ingredient group.
	 * Group with same id will be merged.
	 *
	 * @param id             the group id
	 * @param ingredientType the ingredient type
	 * @param selector       the ingredient selector
	 * @param <T>            the ingredient type
	 */
	<T> void addGroupSelector(Identifier id, IIngredientType<T> ingredientType, Predicate<T> selector);

}
