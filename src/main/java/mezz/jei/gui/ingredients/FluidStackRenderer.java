package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import net.minecraftforge.fluids.FluidStack;

public class FluidStackRenderer implements IIngredientRenderer<FluidStack> {
	private final int capacityMb;

	public FluidStackRenderer(int capacityMb) {
		this.capacityMb = capacityMb;
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft, int xPosition, int yPosition, @Nonnull FluidStack value) {
		// TODO
	}

	@Override
	public List<String> getTooltip(@Nonnull Minecraft minecraft, @Nonnull FluidStack value) {
		// TODO
		return null;
	}

	@Override
	public FontRenderer getFontRenderer(@Nonnull Minecraft minecraft, @Nonnull FluidStack value) {
		return minecraft.fontRendererObj;
	}
}
