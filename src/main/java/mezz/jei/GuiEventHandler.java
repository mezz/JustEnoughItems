package mezz.jei;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mezz.jei.gui.wrappers.GuiChestWrapper;
import mezz.jei.gui.wrappers.GuiCraftingWrapper;
import mezz.jei.gui.wrappers.GuiInventoryWrapper;
import mezz.jei.util.Log;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.client.event.GuiOpenEvent;

public class GuiEventHandler {
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		GuiScreen gui = event.gui;
		if (gui == null)
			return;

		if (gui instanceof GuiContainer) {
			Log.debug("Container Gui Opened: " + gui.getClass().getName());
			if (gui instanceof GuiInventory)
				event.gui = new GuiInventoryWrapper((GuiInventory)gui);
			if (gui instanceof GuiChest)
				event.gui = new GuiChestWrapper((GuiChest)gui);
			if (gui instanceof GuiCrafting)
				event.gui = new GuiCraftingWrapper((GuiCrafting)gui);
		}
	}
}
