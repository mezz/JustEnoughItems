package mezz.jei;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.GuiOpenEvent;

public class GuiEventHandler {

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		if (event.gui == null)
			return;

		event.gui = JustEnoughItems.wrapperManager.wrapGui(event.gui);
	}

}
