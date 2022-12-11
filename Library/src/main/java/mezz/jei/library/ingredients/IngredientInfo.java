package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientInfo;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;

import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class IngredientInfo<T> implements IIngredientInfo<T> {
	private final IIngredientType<T> ingredientType;
	private final IIngredientHelper<T> ingredientHelper;
	private final IIngredientRenderer<T> ingredientRenderer;
	private final IngredientSet<T> ingredientSet;

	public IngredientInfo(IIngredientType<T> ingredientType, Collection<T> ingredients, IIngredientHelper<T> ingredientHelper, IIngredientRenderer<T> ingredientRenderer) {
		this.ingredientType = ingredientType;
		this.ingredientHelper = ingredientHelper;
		this.ingredientRenderer = ingredientRenderer;

		this.ingredientSet = IngredientSet.create(ingredientHelper, UidContext.Ingredient);
		this.ingredientSet.addAll(ingredients);
	}

	@Override
	public IIngredientType<T> getIngredientType() {
		return ingredientType;
	}

	@Override
	public IIngredientHelper<T> getIngredientHelper() {
		return ingredientHelper;
	}

	@Override
	public IIngredientRenderer<T> getIngredientRenderer() {
		return ingredientRenderer;
	}

	@Override
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

	@Override
	public Optional<T> getIngredientByUid(String uid) {
		return ingredientSet.getByUid(uid);
	}
}
