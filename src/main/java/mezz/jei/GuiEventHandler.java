package mezz.jei;

import mezz.jei.gui.ItemListOverlay;
import mezz.jei.gui.RecipesGui;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.input.InputHandler;
import mezz.jei.util.Translator;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuiEventHandler {
	@Nonnull
	private static final String showRecipesText = Translator.translateToLocal("jei.tooltip.show.recipes");
	@Nullable
	private ItemListOverlay itemListOverlay;
	@Nullable
	private InputHandler inputHandler;
	@Nullable
	private GuiContainer previousGui = null;

	public void setItemListOverlay(@Nullable ItemListOverlay itemListOverlay) {
		if (this.itemListOverlay != null && this.itemListOverlay.isOpen()) {
			this.itemListOverlay.close();
		}

		this.itemListOverlay = itemListOverlay;
	}

	@SubscribeEvent
	public void onGuiInit(@Nonnull GuiScreenEvent.InitGuiEvent.Post event) {
		if (itemListOverlay == null) {
			return;
		}

		if (event.gui instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) event.gui;
			itemListOverlay.initGui(guiContainer);

			RecipesGui recipesGui = new RecipesGui();
			inputHandler = new InputHandler(recipesGui, itemListOverlay);
		}
	}
	
	@SubscribeEvent
	public void onGuiOpen(@Nonnull GuiOpenEvent event) {
		if (itemListOverlay == null) {
			return;
		}

		if (event.gui instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) event.gui;
			if (previousGui != guiContainer) {
				previousGui = guiContainer;
				if (itemListOverlay.isOpen()) {
					itemListOverlay.close();
				}
			}
		} else if (!(event.gui instanceof RecipesGui)) {
			if (itemListOverlay.isOpen()) {
				itemListOverlay.close();
			}
		}
	}

	@SubscribeEvent
	public void onDrawBackgroundEventPost(@Nonnull GuiScreenEvent.BackgroundDrawnEvent event) {
		if (itemListOverlay != null && itemListOverlay.isOpen()) {
			itemListOverlay.updateGui(event.gui);
			itemListOverlay.drawScreen(event.gui.mc, event.getMouseX(), event.getMouseY());
		}
	}

	@SubscribeEvent
	public void onDrawScreenEventPost(@Nonnull GuiScreenEvent.DrawScreenEvent.Post event) {
		if (itemListOverlay == null) {
			return;
		}

		if (event.gui instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) event.gui;
			RecipeRegistry recipeRegistry = Internal.getRuntime().getRecipeRegistry();
			if (recipeRegistry.getRecipeClickableArea(guiContainer, event.mouseX - guiContainer.guiLeft, event.mouseY - guiContainer.guiTop) != null) {
				TooltipRenderer.drawHoveringText(guiContainer.mc, showRecipesText, event.mouseX, event.mouseY);
			}
		}

		if (itemListOverlay.isOpen()) {
			itemListOverlay.drawTooltips(event.gui.mc, event.mouseX, event.mouseY);
		}
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
		if (inputHandler != null) {
			if (inputHandler.handleKeyEvent()) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onGuiMouseEvent(GuiScreenEvent.MouseInputEvent.Pre event) {
		GuiScreen guiScreen = event.gui;
		if (inputHandler != null) {
			int x = Mouse.getEventX() * guiScreen.width / guiScreen.mc.displayWidth;
			int y = guiScreen.height - Mouse.getEventY() * guiScreen.height / guiScreen.mc.displayHeight - 1;
			if (inputHandler.handleMouseEvent(guiScreen, x, y)) {
				event.setCanceled(true);
			}
		}
	}
}
