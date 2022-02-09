package mezz.jei.input;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.gui.ingredients.GuiIngredient;
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

import java.util.Optional;

public class ClickedIngredient<V> implements IClickedIngredient<V> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final V value;
	@Nullable
	private final Rect2i area;
	private final boolean canSetFocusWithMouse;
	private final boolean allowsCheating;

	public static <V> Optional<IClickedIngredient<? extends V>> create(GuiIngredient<V> guiIngredient, boolean allowsCheating, boolean canSetFocusWithMouse) {
		V displayedIngredient = guiIngredient.getDisplayedIngredient();
		if (displayedIngredient == null) {
			return Optional.empty();
		}
		return create(displayedIngredient, guiIngredient.getRect(), allowsCheating, canSetFocusWithMouse);
	}

	public static <V> Optional<IClickedIngredient<? extends V>> create(V value, @Nullable Rect2i area, boolean allowsCheating, boolean canSetFocusWithMouse) {
		ErrorUtil.checkNotNull(value, "value");
		IngredientManager ingredientManager = Internal.getIngredientManager();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value);
		try {
			if (ingredientHelper.isValidIngredient(value)) {
				ClickedIngredient<V> clickedIngredient = new ClickedIngredient<>(value, area, allowsCheating, canSetFocusWithMouse);
				return Optional.of(clickedIngredient);
			}
			String ingredientInfo = ingredientHelper.getErrorInfo(value);
			LOGGER.error("Clicked invalid ingredient. Ingredient Info: {}", ingredientInfo);
		} catch (RuntimeException e) {
			String ingredientInfo = ingredientHelper.getErrorInfo(value);
			LOGGER.error("Clicked invalid ingredient. Ingredient Info: {}", ingredientInfo, e);
		}
		return Optional.empty();
	}

	private ClickedIngredient(V value, @Nullable Rect2i area, boolean allowsCheating, boolean canSetFocusWithMouse) {
		this.value = value;
		this.area = area;
		this.allowsCheating = allowsCheating;
		this.canSetFocusWithMouse = canSetFocusWithMouse;
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
