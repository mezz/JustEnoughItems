package mezz.jei.input;

import javax.annotation.Nullable;
import java.awt.Rectangle;

import net.minecraft.item.ItemStack;

import com.google.common.base.MoreObjects;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClickedIngredient<V> implements IClickedIngredient<V> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final V value;
	@Nullable
	private final Rectangle area;
	@Nullable
	private IOnClickHandler onClickHandler;
	private boolean allowsCheating;

	@Nullable
	public static <V> ClickedIngredient<V> create(V value, @Nullable Rectangle area) {
		ErrorUtil.checkNotNull(value, "value");
		IngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(value);
		try {
			if (ingredientHelper.isValidIngredient(value)) {
				return new ClickedIngredient<>(value, area);
			}
			String ingredientInfo = ingredientHelper.getErrorInfo(value);
			LOGGER.error("Clicked invalid ingredient. Ingredient Info: {}", ingredientInfo);
		} catch (RuntimeException e) {
			String ingredientInfo = ingredientHelper.getErrorInfo(value);
			LOGGER.error("Clicked invalid ingredient. Ingredient Info: {}", ingredientInfo, e);
		}
		return null;
	}

	private ClickedIngredient(V value, @Nullable Rectangle area) {
		this.value = value;
		this.area = area;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Nullable
	@Override
	public Rectangle getArea() {
		return area;
	}

	public void setAllowsCheating() {
		this.allowsCheating = true;
	}

	@Override
	public void setOnClickHandler(IOnClickHandler onClickHandler) {
		this.onClickHandler = onClickHandler;
	}

	@Override
	public ItemStack getCheatItemStack() {
		if (allowsCheating) {
			IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
			IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(value);
			return ingredientHelper.getCheatItemStack(value);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void onClickHandled() {
		if (this.onClickHandler != null) {
			this.onClickHandler.onClick();
		}
	}

	@Override
	public String toString() {
		IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(value);
		return MoreObjects.toStringHelper(ClickedIngredient.class)
			.add("value", ingredientHelper.getUniqueId(value))
			.add("area", area)
			.add("allowsCheating", allowsCheating)
			.toString();
	}
}
