package mezz.jei.input;

import javax.annotation.Nullable;

import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import com.google.common.base.MoreObjects;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClickedIngredient<V> implements IClickedIngredient<V> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final V value;
	@Nullable
	private final Rect2i area;
	private boolean canSetFocusWithMouse;
	private boolean allowsCheating;

	@Nullable
	public static <V> ClickedIngredient<V> create(V value, @Nullable Rect2i area) {
		ErrorUtil.checkNotNull(value, "value");
		IngredientManager ingredientManager = Internal.getIngredientManager();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value);
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

	private ClickedIngredient(V value, @Nullable Rect2i area) {
		this.value = value;
		this.area = area;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Nullable
	@Override
	public Rect2i getArea() {
		return area;
	}

	public void setAllowsCheating() {
		this.allowsCheating = true;
	}

	public void setCanSetFocusWithMouse() {
		this.canSetFocusWithMouse = true;
	}

	@Override
	public ItemStack getCheatItemStack() {
		if (allowsCheating) {
			IIngredientManager ingredientManager = Internal.getIngredientManager();
			IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value);
			return ingredientHelper.getCheatItemStack(value);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return this.canSetFocusWithMouse;
	}

	@Override
	public String toString() {
		IIngredientManager ingredientManager = Internal.getIngredientManager();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value);
		return MoreObjects.toStringHelper(ClickedIngredient.class)
			.add("value", ingredientHelper.getUniqueId(value, UidContext.Ingredient))
			.add("area", area)
			.add("allowsCheating", allowsCheating)
			.add("canSetFocusWithMouse", canSetFocusWithMouse)
			.toString();
	}
}
