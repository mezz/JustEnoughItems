package mezz.jei.api.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * An extension of {@link IRecipeLayout} for addons that want to draw the layouts themselves somewhere.
 *
 * Create an instance with {@link IRecipeManager#createRecipeLayoutDrawable(IRecipeCategory, Object, IFocus)}
 * or {@link IRecipeManager#createRecipeLayoutDrawable(IRecipeCategory, Object, IFocusGroup)}.
 */
@ApiStatus.NonExtendable
@SuppressWarnings("removal")
public interface IRecipeLayoutDrawable extends IRecipeLayout {
	/**
	 * Set the position of the recipe layout in screen coordinates.
	 * To help decide on the position, you can get the width and height of this recipe from {@link IRecipeCategory#getBackground()}.
	 */
	void setPosition(int posX, int posY);

	/**
	 * Draw the recipe without overlays such as item tool tips.
	 */
	void drawRecipe(PoseStack stack, int mouseX, int mouseY);

	/**
	 * Draw the recipe overlays such as item tool tips.
	 */
	void drawOverlays(PoseStack stack, int mouseX, int mouseY);

	/**
	 * Returns true if the mouse is hovering over the recipe.
	 * Hovered recipes should be drawn after other recipes to have the drawn tooltips overlap other elements properly.
	 */
	boolean isMouseOver(double mouseX, double mouseY);

	/**
	 * Returns the ItemStack currently under the mouse, if there is one.
	 *
	 * @see #getIngredientUnderMouse(int, int, IIngredientType) to get other types of ingredient.
	 * @since 10.1.3
	 */
	@Nullable
	default ItemStack getItemStackUnderMouse(int mouseX, int mouseY) {
		return getIngredientUnderMouse(mouseX, mouseY, VanillaTypes.ITEM_STACK);
	}

	/**
	 * Returns the ingredient currently under the mouse, if there is one.
	 * Can be an ItemStack, FluidStack, or any other ingredient type registered with JEI.
	 */
	@Nullable
	<T> T getIngredientUnderMouse(int mouseX, int mouseY, IIngredientType<T> ingredientType);

	/**
	 * Returns the ingredient currently under the mouse, if there is one.
	 *
	 * @since 10.6.1
	 */
	default <T> Optional<T> getOptionalIngredientUnderMouse(int mouseX, int mouseY, IIngredientType<T> ingredientType) {
		return Optional.ofNullable(getIngredientUnderMouse(mouseX, mouseY, ingredientType));
	}

	/**
	 * Get the recipe slot currently under the mouse, if there is one.
	 * @since 10.3.0
	 */
	Optional<IRecipeSlotDrawable> getRecipeSlotUnderMouse(double mouseX, double mouseY);

	/**
	 * Get position and size for the recipe in absolute screen coordinates.
	 * @since 10.3.0
	 */
	Rect2i getRect();

	/**
	 * Get position and size for the recipe, including the border drawn around it, in absolute screen coordinates.
	 * @since 10.5.0
	 */
	default Rect2i getRectWithBorder() {
		return getRect();
	}

	/**
	 * Get the position of the recipe transfer button area, relative to the recipe layout drawable.
	 * @since 10.3.0
	 */
	Rect2i getRecipeTransferButtonArea();

	/**
	 * Get a view of the recipe slots for this recipe layout.
	 * @since 10.3.0
	 */
	IRecipeSlotsView getRecipeSlotsView();

	/**
	 * Get the recipe slot currently under the mouse, if there is one.
	 *
	 * @return the slot under the mouse, with an offset
	 *
	 * @since 10.5.0
	 */
	default Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
		return getRecipeSlotUnderMouse(mouseX, mouseY)
			.map(slot -> new RecipeSlotUnderMouse(slot, 0, 0));
	}

	/**
	 * Get the recipe category that this recipe layout is a part of.
	 * @since 10.3.0
	 */
	IRecipeCategory<?> getRecipeCategory();

	/**
	 * Get the recipe that this recipe layout displays.
	 * @since 10.3.0
	 */
	Object getRecipe();

	/**
	 * Get the input handler for this recipe layout.
	 *
	 * @since 10.5.0
	 */
	IJeiInputHandler getInputHandler();

	/**
	 * Update the recipe layout on game tick.
	 *
	 * @since 10.6.1
	 */
	void tick();
}
