package mezz.jei;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mezz.jei.util.Log;
import mezz.jei.wrappers.GuiInventoryWrapper;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.client.event.GuiOpenEvent;

public class GuiEventHandler {
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		if (event.gui == null) {
			//Log.info("Gui Closed");
			return;
		}

		if (event.gui instanceof GuiContainer) {
			Log.debug("Container Gui Opened: " + event.gui.getClass().getName());
			if (event.gui instanceof GuiInventory)
				event.gui = new GuiInventoryWrapper((GuiInventory)event.gui);

		}
	}
}
