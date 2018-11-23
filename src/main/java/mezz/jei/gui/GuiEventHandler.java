package mezz.jei.gui;

import mezz.jei.config.Config;
import mezz.jei.config.OverlayToggleEvent;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class GuiEventHandler {
	private final IngredientListOverlay ingredientListOverlay;
	private final RecipeRegistry recipeRegistry;
	private boolean drawnOnBackground = false;

	public GuiEventHandler(IngredientListOverlay ingredientListOverlay, RecipeRegistry recipeRegistry) {
		this.ingredientListOverlay = ingredientListOverlay;
		this.recipeRegistry = recipeRegistry;
	}

	@SubscribeEvent
	public void onOverlayToggle(OverlayToggleEvent event) {
		GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
		ingredientListOverlay.updateScreen(currentScreen, true);
	}

	@SubscribeEvent
	public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
		GuiScreen gui = event.getGui();
		ingredientListOverlay.updateScreen(gui, false);
	}

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		GuiScreen gui = event.getGui();
		ingredientListOverlay.updateScreen(gui, false);
	}

	@SubscribeEvent
	public void onDrawBackgroundEventPost(GuiScreenEvent.BackgroundDrawnEvent event) {
		GuiScreen gui = event.getGui();
		ingredientListOverlay.updateScreen(gui, false);

		drawnOnBackground = true;
		ingredientListOverlay.drawScreen(gui.mc, event.getMouseX(), event.getMouseY(), gui.mc.getRenderPartialTicks());
	}

	/**
	 * Draws above most GuiContainer elements, but below the tooltips.
	 */
	@SubscribeEvent
	public void onDrawForegroundEvent(GuiContainerEvent.DrawForeground event) {
		GuiContainer gui = event.getGuiContainer();
		ingredientListOverlay.drawOnForeground(gui, event.getMouseX(), event.getMouseY());
	}

	@SubscribeEvent
	public void onDrawScreenEventPost(GuiScreenEvent.DrawScreenEvent.Post event) {
		GuiScreen gui = event.getGui();

		ingredientListOverlay.updateScreen(gui, false);

		if (!drawnOnBackground) {
			ingredientListOverlay.drawScreen(gui.mc, event.getMouseX(), event.getMouseY(), gui.mc.getRenderPartialTicks());
		}
		drawnOnBackground = false;

		if (gui instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) gui;
			if (recipeRegistry.getRecipeClickableArea(guiContainer, event.getMouseX() - guiContainer.getGuiLeft(), event.getMouseY() - guiContainer.getGuiTop()) != null) {
				String showRecipesText = Translator.translateToLocal("jei.tooltip.show.recipes");
				TooltipRenderer.drawHoveringText(guiContainer.mc, showRecipesText, event.getMouseX(), event.getMouseY());
			}
		}

		ingredientListOverlay.drawTooltips(gui.mc, event.getMouseX(), event.getMouseY());
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			return;
		}

		ingredientListOverlay.handleTick();
	}

	@SubscribeEvent
	public void onPotionShiftEvent(GuiScreenEvent.PotionShiftEvent event) {
		if (Config.isOverlayEnabled()) {
			event.setCanceled(true);
		}
	}
}
