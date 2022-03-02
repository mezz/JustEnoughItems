package mezz.jei.api.ingredients;

/**
 *
 */
public interface IExtractableIngredientHelper<V, T> extends IIngredientHelper<V> {
	T extractImmutablePart(V ingredient);
}
