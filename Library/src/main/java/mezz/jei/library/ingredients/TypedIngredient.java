package mezz.jei.library.ingredients;

import com.google.common.base.Preconditions;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class TypedIngredient<T> implements ITypedIngredient<T> {
	private static <T> boolean checkIsValidIngredient(IIngredientManager ingredientManager, IIngredientType<T> ingredientType, T ingredient) {
		Preconditions.checkNotNull(ingredientType, "ingredientType");
		Preconditions.checkNotNull(ingredient, "ingredient");

		Class<? extends T> ingredientClass = ingredientType.getIngredientClass();
		if (!ingredientClass.isInstance(ingredient)) {
			throw new IllegalArgumentException("Invalid ingredient found. " +
				" Should be an instance of: " + ingredientClass + " Instead got: " + ingredient.getClass());
		}

		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		try {
			return ingredientHelper.isValidIngredient(ingredient);
		} catch (RuntimeException e) {
			String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
			throw new IllegalArgumentException("Crashing ingredient found. Ingredient Info: " + ingredientInfo, e);
		}
	}

	public static <T> Optional<ITypedIngredient<?>> create(IIngredientManager ingredientManager, @Nullable T ingredient) {
		if (ingredient == null) {
			return Optional.empty();
		}
		return ingredientManager.getIngredientTypeChecked(ingredient)
			.filter(ingredientType -> checkIsValidIngredient(ingredientManager, ingredientType, ingredient))
			.map(ingredientType -> new TypedIngredient<>(ingredientType, ingredient));
	}

	public static <T> Optional<ITypedIngredient<?>> create(IIngredientManager ingredientManager, IIngredientType<T> ingredientType, @Nullable T ingredient) {
		if (ingredient == null) {
			return Optional.empty();
		}
		if (!checkIsValidIngredient(ingredientManager, ingredientType, ingredient)) {
			return Optional.empty();
		}

		TypedIngredient<T> typedIngredient = new TypedIngredient<>(ingredientType, ingredient);
		return Optional.of(typedIngredient);
	}

	public static <T> Optional<ITypedIngredient<T>> createTyped(IIngredientManager ingredientManager, IIngredientType<T> ingredientType, @Nullable T ingredient) {
		if (ingredient == null) {
			return Optional.empty();
		}
		if (!checkIsValidIngredient(ingredientManager, ingredientType, ingredient)) {
			return Optional.empty();
		}
		TypedIngredient<T> typedIngredient = new TypedIngredient<>(ingredientType, ingredient);
		return Optional.of(typedIngredient);
	}

	public static <T> Optional<ITypedIngredient<T>> deepCopy(IIngredientManager ingredientManager, ITypedIngredient<T> value) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		T ingredient = ingredientHelper.copyIngredient(value.getIngredient());
		return TypedIngredient.createTyped(ingredientManager, value.getType(), ingredient);
	}

	private final IIngredientType<T> ingredientType;
	private final T ingredient;

	private TypedIngredient(IIngredientType<T> ingredientType, T ingredient) {
		this.ingredientType = ingredientType;
		this.ingredient = ingredient;
	}

	@Override
	public <V> Optional<V> getIngredient(IIngredientType<V> ingredientType) {
		if (this.ingredientType == ingredientType) {
			@SuppressWarnings("unchecked")
			V castIngredient = (V) this.ingredient;
			return Optional.of(castIngredient);
		}
		return Optional.empty();
	}

	@Override
	public T getIngredient() {
		return this.ingredient;
	}

	@Override
	public IIngredientType<T> getType() {
		return this.ingredientType;
	}

}
