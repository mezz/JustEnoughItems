package mezz.jei.plugins.vanilla.furnace.smelting;

import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Collections;
import java.util.List;

public class SmeltingRecipe implements IRecipeWrapper {
	@Nonnull
	private final List<ItemStack> input;
	@Nonnull
	private final ItemStack output;

	@Nullable
	private final String experienceString;

	public SmeltingRecipe(@Nonnull List<ItemStack> input, @Nonnull ItemStack output, float experience) {
		this.input = input;
		this.output = output;

		if (experience > 0.0) {
			experienceString = StatCollector.translateToLocalFormatted("gui.jei.furnaceExperience", experience);
		} else {
			experienceString = null;
		}
	}

	@Nonnull
	public List<ItemStack> getInputs() {
		return input;
	}

	@Nonnull
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(output);
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft) {
		if (experienceString != null) {
			FontRenderer fontRendererObj = minecraft.fontRenderer;
			fontRendererObj.drawString(experienceString, 69 - fontRendererObj.getStringWidth(experienceString) / 2, 0, Color.gray.getRGB());
		}
	}

}
