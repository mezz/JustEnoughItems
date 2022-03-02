package mezz.jei.ingredients;

import mezz.jei.api.ingredients.IExtractableIngredientHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.collect.AbstractIngredientSet;
import mezz.jei.collect.IngredientSet;
import mezz.jei.collect.StackSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class IngredientInfo<T> {
	private final IIngredientType<T> ingredientType;
	private final IIngredientHelper<T> ingredientHelper;
	private final IIngredientRenderer<T> ingredientRenderer;
	private final AbstractIngredientSet<T> ingredientSet;

	public IngredientInfo(RawIngredientInfo<T> info) {
		this(
			info.getIngredientType(),
			info.getAllIngredients(),
			info.getIngredientHelper(),
			info.getIngredientRenderer()
		);
	}

	public IngredientInfo(IIngredientType<T> ingredientType, Collection<T> ingredients, IIngredientHelper<T> ingredientHelper, IIngredientRenderer<T> ingredientRenderer) {
		this.ingredientType = ingredientType;
		this.ingredientHelper = ingredientHelper;
		this.ingredientRenderer = ingredientRenderer;

		if (ingredientHelper instanceof IExtractableIngredientHelper<T, ?> extractableIngredientHelper) {
			this.ingredientSet = new StackSet<>(extractableIngredientHelper);
		} else {
			this.ingredientSet = IngredientSet.create(ingredientHelper, UidContext.Ingredient);
		}
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

	public Set<T> getAllIngredients() {
		return Collections.unmodifiableSet(ingredientSet);
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
}
