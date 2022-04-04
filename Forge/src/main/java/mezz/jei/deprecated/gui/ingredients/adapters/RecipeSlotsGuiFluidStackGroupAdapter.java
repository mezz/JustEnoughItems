package mezz.jei.deprecated.gui.ingredients.adapters;

import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.gui.ingredients.RecipeSlots;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.forge.plugins.forge.ingredients.fluid.FluidStackRenderer;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"removal", "deprecation"})
public class RecipeSlotsGuiFluidStackGroupAdapter extends RecipeSlotsGuiIngredientGroupAdapter<FluidStack> implements IGuiFluidStackGroup {
	public RecipeSlotsGuiFluidStackGroupAdapter(RecipeSlots recipeSlots, RegisteredIngredients registeredIngredients, int cycleOffset) {
		super(recipeSlots, registeredIngredients, ForgeTypes.FLUID_STACK, cycleOffset);
	}

	@Override
	public void init(int slotIndex, boolean input, int xPosition, int yPosition, int width, int height, int capacityMb, boolean showCapacity, @Nullable IDrawable overlay) {
		FluidStackRenderer renderer = new FluidStackRenderer(capacityMb, showCapacity, width, height, overlay);
		init(slotIndex, input, renderer, xPosition, yPosition, width, height, 0, 0);
	}
}
