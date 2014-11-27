package mezz.jei;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.gui.RecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuiEventHandler {

	@Nonnull private final ItemListOverlay itemListOverlay = new ItemListOverlay();
	@Nonnull private final RecipesGui recipesGui = new RecipesGui();

	@SubscribeEvent
	public void onGuiInit(@Nonnull GuiScreenEvent.InitGuiEvent.Post event) {
		GuiContainer guiContainer = asGuiContainer(event.gui);
		if (guiContainer == null)
			return;
		itemListOverlay.initGui(guiContainer, recipesGui);

		Minecraft minecraft = Minecraft.getMinecraft();

		recipesGui.initGui(minecraft);
	}

	@SubscribeEvent
	public void onDrawScreenEventPre(@Nonnull GuiScreenEvent.DrawScreenEvent.Pre event) {
		GuiContainer guiContainer = asGuiContainer(event.gui);
		if (guiContainer == null)
			return;

		if (recipesGui.isVisible())
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onDrawScreenEventPost(@Nonnull GuiScreenEvent.DrawScreenEvent.Post event) {
		GuiContainer guiContainer = asGuiContainer(event.gui);
		if (guiContainer == null)
			return;

		if (recipesGui.isVisible()) {
			recipesGui.drawBackground();
		}

		itemListOverlay.drawScreen(guiContainer.mc, event.mouseX, event.mouseY);

		if (recipesGui.isVisible())
			recipesGui.draw(event.mouseX, event.mouseY);

		itemListOverlay.drawHovered(guiContainer.mc, event.mouseX, event.mouseY);
		itemListOverlay.handleMouseEvent(guiContainer.mc, event.mouseX, event.mouseY);

		if (!recipesGui.isVisible()) {
			/**
			 * There is no way to render between the existing inventory tooltip and the dark background layer,
			 * so we have to re-render the inventory tooltip over the item list.
			 **/
			Slot theSlot = guiContainer.theSlot;
			if (theSlot != null && theSlot.getHasStack()) {
				ItemStack itemStack = theSlot.getStack();
				guiContainer.renderToolTip(itemStack, event.mouseX, event.mouseY);
			}
		}
	}

	@SubscribeEvent
	public void onClientTick(@Nonnull TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END)
			return;

		Minecraft minecraft = Minecraft.getMinecraft();
		GuiContainer guiContainer = asGuiContainer(minecraft.currentScreen);
		if (guiContainer == null)
			return;

		itemListOverlay.handleTick();

		Slot theSlot = guiContainer.theSlot;
		itemListOverlay.handleKeyEvent(theSlot);
	}

	@Nullable
	private GuiContainer asGuiContainer(GuiScreen guiScreen) {
		if (!(guiScreen instanceof GuiContainer) || (guiScreen instanceof GuiContainerCreative))
			return null;
		return (GuiContainer)guiScreen;
	}
}
