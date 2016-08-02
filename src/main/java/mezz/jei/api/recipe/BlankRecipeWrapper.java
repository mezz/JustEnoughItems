package mezz.jei.api.recipe;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * An {@link IRecipeWrapper} that does nothing, inherit from this to avoid implementing methods you don't need.
 */
public abstract class BlankRecipeWrapper implements IRecipeWrapper {
	@Override
	@Nonnull
	public List getInputs() {
		return Collections.emptyList();
	}

	@Override
	@Nonnull
	public List getOutputs() {
		return Collections.emptyList();
	}

	@Override
	@Nonnull
	public List<FluidStack> getFluidInputs() {
		return Collections.emptyList();
	}

	@Override
	@Nonnull
	public List<FluidStack> getFluidOutputs() {
		return Collections.emptyList();
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {

	}

	@Override
	public void drawAnimations(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight) {

	}

	@Nullable
	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY) {
		return null;
	}

	@Override
	public boolean handleClick(@Nonnull Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
		return false;
	}
}
