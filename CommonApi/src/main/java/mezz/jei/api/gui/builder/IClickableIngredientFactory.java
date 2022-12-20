package mezz.jei.api.gui.builder;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Helper factory for creating {@link IClickableIngredient}.
 *
 * Passed to mods in methods that need to create clickable ingredients, like
 * {@link IGuiContainerHandler#getClickableIngredientUnderMouse(IClickableIngredientFactory, AbstractContainerScreen, double, double)}.
 *
 * @since 10.3.0
 */
public interface IClickableIngredientFactory {
	/**
	 * Create a clickable ingredient builder with the given ItemStack.
	 *
	 * @since 10.3.0
	 */
	default IBuilder<ItemStack> createBuilder(ItemStack itemStack) {
		return createBuilder(VanillaTypes.ITEM_STACK, itemStack);
	}

	/**
	 * Create a clickable ingredient builder with the given typed ingredient.
	 *
	 * @since 10.3.0
	 */
	<T> IBuilder<T> createBuilder(ITypedIngredient<T> value);

	/**
	 * Create a clickable ingredient builder with the given legacy ingredient.
	 *
	 * @since 10.3.0
	 */
	IBuilder<?> createBuilder(@Nullable Object ingredient);

	/**
	 * Create a clickable ingredient builder with the given ingredient.
	 *
	 * @since 10.3.0
	 */
	<T> IBuilder<T> createBuilder(IIngredientType<T> ingredientType, T ingredient);

	/**
	 * An intermediate builder for clickable ingredients.
	 * It has an ingredient and needs an area in order to build the clickable ingredient.
	 *
	 * @since 10.3.0
	 */
	interface IBuilder<T> {
		/**
		 * Create a clickable ingredient with the given area.
		 *
		 * @since 10.3.0
		 */
		Optional<IClickableIngredient<T>> buildWithArea(int x, int y, int width, int height);

		/**
		 * Create a clickable ingredient with the given area.
		 *
		 * @since 10.3.0
		 */
		Optional<IClickableIngredient<T>> buildWithArea(Rect2i area);
	}
}
