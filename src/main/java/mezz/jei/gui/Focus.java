package mezz.jei.gui;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.LegacyUtil;
import net.minecraft.item.ItemStack;

public class Focus<V> implements IFocus<V> {
	private final Mode mode;
	private final V value;

	public Focus(Mode mode, V value) {
		this.mode = mode;
		IIngredientHelper<V> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(value);
		this.value = LegacyUtil.getIngredientCopy(value, ingredientHelper);
		checkInternal(this);
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
		ErrorUtil.checkNotNull(focus, "focus");
		if (focus instanceof Focus) {
			checkInternal(focus);
			return (Focus<V>) focus;
		}
		return new Focus<>(focus.getMode(), focus.getValue());
	}

	private static void checkInternal(IFocus<?> focus) {
		ErrorUtil.checkNotNull(focus.getMode(), "focus mode");
		Object value = focus.getValue();
		ErrorUtil.checkIsValidIngredient(value, "focus value");
	}
}
