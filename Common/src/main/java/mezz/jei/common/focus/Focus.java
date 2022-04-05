package mezz.jei.common.focus;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.ingredients.TypedIngredient;
import mezz.jei.common.util.ErrorUtil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class Focus<V> implements IFocus<V>, IFocusGroup {
	private final RecipeIngredientRole role;
	private final ITypedIngredient<V> value;

	public Focus(RecipeIngredientRole role, ITypedIngredient<V> value) {
		ErrorUtil.checkNotNull(role, "focus role");
		ErrorUtil.checkNotNull(value, "focus value");
		this.role = role;
		this.value = value;
	}

	@Override
	public ITypedIngredient<V> getTypedValue() {
		return value;
	}

	@SuppressWarnings("removal")
	@Override
	public V getValue() {
		return value.getIngredient();
	}

	@SuppressWarnings({"removal"})
	@Override
	public IFocus.Mode getMode() {
		return switch (role) {
			case INPUT, CATALYST -> IFocus.Mode.INPUT;
			case OUTPUT, RENDER_ONLY -> IFocus.Mode.OUTPUT;
		};
	}

	@Override
	public RecipeIngredientRole getRole() {
		return role;
	}

	@Override
	public <T> Optional<IFocus<T>> checkedCast(IIngredientType<T> ingredientType) {
		if (value.getType() == ingredientType) {
			@SuppressWarnings("unchecked")
			Focus<T> cast = (Focus<T>) this;
			return Optional.of(cast);
		}
		return Optional.empty();
	}

	/**
	 * Make sure any IFocus coming in through API calls is validated and turned into JEI's Focus.
	 */
	public static <V> Focus<V> checkOne(IFocus<V> focus, RegisteredIngredients registeredIngredients) {
		if (focus instanceof Focus) {
			return (Focus<V>) focus;
		}
		ErrorUtil.checkNotNull(focus, "focus");
		@SuppressWarnings("removal")  // call the old function in case they don't implement the new getTypedValue yet
		V value = focus.getValue();
		ErrorUtil.checkNotNull(value, "focus value");

		IIngredientType<V> ingredientType = registeredIngredients.getIngredientType(value);
		return createFromApi(registeredIngredients, focus.getRole(), ingredientType, value);
	}

	public static <V> Focus<V> createFromApi(RegisteredIngredients registeredIngredients, RecipeIngredientRole role, IIngredientType<V> ingredientType, V value) {
		Optional<ITypedIngredient<V>> typedIngredient = TypedIngredient.createTyped(registeredIngredients, ingredientType, value)
			.flatMap(i -> TypedIngredient.deepCopy(registeredIngredients, i));

		if (typedIngredient.isEmpty()) {
			throw new IllegalArgumentException("Focus value is invalid: " + ErrorUtil.getIngredientInfo(value, ingredientType, registeredIngredients));
		}
		return new Focus<>(role, typedIngredient.get());
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public List<IFocus<?>> getAllFocuses() {
		return List.of(this);
	}

	@Override
	public Stream<IFocus<?>> getFocuses(RecipeIngredientRole role) {
		if (role == this.role) {
			return Stream.of(this);
		}
		return Stream.empty();
	}

	@Override
	public <T> Stream<IFocus<T>> getFocuses(IIngredientType<T> ingredientType) {
		return checkedCast(ingredientType).stream();
	}

	@Override
	public <T> Stream<IFocus<T>> getFocuses(IIngredientType<T> ingredientType, RecipeIngredientRole role) {
		if (role == this.role) {
			return getFocuses(ingredientType);
		}
		return Stream.empty();
	}
}
