package mezz.jei.api.recipe;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.client.Minecraft;

public interface IRecipeWrapper {

	List getInputs();

	List getOutputs();

	/* Draw additional info about the recipe. */
	void drawInfo(@Nonnull Minecraft minecraft);
}
