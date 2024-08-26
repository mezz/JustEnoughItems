package mezz.jei.ingredients;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.collect.IngredientSet;

import java.util.Collection;

public class RegisteredIngredient<T> {
	private final IIngredientType<T> ingredientType;
	private final IIngredientHelper<T> ingredientHelper;
	private final IIngredientRenderer<T> ingredientRenderer;
	private final IngredientSet<T> ingredientSet;
	private final ListMultimap<String, String> aliases = ArrayListMultimap.create();

	public RegisteredIngredient(IIngredientType<T> ingredientType, Collection<T> ingredients, IIngredientHelper<T> ingredientHelper, IIngredientRenderer<T> ingredientRenderer) {
		this.ingredientType = ingredientType;
		this.ingredientHelper = ingredientHelper;
		this.ingredientRenderer = ingredientRenderer;

		this.ingredientSet = IngredientSet.create(ingredientHelper, UidContext.Ingredient);
		this.ingredientSet.addAll(ingredients);
	}

	public IIngredientType<T> getIngredientType() {
		return ingredientType;
	}

	public IIngredientHelper<T> getIngredientHelper() {
		return ingredientHelper;
	}

	public IIngredientRenderer<T> getIngredientRenderer() {
		return ingredientRenderer;
	}

	public IngredientSet<T> getIngredientSet() {
		return ingredientSet;
	}

	public void addAliases(T ingredient, Collection<String> ingredientAliases) {
		String uid = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		aliases.putAll(uid, ingredientAliases);
	}

	public Collection<String> getAliases(T ingredient) {
		String uid = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		return ImmutableList.copyOf(aliases.get(uid));
	}
}
