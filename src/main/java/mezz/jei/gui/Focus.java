package mezz.jei.gui;

import javax.annotation.Nullable;

import mezz.jei.IngredientRegistry;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IFocus;

public class Focus<V> implements IFocus<V> {
	private final Mode mode;
	@Nullable
	private final V value;

	public Focus(@Nullable V value) {
		this.mode = Mode.NONE;
		this.value = value;
	}

	public Focus(Mode mode, @Nullable V value) {
		this.mode = mode;
		this.value = value;
	}

	@Nullable
	@Override
	public V getValue() {
		return value;
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	public static boolean areFocusesEqual(IFocus focus1, IFocus focus2) {
		if (focus1.getMode() == focus2.getMode()) {
			String uid1 = getUidForFocusValue(focus1);
			String uid2 = getUidForFocusValue(focus2);
			return uid1.equals(uid2);
		}
		return false;
	}

	private static <V> String getUidForFocusValue(IFocus<V> focus) {
		V value = focus.getValue();
		if (value != null) {
			IngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
			IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(value);
			return ingredientHelper.getUniqueId(value);
		}
		return "null";
	}
}
