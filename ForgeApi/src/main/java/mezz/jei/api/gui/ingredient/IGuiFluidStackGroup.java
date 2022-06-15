package mezz.jei.api.gui.ingredient;

import mezz.jei.api.recipe.IFocusGroup;
import org.jetbrains.annotations.Nullable;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.gui.drawable.IDrawable;

/**
 * IGuiFluidStackGroup displays one or more {@link FluidStack} in a gui.
 *
 * If multiple FluidStacks are set, they will be displayed in rotation.
 *
 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}
 */
@SuppressWarnings("removal")
@Deprecated(forRemoval = true, since = "9.3.0")
public interface IGuiFluidStackGroup extends IGuiIngredientGroup<FluidStack> {
	/**
	 * Initialize the fluid at slotIndex.
	 *
	 * @param ingredientIndex the ingredient index of this fluid
	 * @param input           whether this slot is an input
	 * @param xPosition       x position relative to the recipe background
	 * @param yPosition       y position relative to the recipe background
	 * @param width           width of this fluid
	 * @param height          height of this fluid
	 * @param capacityMb      maximum amount of fluid that this "tank" can hold in milli-buckets
	 * @param showCapacity    show the capacity in the tooltip
	 * @param overlay         optional overlay to display over the tank.
	 *                        Typically the overlay is fluid level lines, but it could also be a mask to shape the tank.
	 *
	 * @deprecated Use {@link IRecipeSlotBuilder#setFluidRenderer(int, boolean, int, int)} instead.
	 * To add an overlay, use {@link IRecipeSlotBuilder#setOverlay(IDrawable, int, int)}.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	void init(int ingredientIndex, boolean input, int xPosition, int yPosition, int width, int height, int capacityMb, boolean showCapacity, @Nullable IDrawable overlay);

	@Override
	@SuppressWarnings("removal")
	@Deprecated(forRemoval = true, since = "9.3.0")
	void set(int ingredientIndex, @Nullable FluidStack fluidStack);

	@Override
	@SuppressWarnings("removal")
	@Deprecated(forRemoval = true, since = "9.3.0")
	void addTooltipCallback(ITooltipCallback<FluidStack> tooltipCallback);
}
