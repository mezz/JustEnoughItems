package mezz.jei.wrappers;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.gui.inventory.GuiInventory;

public class GuiInventoryWrapper extends GuiInventory {

	public GuiInventoryWrapper(GuiInventory gui) {
		super(FMLClientHandler.instance().getClientPlayerEntity());
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_)
	{
//		this.fontRendererObj.drawString(I18n.format("container.crafting", new Object[0]), 86, 16, 4210752);
		this.fontRendererObj.drawString("Test", 86, 16, 4210752);
	}

}
