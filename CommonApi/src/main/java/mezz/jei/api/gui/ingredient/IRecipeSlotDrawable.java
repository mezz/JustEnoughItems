package mezz.jei.api.gui.ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.runtime.util.IImmutableRect2i;


/**
 * Represents one drawn ingredient that is part of a recipe.
 * One recipe slot can contain multiple ingredients, displayed one after the other over time.
 *
 * These ingredients may be different types, for example {@link VanillaTypes#ITEM_STACK} and another type
 * can be displayed in one recipe slot in rotation.
 *
 * Useful for implementing {@link IRecipeTransferHandler} and some other advanced cases.
 *
 * @since 9.3.0
 */
public interface IRecipeSlotDrawable extends IRecipeSlotView {
	void addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback);

	IImmutableRect2i getRect();

	boolean isMouseOver(double recipeMouseX, double recipeMouseY);

	void draw(PoseStack poseStack);

	void drawOverlays(PoseStack poseStack, int posX, int posY, int recipeMouseX, int recipeMouseY, IModIdHelper modIdHelper);
}
