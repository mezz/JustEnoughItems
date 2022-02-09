package mezz.jei.gui;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.ingredients.TypedIngredient;
import mezz.jei.util.ErrorUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Focus<V> implements IFocus<V> {
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

	/**
	 * Make sure any IFocus coming in through API calls is validated and turned into JEI's Focus.
	 */
	public static <V> Focus<V> checkOne(IFocus<V> focus) {
		if (focus instanceof Focus) {
			return (Focus<V>) focus;
		}
		ErrorUtil.checkNotNull(focus, "focus");
		@SuppressWarnings("removal")  // call the old function in case they don't implement the new getTypedValue yet
		V value = focus.getValue();

		IngredientManager ingredientManager = Internal.getIngredientManager();
		IIngredientType<V> ingredientType = ingredientManager.getIngredientType(value);
		return createFromApi(ingredientManager, focus.getRole(), ingredientType, value);
	}

	@SuppressWarnings("removal")
	public static <V> Focus<V> createFromLegacyApi(IIngredientManager ingredientManager, IFocus.Mode mode, V value) {
		IIngredientType<V> ingredientType = ingredientManager.getIngredientType(value);
		return createFromApi(ingredientManager, mode.toRole(), ingredientType, value);
	}

	public static <V> Focus<V> createFromApi(IIngredientManager ingredientManager, RecipeIngredientRole role, IIngredientType<V> ingredientType, V value) {
		Optional<ITypedIngredient<V>> typedIngredient = TypedIngredient.createTyped(ingredientManager, ingredientType, value)
			.flatMap(i -> TypedIngredient.deepCopy(ingredientManager, i));

		if (typedIngredient.isEmpty()) {
			throw new IllegalArgumentException("Focus value is invalid: " + ErrorUtil.getIngredientInfo(value, ingredientType));
		}
		return new Focus<>(role, typedIngredient.get());
	}

	/**
	 * Make sure any IFocus coming in through API calls is validated and turned into JEI's Focus.
	 */
	public static <V> List<Focus<?>> check(IFocus<V> focus) {
		return List.of(checkOne(focus));
	}

	/**
	 * Make sure any IFocus coming in through API calls is validated and turned into JEI's Focus.
	 */
	public static <V> List<Focus<?>> checkNullable(@Nullable IFocus<V> focus) {
		if (focus == null) {
			return List.of();
		}
		return check(focus);
	}

	/**
	 * Make sure any IFocus coming in through API calls is validated and turned into JEI's Focus.
	 */
	public static List<Focus<?>> check(Collection<? extends IFocus<?>> focuses) {
		return focuses.stream()
			.filter(Objects::nonNull)
			.<Focus<?>>map(Focus::checkOne)
			.toList();
	}
}
