package mezz.jei.gui;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import net.minecraftforge.fluids.FluidStack;

public class GuiFluidTank extends GuiWidget<FluidStack> {
	private final int capacityMb;

	public GuiFluidTank(int xPosition, int yPosition, int width, int height, int capacityMb) {
		super(xPosition, yPosition, width, height);
		this.capacityMb = capacityMb;
	}

	@Override
	protected Collection<FluidStack> expandSubtypes(Collection<FluidStack> contained) {
		return contained;
	}

	@Override
	protected FluidStack getMatch(Iterable<FluidStack> contained, @Nonnull Focus toMatch) {
		if (toMatch.getFluid() == null) {
			return null;
		}
		for (FluidStack fluidStack : contained) {
			if (toMatch.getFluid() == fluidStack.getFluid()) {
				return fluidStack;
			}
		}
		return null;
	}

	@Override
	protected void draw(@Nonnull Minecraft minecraft, int xPosition, int yPosition, @Nonnull FluidStack contents) {
		// TODO
	}

	@Override
	protected List getTooltip(@Nonnull Minecraft minecraft, @Nonnull FluidStack value) {
		// TODO
		return null;
	}

	@Override
	protected FontRenderer getFontRenderer(@Nonnull Minecraft minecraft, @Nonnull FluidStack value) {
		return minecraft.fontRendererObj;
	}
}
