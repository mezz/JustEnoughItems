package mezz.jei.plugins.forestry.fabricator;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import forestry.factory.gadgets.MachineFabricator;

import mezz.jei.api.recipe.IRecipeWrapper;

public class FabricatorSmeltingRecipeWrapper implements IRecipeWrapper {

	private final MachineFabricator.Smelting smelting;

	public FabricatorSmeltingRecipeWrapper(Object recipe) {
		smelting = (MachineFabricator.Smelting)recipe;
	}

	@Override
	public List<ItemStack> getInputs() {
		return Collections.singletonList(smelting.getResource());
	}

	@Override
	public List getOutputs() {
		return Collections.emptyList();
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft) {

	}
}
