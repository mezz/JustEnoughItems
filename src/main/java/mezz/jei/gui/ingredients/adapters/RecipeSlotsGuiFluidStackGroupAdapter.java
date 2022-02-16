package mezz.jei.gui.ingredients.adapters;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.ingredients.RecipeSlots;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"removal", "deprecation"})
public class RecipeSlotsGuiFluidStackGroupAdapter extends RecipeSlotsGuiIngredientGroupAdapter<FluidStack> implements IGuiFluidStackGroup {
	public RecipeSlotsGuiFluidStackGroupAdapter(RecipeSlots recipeSlots, IIngredientManager ingredientManager, int cycleOffset) {
		super(recipeSlots, ingredientManager, VanillaTypes.FLUID, cycleOffset);
	}

	@Override
	public void init(int slotIndex, boolean input, int xPosition, int yPosition, int width, int height, int capacityMb, boolean showCapacity, @Nullable IDrawable overlay) {
		FluidStackRenderer renderer = new FluidStackRenderer(capacityMb, showCapacity, width, height, overlay);
		init(slotIndex, input, renderer, xPosition, yPosition, width, height, 0, 0);
	}
}
