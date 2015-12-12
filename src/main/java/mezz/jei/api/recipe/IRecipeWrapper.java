package mezz.jei.api.recipe;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.client.Minecraft;

import net.minecraftforge.fluids.FluidStack;

/**
 * A wrapper around a normal recipe with methods that allow JEI can make sense of it.
 * Implementers will have to create a wrapper for each type of recipe they have.
 */
public interface IRecipeWrapper {

	/**
	 * Return a list of recipe inputs.
	 * Each element can be an ItemStack, null, or a List of ItemStacks.
	 */
	List getInputs();

	/**
	 * Return a list of recipe inputs.
	 * Each element can be an ItemStack, null, or a List of ItemStacks.
	 */
	List getOutputs();

	/** Return a list of recipe fluid inputs. */
	List<FluidStack> getFluidInputs();

	/** Return a list of recipe fluid outputs. */
	List<FluidStack> getFluidOutputs();

	/** Draw additional info about the recipe. */
	void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight);

	/** Draw animations involving the recipe. Can be disabled in the config. */
	void drawAnimations(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight);
}
