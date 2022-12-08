package mezz.jei.api.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.util.IImmutableRect2i;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * For addons that want to draw recipe layouts somewhere themselves.
 *
 * Create an instance with {@link IRecipeManager#createRecipeLayoutDrawable(IRecipeCategory, Object, IFocusGroup)}.
 */
public interface IRecipeLayoutDrawable<R> {
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
	 * @since 11.1.1
	 */
	default Optional<ItemStack> getItemStackUnderMouse(int mouseX, int mouseY) {
		return getIngredientUnderMouse(mouseX, mouseY, VanillaTypes.ITEM_STACK);
	}

	/**
	 * Returns the ingredient currently under the mouse, if there is one.
	 * Can be an ItemStack, FluidStack, or any other ingredient type registered with JEI.
	 */
	<T> Optional<T> getIngredientUnderMouse(int mouseX, int mouseY, IIngredientType<T> ingredientType);

	boolean handleInput(double mouseX, double mouseY, InputConstants.Key key);

    Optional<IRecipeSlotDrawable> getRecipeSlotUnderMouse(double mouseX, double mouseY);

	int getPosX();

	int getPosY();

	IImmutableRect2i getRecipeTransferButtonArea();

	IRecipeSlotsView getRecipeSlotsView();

	IRecipeCategory<R> getRecipeCategory();

	R getRecipe();
}
