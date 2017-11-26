package mezz.jei.gui;

import javax.annotation.Nullable;

import mezz.jei.config.Config;
import mezz.jei.config.OverlayToggleEvent;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.input.InputHandler;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiContainerEvent;
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
	private boolean drawnOnBackground = false;

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
		IngredientListOverlay ingredientListOverlay = runtime.getItemListOverlay();
		ingredientListOverlay.updateScreen(screen);
		if (inputHandler == null) {
			inputHandler = new InputHandler(runtime, ingredientListOverlay);
		}
	}

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		IngredientListOverlay ingredientListOverlay = runtime.getItemListOverlay();
		final boolean wasEnabled = ingredientListOverlay.isEnabled();

		GuiScreen gui = event.getGui();
		ingredientListOverlay.updateScreen(gui);

		if (wasEnabled && !ingredientListOverlay.isEnabled()) {
			Config.saveFilterText();
		}
	}

	@SubscribeEvent
	public void onDrawBackgroundEventPost(GuiScreenEvent.BackgroundDrawnEvent event) {
		IngredientListOverlay ingredientListOverlay = runtime.getItemListOverlay();
		GuiScreen gui = event.getGui();
		ingredientListOverlay.updateScreen(gui);

		if (ingredientListOverlay.isEnabled()) {
			ingredientListOverlay.drawScreen(gui.mc, event.getMouseX(), event.getMouseY(), gui.mc.getRenderPartialTicks());
			drawnOnBackground = true;
		}
	}

	/**
	 * Draws above most GuiContainer elements, but below the tooltips.
	 */
	@SubscribeEvent
	public void onDrawForegroundEvent(GuiContainerEvent.DrawForeground event) {
		IngredientListOverlay ingredientListOverlay = runtime.getItemListOverlay();
		GuiContainer gui = event.getGuiContainer();

		if (ingredientListOverlay.isEnabled()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(-gui.getGuiLeft(), -gui.getGuiTop(), 0);
			ingredientListOverlay.drawOnForeground(gui.mc, event.getMouseX(), event.getMouseY());
			GlStateManager.popMatrix();
		}
	}

	@SubscribeEvent
	public void onDrawScreenEventPost(GuiScreenEvent.DrawScreenEvent.Post event) {
		GuiScreen gui = event.getGui();

		IngredientListOverlay ingredientListOverlay = runtime.getItemListOverlay();
		ingredientListOverlay.updateScreen(gui);

		if (!drawnOnBackground && ingredientListOverlay.isEnabled()) {
			ingredientListOverlay.drawScreen(gui.mc, event.getMouseX(), event.getMouseY(), gui.mc.getRenderPartialTicks());
		}
		drawnOnBackground = false;

		if (gui instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) gui;
			RecipeRegistry recipeRegistry = runtime.getRecipeRegistry();
			if (recipeRegistry.getRecipeClickableArea(guiContainer, event.getMouseX() - guiContainer.getGuiLeft(), event.getMouseY() - guiContainer.getGuiTop()) != null) {
				TooltipRenderer.drawHoveringText(guiContainer.mc, showRecipesText, event.getMouseX(), event.getMouseY());
			}
		}

		if (ingredientListOverlay.isEnabled()) {
			ingredientListOverlay.drawTooltips(gui.mc, event.getMouseX(), event.getMouseY());
		}
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			return;
		}

		IngredientListOverlay ingredientListOverlay = runtime.getItemListOverlay();
		if (ingredientListOverlay.isEnabled()) {
			ingredientListOverlay.handleTick();
		}
	}

	/**
	 * When we have keyboard focus, use Pre
	 */
	@SubscribeEvent
	public void onGuiKeyboardEvent(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (inputHandler != null && inputHandler.hasKeyboardFocus()) {
			if (inputHandler.handleKeyEvent()) {
				event.setCanceled(true);
			}
		}
	}

	/**
	 * Without focus, use Post
	 */
	@SubscribeEvent
	public void onGuiKeyboardEvent(GuiScreenEvent.KeyboardInputEvent.Post event) {
		if (inputHandler != null && !inputHandler.hasKeyboardFocus()) {
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
