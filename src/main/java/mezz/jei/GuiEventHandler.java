package mezz.jei;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mezz.jei.gui.GuiContainerOverlay;
import mezz.jei.util.Reflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraftforge.client.event.GuiScreenEvent;

public class GuiEventHandler {

	private GuiContainerOverlay overlay = new GuiContainerOverlay();

	@SubscribeEvent
	public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
		if (!shouldOverlay(event.gui))
			return;
		GuiContainer guiContainer = (GuiContainer) event.gui;
		Integer[] dimensions = Reflection.getDimensions(guiContainer);
		overlay.initGui(dimensions[0], dimensions[1], dimensions[2], dimensions[3], guiContainer.width, guiContainer.height, event.buttonList);
	}

	@SubscribeEvent
	public void onDrawScreenEvent(GuiScreenEvent.DrawScreenEvent event) {
		if (!shouldOverlay(event.gui))
			return;
		Minecraft minecraft = event.gui.mc;
		overlay.drawScreen(minecraft.fontRenderer);
	}

	@SubscribeEvent
	public void onActionPerformedEvent(GuiScreenEvent.ActionPerformedEvent.Pre event) {
		if (!shouldOverlay(event.gui))
			return;
		if (overlay.actionPerformed(event.button))
			event.setCanceled(true);
	}

	private boolean shouldOverlay(GuiScreen gui) {
		return (gui instanceof GuiContainer) && !(gui instanceof GuiContainerCreative);
	}
}
