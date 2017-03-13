package mezz.jei.gui;

import com.google.common.base.Preconditions;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.util.ErrorUtil;
import net.minecraft.item.ItemStack;

public class Focus<V> implements IFocus<V> {
	private final Mode mode;
	private final V value;

	public Focus(Mode mode, V value) {
		this.mode = mode;
		IIngredientHelper<V> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(value);
		V valueCopy;
		try {
			valueCopy = ingredientHelper.copyIngredient(value);
		} catch (AbstractMethodError ignored) { // older ingredient helpers do not have this method
			valueCopy = value;
		}
		this.value = valueCopy;
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
		Preconditions.checkNotNull(focus, "focus must not be null");
		if (focus instanceof Focus) {
			checkInternal(focus);
			return (Focus<V>) focus;
		}
		return new Focus<V>(focus.getMode(), focus.getValue());
	}

	private static void checkInternal(IFocus<?> focus) {
		Preconditions.checkNotNull(focus.getMode(), "mode must not be null");
		Object value = focus.getValue();
		Preconditions.checkNotNull(value, "value must not be null");
		if (value instanceof ItemStack) {
			ItemStack itemStack = (ItemStack) value;
			ErrorUtil.checkNotEmpty(itemStack);
		}
	}
}
