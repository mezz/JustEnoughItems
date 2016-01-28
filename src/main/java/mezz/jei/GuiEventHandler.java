package mezz.jei;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.lwjgl.input.Mouse;

import mezz.jei.gui.ItemListOverlay;
import mezz.jei.gui.RecipeClickableArea;
import mezz.jei.gui.RecipesGui;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.input.InputHandler;
import mezz.jei.util.Translator;

public class GuiEventHandler {

	@Nullable
	private ItemListOverlay itemListOverlay;
	@Nonnull
	private final RecipesGui recipesGui = new RecipesGui();
	@Nonnull
	private final String showRecipesText = Translator.translateToLocal("jei.tooltip.show.recipes");
	@Nullable
	private InputHandler inputHandler;
	@Nullable
	private GuiScreen previousGui = null;

	public void setItemListOverlay(@Nullable ItemListOverlay itemListOverlay) {
		if (this.itemListOverlay != null) {
			this.itemListOverlay.close();
		}

		this.itemListOverlay = itemListOverlay;
	}

	@SubscribeEvent
	public void onGuiInit(@Nonnull GuiScreenEvent.InitGuiEvent.Post event) {
		if (itemListOverlay == null) {
			return;
		}

		GuiContainer guiContainer = asGuiContainer(event.gui);
		if (guiContainer == null) {
			return;
		}

		itemListOverlay.initGui(guiContainer);

		inputHandler = new InputHandler(recipesGui, itemListOverlay);
	}
	
	@SubscribeEvent
	public void onGuiOpen(@Nonnull GuiOpenEvent event) {
		if (itemListOverlay == null) {
			return;
		}
		if (previousGui != event.gui && recipesGui != event.gui) {
			previousGui = event.gui;
			if (itemListOverlay.isOpen()) {
				itemListOverlay.close();
			}
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

		RecipeClickableArea clickableArea = Internal.getRuntime().getRecipeRegistry().getRecipeClickableArea(guiContainer);
		if (clickableArea != null && clickableArea.checkHover(event.mouseX - guiContainer.guiLeft, event.mouseY - guiContainer.guiTop)) {
			TooltipRenderer.drawHoveringText(guiContainer.mc, showRecipesText, event.mouseX, event.mouseY);
		}

		itemListOverlay.drawHovered(guiContainer.mc, event.mouseX, event.mouseY);
	}

	@SubscribeEvent
	public void onClientTick(@Nonnull TickEvent.ClientTickEvent event) {
		if (itemListOverlay == null || event.phase == TickEvent.Phase.END) {
			return;
		}

		if (itemListOverlay.isOpen()) {
			itemListOverlay.handleTick();
		}
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
		if (!(event.gui instanceof GuiContainer)) {
			return;
		}
		GuiContainer guiContainer = (GuiContainer) event.gui;
		if (inputHandler != null) {
			int x = Mouse.getEventX() * guiContainer.width / guiContainer.mc.displayWidth;
			int y = guiContainer.height - Mouse.getEventY() * guiContainer.height / guiContainer.mc.displayHeight - 1;
			if (inputHandler.handleMouseEvent(guiContainer, x, y)) {
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
