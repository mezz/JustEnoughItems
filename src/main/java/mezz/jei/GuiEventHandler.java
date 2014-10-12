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
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;

public class GuiEventHandler {

	private GuiContainerOverlay overlay = new GuiContainerOverlay();

	@SubscribeEvent
	public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
		GuiContainer guiContainer = asGuiContainer(event.gui);
		if (guiContainer == null)
			return;
		Integer[] dimensions = Reflection.getDimensions(guiContainer);
		overlay.initGui(dimensions[0], dimensions[1], dimensions[2], dimensions[3], guiContainer.width, guiContainer.height, event.buttonList);
	}

	@SubscribeEvent
	public void onDrawScreenEvent(GuiScreenEvent.DrawScreenEvent.Post event) {
		GuiContainer guiContainer = asGuiContainer(event.gui);
		if (guiContainer == null)
			return;

		overlay.drawScreen(guiContainer.mc, event.mouseX, event.mouseY);

		/**
		 * There is no way to render between the existing inventory tooltip and the dark background layer,
		 * so we have to re-render the inventory tooltip over the item list.
		 **/
		Slot theSlot = Reflection.getTheSlot(guiContainer);
		if (theSlot != null && theSlot.getHasStack()) {
			ItemStack itemStack = theSlot.getStack();
			Render.renderToolTip(itemStack, event.mouseX, event.mouseY);
		}
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END)
			return;

		Minecraft minecraft = Minecraft.getMinecraft();
		if (asGuiContainer(minecraft.currentScreen) == null)
			return;

		overlay.handleInput(minecraft);
	}

	private GuiContainer asGuiContainer(GuiScreen guiScreen) {
		if (!(guiScreen instanceof GuiContainer) || (guiScreen instanceof GuiContainerCreative))
			return null;
		return (GuiContainer)guiScreen;
	}
}
