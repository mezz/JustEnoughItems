package mezz.jei.input;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.util.ImmutableRect2i;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

public interface IClickedIngredient<V> {

	ITypedIngredient<V> getValue();

	@Nullable
	ImmutableRect2i getArea();

	ItemStack getCheatItemStack();

	/**
	 * Some GUIs (like vanilla) shouldn't allow JEI to click to set the focus,
	 * it would conflict with their normal behavior.
	 */
	boolean canOverrideVanillaClickHandler();
}
