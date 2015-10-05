package mezz.jei;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.lwjgl.input.Mouse;

import mezz.jei.gui.ItemListOverlay;
import mezz.jei.gui.RecipesGui;
import mezz.jei.input.InputHandler;

public class GuiEventHandler {

	@Nonnull
	private final ItemListOverlay itemListOverlay;
	@Nonnull
	private final RecipesGui recipesGui = new RecipesGui();
	@Nullable
	private InputHandler inputHandler;

	public GuiEventHandler(@Nonnull ItemListOverlay itemListOverlay) {
		this.itemListOverlay = itemListOverlay;
	}

	@SubscribeEvent
	public void onGuiInit(@Nonnull GuiScreenEvent.InitGuiEvent.Post event) {
		GuiContainer guiContainer = asGuiContainer(event.gui);
		if (guiContainer == null) {
			return;
		}
		itemListOverlay.initGui(guiContainer);

		Minecraft minecraft = Minecraft.getMinecraft();

		recipesGui.initGui(minecraft);

		inputHandler = new InputHandler(minecraft, recipesGui, itemListOverlay, guiContainer);

		itemListOverlay.open();
	}

	@SubscribeEvent
	public void onDrawScreenEventPre(@Nonnull GuiScreenEvent.DrawScreenEvent.Pre event) {
		GuiContainer guiContainer = asGuiContainer(event.gui);
		if (guiContainer == null) {
			return;
		}

		if (recipesGui.isOpen()) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onDrawScreenEventPost(@Nonnull GuiScreenEvent.DrawScreenEvent.Post event) {
		GuiContainer guiContainer = asGuiContainer(event.gui);
		if (guiContainer == null) {
			return;
		}

		if (recipesGui.isOpen()) {
			recipesGui.drawBackground();
		}

		itemListOverlay.drawScreen(guiContainer.mc, event.mouseX, event.mouseY);

		if (recipesGui.isOpen()) {
			recipesGui.draw(event.mouseX, event.mouseY);
		}

		itemListOverlay.drawHovered(guiContainer.mc, event.mouseX, event.mouseY);

		if (!recipesGui.isOpen()) {
			/**
			 * There is no way to render between the existing inventory tooltip and the dark background layer,
			 * so we have to re-render the inventory tooltip over the item list.
			 **/
			Slot slotUnderMouse = guiContainer.getSlotUnderMouse();
			if (slotUnderMouse != null && slotUnderMouse.getHasStack()) {
				ItemStack itemStack = slotUnderMouse.getStack();
				guiContainer.renderToolTip(itemStack, event.mouseX, event.mouseY);
			}
		}
	}

	@SubscribeEvent
	public void onClientTick(@Nonnull TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			return;
		}

		Minecraft minecraft = Minecraft.getMinecraft();
		GuiContainer guiContainer = asGuiContainer(minecraft.currentScreen);
		if (guiContainer == null) {
			return;
		}

		itemListOverlay.handleTick();
	}

	@SubscribeEvent
	public void onGuiKeyboardEvent(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (inputHandler != null) {
			if (inputHandler.handleKeyEvent()) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onGuiMouseEvent(GuiScreenEvent.MouseInputEvent.Pre event) {
		GuiScreen gui = event.gui;
		if (inputHandler != null) {
			int x = Mouse.getEventX() * gui.width / gui.mc.displayWidth;
			int y = gui.height - Mouse.getEventY() * gui.height / gui.mc.displayHeight - 1;
			if (inputHandler.handleMouseEvent(x, y)) {
				event.setCanceled(true);
			}
		}
	}

	@Nullable
	private GuiContainer asGuiContainer(GuiScreen guiScreen) {
		if (!(guiScreen instanceof GuiContainer) || (guiScreen instanceof GuiContainerCreative)) {
			return null;
		}
		return (GuiContainer) guiScreen;
	}
}
