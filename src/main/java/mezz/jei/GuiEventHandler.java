package mezz.jei;

import javax.annotation.Nullable;

import mezz.jei.config.Config;
import mezz.jei.config.OverlayToggleEvent;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.gui.ItemListOverlayInternal;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.InputHandler;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

public class GuiEventHandler {
	private static final String showRecipesText = Translator.translateToLocal("jei.tooltip.show.recipes");
	private final JeiRuntime runtime;
	@Nullable
	private InputHandler inputHandler;
	@Nullable
	private GuiContainer previousGui = null;

	public GuiEventHandler(JeiRuntime runtime) {
		this.runtime = runtime;
	}

	@SubscribeEvent
	public void onOverlayToggle(OverlayToggleEvent event) {
		GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
		onNewScreen(currentScreen);
	}

	@SubscribeEvent
	public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
		GuiScreen gui = event.getGui();
		onNewScreen(gui);
	}

	private void onNewScreen(@Nullable GuiScreen screen) {
		if (screen instanceof GuiContainer || screen instanceof RecipesGui) {
			ItemListOverlay itemListOverlay = runtime.getItemListOverlay();
			ItemListOverlayInternal itemListOverlayInternal = itemListOverlay.create(screen);
			inputHandler = new InputHandler(runtime, itemListOverlayInternal);
		} else {
			inputHandler = null;
		}
	}

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
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
	public void onDrawBackgroundEventPost(GuiScreenEvent.BackgroundDrawnEvent event) {
		ItemListOverlay itemListOverlay = runtime.getItemListOverlay();
		ItemListOverlayInternal itemListOverlayInternal = itemListOverlay.getInternal();
		if (itemListOverlayInternal != null) {
			GuiScreen gui = event.getGui();
			if (itemListOverlayInternal.hasScreenChanged(gui)) {
				itemListOverlayInternal = itemListOverlay.create(gui);
				inputHandler = new InputHandler(runtime, itemListOverlayInternal);
			}

			if (itemListOverlayInternal != null) {
				itemListOverlayInternal.drawScreen(gui.mc, event.getMouseX(), event.getMouseY());
			}
		}
	}

	@SubscribeEvent
	public void onDrawScreenEventPost(GuiScreenEvent.DrawScreenEvent.Post event) {
		GuiScreen gui = event.getGui();
		if (gui instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) gui;
			RecipeRegistry recipeRegistry = runtime.getRecipeRegistry();
			if (recipeRegistry.getRecipeClickableArea(guiContainer, event.getMouseX() - guiContainer.getGuiLeft(), event.getMouseY() - guiContainer.getGuiTop()) != null) {
				TooltipRenderer.drawHoveringText(guiContainer.mc, showRecipesText, event.getMouseX(), event.getMouseY());
			}
		}

		ItemListOverlay itemListOverlay = runtime.getItemListOverlay();
		ItemListOverlayInternal itemListOverlayInternal = itemListOverlay.getInternal();
		if (itemListOverlayInternal != null) {
			itemListOverlayInternal.drawTooltips(gui.mc, event.getMouseX(), event.getMouseY());
		}
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			return;
		}

		ItemListOverlay itemListOverlay = runtime.getItemListOverlay();
		ItemListOverlayInternal itemListOverlayInternal = itemListOverlay.getInternal();
		if (itemListOverlayInternal != null) {
			itemListOverlayInternal.handleTick();
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
