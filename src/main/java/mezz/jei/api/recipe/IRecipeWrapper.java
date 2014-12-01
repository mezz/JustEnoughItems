package mezz.jei.api.recipe;

import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.util.List;

public interface IRecipeWrapper {

	List getInputs();
	List getOutputs();

	/* Draw additional info about the recipe. */
	void drawInfo(@Nonnull Minecraft minecraft);
}
