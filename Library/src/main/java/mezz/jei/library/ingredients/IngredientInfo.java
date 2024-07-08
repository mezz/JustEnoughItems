package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.core.collect.ListMultiMap;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;

public class IngredientInfo<T> {
	private final IIngredientType<T> ingredientType;
	private final IIngredientHelper<T> ingredientHelper;
	private final IIngredientRenderer<T> ingredientRenderer;
	private final IngredientSet<T> ingredientSet;
	private final ListMultiMap<String, String> aliases;
	private final ListMultiMap<Object, String> baseAliases;

	public IngredientInfo(IIngredientType<T> ingredientType, Collection<T> ingredients, IIngredientHelper<T> ingredientHelper, IIngredientRenderer<T> ingredientRenderer) {
		this.ingredientType = ingredientType;
		this.ingredientHelper = ingredientHelper;
		this.ingredientRenderer = ingredientRenderer;

		this.ingredientSet = new IngredientSet<>(ingredientHelper, UidContext.Ingredient);
		this.ingredientSet.addAll(ingredients);

		this.aliases = new ListMultiMap<>();
		this.baseAliases = new ListMultiMap<>(new IdentityHashMap<>(), ArrayList::new);
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
		Collection<String> ingredientAliases = aliases.get(uid);
		Collection<String> baseIngredientAliases = getBaseIngredientAliases(ingredient);
		if (ingredientAliases.isEmpty()) {
			return baseIngredientAliases;
		}
		if (baseIngredientAliases.isEmpty()) {
			return ingredientAliases;
		}
		List<String> combinedAliases = new ArrayList<>(ingredientAliases.size() + baseIngredientAliases.size());
		combinedAliases.addAll(ingredientAliases);
		combinedAliases.addAll(baseIngredientAliases);
		return Collections.unmodifiableList(combinedAliases);
	}

	public void addIngredientAlias(T ingredient, String alias) {
		String uid = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		this.aliases.put(uid, alias);
	}

	public void addIngredientAliases(T ingredient, Collection<String> aliases) {
		String uid = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		this.aliases.putAll(uid, aliases);
	}

	public void addBaseIngredientAlias(Object baseIngredient, String alias) {
		this.baseAliases.put(baseIngredient, alias);
	}

	public void addBaseIngredientAliases(Object baseIngredient, Collection<String> aliases) {
		this.baseAliases.putAll(baseIngredient, aliases);
	}

	@Unmodifiable
	private Collection<String> getBaseIngredientAliases(T ingredient) {
		if (ingredientType instanceof IIngredientTypeWithSubtypes<?, T> ingredientTypeWithSubtypes) {
			Object baseIngredient = ingredientTypeWithSubtypes.getBase(ingredient);
			return baseAliases.get(baseIngredient);
		}
		return Collections.emptyList();
	}
}
