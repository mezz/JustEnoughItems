package mezz.jei.input;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

public interface IClickedIngredient<V> {

	ITypedIngredient<V> getValue();

	@Nullable
	Rect2i getArea();

	ItemStack getCheatItemStack();

	/**
	 * Some GUIs (like vanilla) shouldn't allow JEI to click to set the focus,
	 * it would conflict with their normal behavior.
	 */
	boolean canSetFocusWithMouse();
}
