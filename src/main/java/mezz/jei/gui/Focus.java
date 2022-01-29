package mezz.jei.gui;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.util.ErrorUtil;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class Focus<V> implements IFocus<V> {
	private final RecipeIngredientRole role;
	private final V value;

	public Focus(IFocus.Mode mode, V value) {
		ErrorUtil.checkNotNull(mode, "focus mode");
		ErrorUtil.checkIsValidIngredient(value, "focus value");
		this.role = mode.toRole();
		IIngredientHelper<V> ingredientHelper = Internal.getIngredientManager().getIngredientHelper(value);
		this.value = ingredientHelper.copyIngredient(value);
	}

	public Focus(RecipeIngredientRole role, V value) {
		ErrorUtil.checkNotNull(role, "focus role");
		ErrorUtil.checkIsValidIngredient(value, "focus value");
		this.role = role;
		IIngredientHelper<V> ingredientHelper = Internal.getIngredientManager().getIngredientHelper(value);
		this.value = ingredientHelper.copyIngredient(value);
	}

	@Override
	public V getValue() {
		return value;
	}

	@SuppressWarnings("deprecation")
	@Override
	public IFocus.Mode getMode() {
		return switch (role) {
			case INPUT, CATALYST -> IFocus.Mode.INPUT;
			case OUTPUT -> IFocus.Mode.OUTPUT;
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
		return new Focus<>(focus.getRole(), focus.getValue());
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
	@SuppressWarnings("unchecked")
	public static List<Focus<?>> check(Collection<? extends IFocus<?>> focuses) {
		List<? extends Focus<?>> result = focuses.stream()
			.filter(Objects::nonNull)
			.map(Focus::checkOne)
			.toList();
		return (List<Focus<?>>) result;
	}
}
