package mezz.jei.api.gui.ingredient;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeLayoutSlotBuilder;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;

import java.util.List;

/**
 * IGuiFluidStackGroup displays one or more {@link FluidStack} in a gui.
 *
 * If multiple FluidStacks are set, they will be displayed in rotation.
 *
 * Get an instance from {@link IRecipeLayout#getFluidStacks()}.
 *
 * @deprecated since JEI 9.3.0.
 * Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, List)}
 */
@Deprecated
public interface IGuiFluidStackGroup extends IGuiIngredientGroup<FluidStack> {
	/**
	 * Initialize the fluid at slotIndex.
	 *
	 * @param slotIndex    the slot index of this fluid
	 * @param input        whether this slot is an input
	 * @param xPosition    x position relative to the recipe background
	 * @param yPosition    y position relative to the recipe background
	 * @param width        width of this fluid
	 * @param height       height of this fluid
	 * @param capacityMb   maximum amount of fluid that this "tank" can hold in milli-buckets
	 * @param showCapacity show the capacity in the tooltip
	 * @param overlay      optional overlay to display over the tank.
	 *                     Typically the overlay is fluid level lines, but it could also be a mask to shape the tank.
	 *
	 * @deprecated @since JEI 9.3.0. Use {@link IRecipeLayoutSlotBuilder#setFluidRenderer(int, boolean, IDrawable)}
	 */
	@Deprecated
	void init(int slotIndex, boolean input, int xPosition, int yPosition, int width, int height, int capacityMb, boolean showCapacity, @Nullable IDrawable overlay);

	@Override
	void set(int slotIndex, @Nullable FluidStack fluidStack);
}
