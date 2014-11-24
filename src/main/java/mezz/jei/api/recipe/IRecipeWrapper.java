package mezz.jei.api.recipe;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public interface IRecipeWrapper {

	List getInputs();
	List<ItemStack> getOutputs();

	/* Draw additional info about the recipe. */
	void drawInfo(@Nonnull Minecraft minecraft, int mouseX, int mouseY);
}
