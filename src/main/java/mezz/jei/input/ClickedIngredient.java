package mezz.jei.input;

import com.google.common.base.MoreObjects;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.util.ErrorUtil;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class ClickedIngredient<V> implements IClickedIngredient<V> {
	private final ITypedIngredient<V> value;
	@Nullable
	private final Rect2i area;
	private final boolean canSetFocusWithMouse;
	private final boolean allowsCheating;

	public ClickedIngredient(ITypedIngredient<V> value, @Nullable Rect2i area, boolean allowsCheating, boolean canSetFocusWithMouse) {
		ErrorUtil.checkNotNull(value, "value");
		this.value = value;
		this.area = area;
		this.allowsCheating = allowsCheating;
		this.canSetFocusWithMouse = canSetFocusWithMouse;
	}

	@Override
	public ITypedIngredient<V> getValue() {
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
			IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
			return ingredientHelper.getCheatItemStack(value.getIngredient());
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
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		return MoreObjects.toStringHelper(ClickedIngredient.class)
			.add("value", ingredientHelper.getUniqueId(value.getIngredient(), UidContext.Ingredient))
			.add("area", area)
			.add("allowsCheating", allowsCheating)
			.add("canSetFocusWithMouse", canSetFocusWithMouse)
			.toString();
	}
}
