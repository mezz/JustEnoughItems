package mezz.jei;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.lwjgl.input.Mouse;

import mezz.jei.gui.ItemListOverlay;
import mezz.jei.gui.RecipesGui;
import mezz.jei.input.InputHandler;

public class GuiEventHandler {

	@Nullable
	private ItemListOverlay itemListOverlay;
	@Nonnull
	private final RecipesGui recipesGui = new RecipesGui();
	@Nullable
	private InputHandler inputHandler;

	public void setItemListOverlay(@Nullable ItemListOverlay itemListOverlay) {
		if (this.itemListOverlay != null) {
			this.itemListOverlay.close();
		}

		this.itemListOverlay = itemListOverlay;

		if (this.recipesGui.isOpen()) {
			this.recipesGui.close();
		}
	}

	@SubscribeEvent
	public void onGuiInit(@Nonnull GuiScreenEvent.InitGuiEvent.Post event) {
		if (itemListOverlay == null) {
			return;
		}
		Minecraft minecraft = Minecraft.getMinecraft();
		GuiContainer guiContainer = asGuiContainer(minecraft.currentScreen);
		if (guiContainer == null) {
			return;
		}

		itemListOverlay.initGui(guiContainer);

		recipesGui.initGui(minecraft);
		inputHandler = new InputHandler(recipesGui, itemListOverlay, guiContainer);
	}
	
	@SubscribeEvent
	public void onGuiOpen(@Nonnull GuiOpenEvent event) {
		if (itemListOverlay == null) {
			return;
		}
		if (event.gui == null && itemListOverlay.isOpen()) {
			itemListOverlay.close();
		}
	}

	@SubscribeEvent
	public void onDrawScreenEventPre(@Nonnull GuiScreenEvent.DrawScreenEvent.Pre event) {
		GuiContainer guiContainer = asGuiContainer(event.gui);
		if (guiContainer == null) {
			return;
		}

		if (recipesGui.isOpen()) {
			event.setCanceled(true);
			recipesGui.drawBackground();
		}
	}

	@SubscribeEvent
	public void onDrawBackgroundEventPost(@Nonnull GuiScreenEvent.BackgroundDrawnEvent event) {
		if (itemListOverlay == null) {
			return;
		}

		GuiContainer guiContainer = asGuiContainer(event.gui);
		if (guiContainer != null) {
			itemListOverlay.updateGui(guiContainer);
		}

		itemListOverlay.drawScreen(event.gui.mc, event.getMouseX(), event.getMouseY());
	}

	@SubscribeEvent
	public void onDrawScreenEventPost(@Nonnull GuiScreenEvent.DrawScreenEvent.Post event) {
		if (itemListOverlay == null) {
			return;
		}

		GuiContainer guiContainer = asGuiContainer(event.gui);
		if (guiContainer == null) {
			return;
		}

		recipesGui.draw(event.mouseX, event.mouseY);
		recipesGui.drawHovered(event.mouseX, event.mouseY);
		itemListOverlay.drawHovered(guiContainer.mc, event.mouseX, event.mouseY);
	}

	@SubscribeEvent
	public void onClientTick(@Nonnull TickEvent.ClientTickEvent event) {
		if (itemListOverlay == null || event.phase == TickEvent.Phase.END) {
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
		if (!(event.gui instanceof GuiContainer)) {
			return;
		}
		if (inputHandler != null) {
			if (inputHandler.handleKeyEvent()) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onGuiMouseEvent(GuiScreenEvent.MouseInputEvent.Pre event) {
		GuiScreen gui = event.gui;
		if (!(gui instanceof GuiContainer)) {
			return;
		}
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
		if (!(guiScreen instanceof GuiContainer)) {
			return null;
		}
		return (GuiContainer) guiScreen;
	}
}
