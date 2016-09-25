package mezz.jei.api.recipe;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;

/**
 * An {@link IRecipeWrapper} that does nothing, inherit from this to avoid implementing methods you don't need.
 */
public abstract class BlankRecipeWrapper implements IRecipeWrapper {
	@Override
	@Deprecated
	public List getInputs() {
		return Collections.emptyList();
	}

	@Override
	@Deprecated
	public List getOutputs() {
		return Collections.emptyList();
	}

	@Override
	@Deprecated
	public List<FluidStack> getFluidInputs() {
		return Collections.emptyList();
	}

	@Override
	@Deprecated
	public List<FluidStack> getFluidOutputs() {
		return Collections.emptyList();
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {

	}

	@Override
	public void drawAnimations(Minecraft minecraft, int recipeWidth, int recipeHeight) {

	}

	@Nullable
	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY) {
		return null;
	}

	@Override
	public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
		return false;
	}
}
