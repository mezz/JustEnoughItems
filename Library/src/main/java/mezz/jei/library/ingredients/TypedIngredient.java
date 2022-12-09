package mezz.jei.library.ingredients;

import com.google.common.base.Preconditions;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class TypedIngredient<T> implements ITypedIngredient<T> {
	private static <T> void assertIsValidIngredient(IRegisteredIngredients registeredIngredients, IIngredientType<T> ingredientType, T ingredient) {
		Preconditions.checkNotNull(ingredientType, "ingredientType");
		Preconditions.checkNotNull(ingredient, "ingredient");

		Class<? extends T> ingredientClass = ingredientType.getIngredientClass();
		if (!ingredientClass.isInstance(ingredient)) {
			throw new IllegalArgumentException("Invalid ingredient found. " +
				" Should be an instance of: " + ingredientClass + " Instead got: " + ingredient.getClass());
		}

		IIngredientHelper<T> ingredientHelper = registeredIngredients.getIngredientHelper(ingredientType);
		boolean valid;
		try {
			valid = ingredientHelper.isValidIngredient(ingredient);
		} catch (RuntimeException e) {
			String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
			throw new IllegalArgumentException("Invalid ingredient found. Ingredient Info: " + ingredientInfo, e);
		}

		if (!valid) {
			String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
			throw new IllegalArgumentException("Invalid ingredient found. Ingredient Info: " + ingredientInfo);
		}
	}

	public static <T> Optional<ITypedIngredient<?>> create(IRegisteredIngredients registeredIngredients, @Nullable T ingredient) {
		if (isBlankIngredient(ingredient)) {
			return Optional.empty();
		}
		IIngredientType<T> ingredientType = registeredIngredients.getIngredientType(ingredient)
			.orElseThrow(() -> new IllegalArgumentException("Invalid ingredient type: " + ingredient.getClass()));
		assertIsValidIngredient(registeredIngredients, ingredientType, ingredient);
		TypedIngredient<T> typedIngredient = new TypedIngredient<>(ingredientType, ingredient);
		return Optional.of(typedIngredient);
	}

	public static <T> Optional<ITypedIngredient<?>> create(IRegisteredIngredients registeredIngredients, IIngredientType<T> ingredientType, @Nullable T ingredient) {
		if (isBlankIngredient(ingredient)) {
			return Optional.empty();
		}
		assertIsValidIngredient(registeredIngredients, ingredientType, ingredient);
		TypedIngredient<T> typedIngredient = new TypedIngredient<>(ingredientType, ingredient);
		return Optional.of(typedIngredient);
	}

	public static <T> Optional<ITypedIngredient<T>> createTyped(IRegisteredIngredients registeredIngredients, IIngredientType<T> ingredientType, @Nullable T ingredient) {
		if (isBlankIngredient(ingredient)) {
			return Optional.empty();
		}
		assertIsValidIngredient(registeredIngredients, ingredientType, ingredient);
		TypedIngredient<T> typedIngredient = new TypedIngredient<>(ingredientType, ingredient);
		return Optional.of(typedIngredient);
	}

	public static <T> Optional<ITypedIngredient<T>> deepCopy(IRegisteredIngredients registeredIngredients, ITypedIngredient<T> value) {
		IIngredientHelper<T> ingredientHelper = registeredIngredients.getIngredientHelper(value.getType());
		T ingredient = ingredientHelper.copyIngredient(value.getIngredient());
		return TypedIngredient.createTyped(registeredIngredients, value.getType(), ingredient);
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

	private static boolean isBlankIngredient(@Nullable Object ingredient) {
		return ingredient == null ||
			ingredient instanceof ItemStack itemStack && itemStack.isEmpty();
	}
}
