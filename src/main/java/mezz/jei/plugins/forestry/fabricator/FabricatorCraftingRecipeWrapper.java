package mezz.jei.plugins.forestry.fabricator;

import forestry.factory.gadgets.MachineFabricator;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.plugins.forestry.crafting.ForestryShapedRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FabricatorCraftingRecipeWrapper implements IRecipeWrapper {

	@Nullable private final ItemStack plan;
	@Nonnull private final ForestryShapedRecipeWrapper internal;

	public FabricatorCraftingRecipeWrapper(Object recipe) {
		MachineFabricator.Recipe fabricatorRecipe = (MachineFabricator.Recipe)recipe;
		this.plan = fabricatorRecipe.getPlan();
		this.internal = new ForestryShapedRecipeWrapper(fabricatorRecipe.asIRecipe());
	}

	@Override
	public List<Object> getInputs() {
		List<Object> inputs = new ArrayList<Object>(internal.getInputs());
		if (plan != null) {
			inputs.add(plan);
		}
		return inputs;
	}

	@Override
	public List<ItemStack> getOutputs() {
		return internal.getOutputs();
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft) {

	}

	@Nullable
	public ItemStack getPlan() {
		return plan;
	}

	@Nonnull
	public ForestryShapedRecipeWrapper getInternal() {
		return internal;
	}
}
