package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.recipe.IFocus;
import net.minecraftforge.fluids.FluidStack;

public class GuiFluidStackGroup extends GuiIngredientGroup<FluidStack, GuiIngredient<FluidStack>> implements IGuiFluidStackGroup {
	private static final FluidStackHelper helper = new FluidStackHelper();

	public GuiFluidStackGroup(@Nonnull IFocus<FluidStack> focus) {
		super(focus);
	}

	@Override
	public void init(int slotIndex, boolean input, int xPosition, int yPosition, int width, int height, int capacityMb, boolean showCapacity, @Nullable IDrawable overlay) {
		FluidStackRenderer renderer = new FluidStackRenderer(capacityMb, showCapacity, width, height, overlay);
		GuiIngredient<FluidStack> guiIngredient = new GuiIngredient<FluidStack>(renderer, helper, slotIndex, input, xPosition, yPosition, width, height, 0, itemCycleOffset);
		guiIngredients.put(slotIndex, guiIngredient);
	}
}
