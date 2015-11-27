package mezz.jei.gui;

import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.gui.IGuiFluidTanks;

public class GuiFluidTanks extends GuiWidgets<FluidStack, GuiFluidTank> implements IGuiFluidTanks {
	@Override
	public void init(int index, int xPosition, int yPosition, int width, int height, int capacityMb) {
		GuiFluidTank guiFluidTank = new GuiFluidTank(xPosition, yPosition, width, height, capacityMb);
		guiWidgets.put(index, guiFluidTank);
	}
}
