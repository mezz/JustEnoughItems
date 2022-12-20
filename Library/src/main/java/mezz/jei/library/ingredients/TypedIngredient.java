package mezz.jei.library.ingredients;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class TypedIngredient<T> implements ITypedIngredient<T> {
	private static <T> void checkParameters(IIngredientType<T> ingredientType, T ingredient) {
		Preconditions.checkNotNull(ingredientType, "ingredientType");
		Preconditions.checkNotNull(ingredient, "ingredient");

		Class<? extends T> ingredientClass = ingredientType.getIngredientClass();
		if (!ingredientClass.isInstance(ingredient)) {
			throw new IllegalArgumentException("Invalid ingredient found. " +
				" Should be an instance of: " + ingredientClass + " Instead got: " + ingredient.getClass());
		}
	}

	public static <T> ITypedIngredient<T> createUnvalidated(IIngredientType<T> ingredientType, T ingredient) {
		checkParameters(ingredientType, ingredient);
		return new TypedIngredient<>(ingredientType, ingredient);
	}

	public static <T> Optional<ITypedIngredient<?>> createAndFilterInvalid(IIngredientManager ingredientManager, @Nullable T ingredient) {
		if (ingredient == null) {
			return Optional.empty();
		}
		return ingredientManager.getIngredientTypeChecked(ingredient)
			.flatMap(ingredientType -> createAndFilterInvalid(ingredientManager, ingredientType, ingredient));
	}

	public static <T> Optional<ITypedIngredient<T>> createAndFilterInvalid(IIngredientManager ingredientManager, IIngredientType<T> ingredientType, @Nullable T ingredient) {
		if (ingredient == null) {
			return Optional.empty();
		}
		checkParameters(ingredientType, ingredient);

		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		try {
			if (!ingredientHelper.isValidIngredient(ingredient)) {
				return Optional.empty();
			}
		} catch (RuntimeException e) {
			String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
			throw new IllegalArgumentException("Crashed when checking if ingredient is valid. Ingredient Info: " + ingredientInfo, e);
		}

		TypedIngredient<T> typedIngredient = new TypedIngredient<>(ingredientType, ingredient);
		return Optional.of(typedIngredient);
	}

	public static <T> Optional<ITypedIngredient<T>> deepCopy(IIngredientManager ingredientManager, ITypedIngredient<T> value) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		T ingredient = ingredientHelper.copyIngredient(value.getIngredient());
		return TypedIngredient.createAndFilterInvalid(ingredientManager, value.getType(), ingredient);
	}

	private final IIngredientType<T> ingredientType;
	private final T ingredient;

	private TypedIngredient(IIngredientType<T> ingredientType, T ingredient) {
		this.ingredientType = ingredientType;
		this.ingredient = ingredient;
	}

	@Override
	public T getIngredient() {
		return this.ingredient;
	}

	@Override
	public IIngredientType<T> getType() {
		return this.ingredientType;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("type", ingredientType)
			.add("ingredient", ingredient)
			.toString();
	}
}
