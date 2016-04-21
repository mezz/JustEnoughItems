package mezz.jei;

import mezz.jei.config.Config;
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
	private InputHandler inputHandler;
	@Nullable
	private GuiContainer previousGui = null;

	@SubscribeEvent
	public void onGuiInit(@Nonnull GuiScreenEvent.InitGuiEvent.Post event) {
		JeiRuntime runtime = Internal.getRuntime();
		if (runtime == null) {
			return;
		}
		ItemListOverlay itemListOverlay = runtime.getItemListOverlay();

		GuiScreen gui = event.getGui();
		if (gui instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) gui;
			itemListOverlay.initGui(guiContainer);

			RecipesGui recipesGui = new RecipesGui();
			inputHandler = new InputHandler(recipesGui, itemListOverlay);
		}
	}
	
	@SubscribeEvent
	public void onGuiOpen(@Nonnull GuiOpenEvent event) {
		JeiRuntime runtime = Internal.getRuntime();
		if (runtime == null) {
			return;
		}
		ItemListOverlay itemListOverlay = runtime.getItemListOverlay();

		GuiScreen gui = event.getGui();
		if (gui instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) gui;
			if (previousGui != guiContainer) {
				previousGui = guiContainer;
				if (itemListOverlay.isOpen()) {
					itemListOverlay.close();
				}
			}
		} else if (!(gui instanceof RecipesGui)) {
			if (itemListOverlay.isOpen()) {
				itemListOverlay.close();
			}
		}
	}

	@SubscribeEvent
	public void onDrawBackgroundEventPost(@Nonnull GuiScreenEvent.BackgroundDrawnEvent event) {
		JeiRuntime runtime = Internal.getRuntime();
		if (runtime == null) {
			return;
		}

		ItemListOverlay itemListOverlay = runtime.getItemListOverlay();
		if (itemListOverlay.isOpen()) {
			GuiScreen gui = event.getGui();
			itemListOverlay.updateGui(gui);
			itemListOverlay.drawScreen(gui.mc, event.getMouseX(), event.getMouseY());
		}
	}

	@SubscribeEvent
	public void onDrawScreenEventPost(@Nonnull GuiScreenEvent.DrawScreenEvent.Post event) {
		JeiRuntime runtime = Internal.getRuntime();
		if (runtime == null) {
			return;
		}

		GuiScreen gui = event.getGui();
		if (gui instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) gui;
			RecipeRegistry recipeRegistry = Internal.getRuntime().getRecipeRegistry();
			if (recipeRegistry.getRecipeClickableArea(guiContainer, event.getMouseX() - guiContainer.guiLeft, event.getMouseY() - guiContainer.guiTop) != null) {
				TooltipRenderer.drawHoveringText(guiContainer.mc, showRecipesText, event.getMouseX(), event.getMouseY());
			}
		}

		ItemListOverlay itemListOverlay = runtime.getItemListOverlay();
		if (itemListOverlay.isOpen()) {
			itemListOverlay.drawTooltips(gui.mc, event.getMouseX(), event.getMouseY());
		}
	}

	@SubscribeEvent
	public void onClientTick(@Nonnull TickEvent.ClientTickEvent event) {
		JeiRuntime runtime = Internal.getRuntime();
		if (runtime == null) {
			return;
		}

		if (event.phase == TickEvent.Phase.END) {
			return;
		}

		ItemListOverlay itemListOverlay = runtime.getItemListOverlay();
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
		GuiScreen guiScreen = event.getGui();
		if (inputHandler != null) {
			int x = Mouse.getEventX() * guiScreen.width / guiScreen.mc.displayWidth;
			int y = guiScreen.height - Mouse.getEventY() * guiScreen.height / guiScreen.mc.displayHeight - 1;
			if (inputHandler.handleMouseEvent(guiScreen, x, y)) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onPotionShiftEvent(GuiScreenEvent.PotionShiftEvent event) {
		if (Config.isOverlayEnabled()) {
			event.setCanceled(true);
		}
	}
}
