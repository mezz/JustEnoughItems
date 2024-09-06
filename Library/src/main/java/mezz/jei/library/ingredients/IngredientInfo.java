package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.core.collect.ListMultiMap;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class IngredientInfo<T> {
	private final IIngredientType<T> ingredientType;
	private final IIngredientHelper<T> ingredientHelper;
	private final IIngredientRenderer<T> ingredientRenderer;
	private final IngredientSet<T> ingredientSet;
	private final ListMultiMap<String, String> aliases;

	public IngredientInfo(IIngredientType<T> ingredientType, Collection<T> ingredients, IIngredientHelper<T> ingredientHelper, IIngredientRenderer<T> ingredientRenderer) {
		this.ingredientType = ingredientType;
		this.ingredientHelper = ingredientHelper;
		this.ingredientRenderer = ingredientRenderer;

		this.ingredientSet = new IngredientSet<>(ingredientHelper, UidContext.Ingredient);
		this.ingredientSet.addAll(ingredients);

		this.aliases = new ListMultiMap<>();
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

	@Unmodifiable
	public Collection<T> getAllIngredients() {
		return Collections.unmodifiableCollection(ingredientSet);
	}

	public void addIngredients(Collection<T> ingredients) {
		this.ingredientSet.addAll(ingredients);
	}

	public void removeIngredients(Collection<T> ingredients) {
		this.ingredientSet.removeAll(ingredients);
	}

	public Optional<T> getIngredientByUid(String uid) {
		return ingredientSet.getByUid(uid);
	}

	@Unmodifiable
	public Collection<String> getIngredientAliases(T ingredient) {
		String uid = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		return aliases.get(uid);
	}

	public void addIngredientAlias(T ingredient, String alias) {
		String uid = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		this.aliases.put(uid, alias);
	}

	public void addIngredientAliases(T ingredient, Collection<String> aliases) {
		String uid = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		this.aliases.putAll(uid, aliases);
	}
}
