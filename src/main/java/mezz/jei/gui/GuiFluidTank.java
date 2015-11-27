package mezz.jei.gui;

import javax.annotation.Nonnull;
import java.util.Collection;

import net.minecraft.client.Minecraft;

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
	public void draw(@Nonnull Minecraft minecraft) {
		//TODO
	}

	@Override
	public void drawHovered(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		//TODO
	}
}
