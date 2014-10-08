package mezz.jei;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import mezz.jei.gui.GuiContainerOverlay;
import mezz.jei.util.Reflection;
import mezz.jei.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;

public class GuiEventHandler {

	private static boolean mouseOverSlot(GuiContainerOverlay guiContainerOverlay, Slot slot, int mouseX, int mouseY) {
		return mouseInMounds(guiContainerOverlay, slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY);
	}

	private static boolean mouseInMounds(GuiContainerOverlay guiContainerOverlay, int x, int y, int w, int h, int mouseX, int mouseY) {
		int k1 = guiContainerOverlay.guiLeft;
		int l1 = guiContainerOverlay.guiTop;
		mouseX -= k1;
		mouseY -= l1;
		return mouseX >= x - 1 && mouseX < x + w + 1 && mouseY >= y - 1 && mouseY < y + h + 1;
	}

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
	public void onDrawScreenEvent(GuiScreenEvent.DrawScreenEvent.Post event) {
		if (!shouldOverlay(event.gui))
			return;

		overlay.drawScreen(event.gui.mc, event.mouseX, event.mouseY);

		// Holy hackery Batman. There isn't a good way to change the zLevel vanilla tooltips render at, so we'll render them ourselves
		// This does mean the tooltip gets rendered twice, but it's a light enough draw call that it shouldn't cause issues
		GuiContainer guiContainer = (GuiContainer) event.gui;
		for (int i1 = 0; i1 < guiContainer.inventorySlots.inventorySlots.size(); ++i1) {
			Slot slot = (Slot) guiContainer.inventorySlots.inventorySlots.get(i1);
			if (mouseOverSlot(overlay, slot, event.mouseX, event.mouseY) && slot.func_111238_b()) {
				Render.renderToolTip(overlay, slot.getStack(), event.mouseX, event.mouseY);
			}
		}
	}

	@SubscribeEvent
	public void onActionPerformedEvent(GuiScreenEvent.ActionPerformedEvent.Pre event) {
		if (!shouldOverlay(event.gui))
			return;

		if (overlay.actionPerformed(event.button))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END)
			return;

		Minecraft mc = Minecraft.getMinecraft();
		if (mc.currentScreen == null || !shouldOverlay(mc.currentScreen))
			return;

		overlay.handleInput();
	}

	private boolean shouldOverlay(GuiScreen gui) {
		return (gui instanceof GuiContainer) && !(gui instanceof GuiContainerCreative);
	}
}
