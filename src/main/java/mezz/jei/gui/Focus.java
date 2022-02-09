package mezz.jei.gui;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.util.ErrorUtil;

import org.jetbrains.annotations.Nullable;

public final class Focus<V> implements IFocus<V> {
	private final Mode mode;
	private final V value;

	public Focus(Mode mode, V value) {
		ErrorUtil.checkNotNull(mode, "focus mode");
		ErrorUtil.checkIsValidIngredient(value, "focus value");
		this.mode = mode;
		IIngredientHelper<V> ingredientHelper = Internal.getIngredientManager().getIngredientHelper(value);
		this.value = ingredientHelper.copyIngredient(value);
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	/**
	 * Make sure any IFocus coming in through API calls is validated and turned into JEI's Focus.
	 */
	public static <V> Focus<V> check(IFocus<V> focus) {
		if (focus instanceof Focus) {
			return (Focus<V>) focus;
		}
		ErrorUtil.checkNotNull(focus, "focus");
		return new Focus<>(focus.getMode(), focus.getValue());
	}

	/**
	 * Make sure any IFocus coming in through API calls is validated and turned into JEI's Focus.
	 */
	@Nullable
	public static <V> Focus<V> checkNullable(@Nullable IFocus<V> focus) {
		if (focus == null) {
			return null;
		}
		return check(focus);
	}

}
