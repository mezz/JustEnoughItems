package mezz.jei.plugins.forestry.centrifuge;

import forestry.factory.gadgets.MachineCentrifuge;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CentrifugeRecipeWrapper implements IRecipeWrapper {

	@Nonnull
	private final MachineCentrifuge.Recipe recipe;
	@Nonnull
	private final List<ItemStack> outputs;
	@Nonnull
	private final List<ItemStack> inputs;
	@Nonnull
	private final String timePerItem;

	public CentrifugeRecipeWrapper(Object recipe) {
		this.recipe = (MachineCentrifuge.Recipe)recipe;
		this.outputs = new ArrayList<ItemStack>(this.recipe.products.keySet());
		this.inputs = Collections.singletonList(this.recipe.resource);
		this.timePerItem = ((Integer)this.recipe.timePerItem).toString();
	}

	@Nonnull
	@Override
	public List<ItemStack> getInputs() {
		return inputs;
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs() {
		return outputs;
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		minecraft.fontRenderer.drawString(timePerItem, 20, 45, Color.gray.getRGB());
	}

	public Integer getChance(ItemStack itemStack) {
		return recipe.products.get(itemStack);
	}
}
