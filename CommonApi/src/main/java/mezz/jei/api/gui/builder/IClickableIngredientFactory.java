package mezz.jei.api.gui.builder;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Helper factory for creating {@link IClickableIngredient}.
 *
 * Passed to mods in methods that need to create clickable ingredients, like
 * {@link IGuiContainerHandler#getClickableIngredientUnderMouse(IClickableIngredientFactory, AbstractContainerScreen, double, double)}
 *
 * An instance is also available from {@link IIngredientManager#getClickableIngredientFactory()}.
 *
 * @since 21.2.0
 */
public interface IClickableIngredientFactory {
	/**
	 * Create a clickable ingredient builder with the given ItemStack.
	 *
	 * @since 21.2.0
	 */
	default IBuilder<ItemStack> createBuilder(ItemStack itemStack) {
		return createBuilder(VanillaTypes.ITEM_STACK, itemStack);
	}

	/**
	 * Create a clickable ingredient builder with the given typed ingredient.
	 *
	 * @since 21.2.0
	 */
	<T> IBuilder<T> createBuilder(ITypedIngredient<T> value);

	/**
	 * Create a clickable ingredient builder with the given typed ingredient.
	 *
	 * @since 21.2.0
	 */
	<T> IBuilder<T> createBuilder(IIngredientType<T> ingredientType, T ingredient);

	/**
	 * An intermediate builder for clickable ingredients.
	 * It has an ingredient and needs an area in order to build the clickable ingredient.
	 *
	 * @since 21.2.0
	 */
	interface IBuilder<T> {
		/**
		 * Create a clickable ingredient with the given area
		 *
		 * @since 21.2.0
		 */
		Optional<IClickableIngredient<T>> buildWithArea(int x, int y, int width, int height);

		/**
		 * Create a clickable ingredient with the given area
		 *
		 * @since 21.2.0
		 */
		Optional<IClickableIngredient<T>> buildWithArea(Rect2i area);
	}
}
