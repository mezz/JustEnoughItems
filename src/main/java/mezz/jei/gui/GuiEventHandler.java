package mezz.jei.gui;

import mezz.jei.util.LimitedLogger;
import mezz.jei.util.Log;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import mezz.jei.config.Config;
import mezz.jei.config.OverlayToggleEvent;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.LeftAreaDispatcher;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.util.Translator;
import org.apache.logging.log4j.Level;

import java.time.Duration;

public class GuiEventHandler {
	private final IngredientListOverlay ingredientListOverlay;
	private final GuiScreenHelper guiScreenHelper;
	private final LeftAreaDispatcher leftAreaDispatcher;
	private final RecipeRegistry recipeRegistry;
	private final LimitedLogger missingBackgroundLogger = new LimitedLogger(Log.get(), Duration.ofHours(1));
	private boolean drawnOnBackground = false;

	public GuiEventHandler(GuiScreenHelper guiScreenHelper, LeftAreaDispatcher leftAreaDispatcher, IngredientListOverlay ingredientListOverlay, RecipeRegistry recipeRegistry) {
		this.guiScreenHelper = guiScreenHelper;
		this.leftAreaDispatcher = leftAreaDispatcher;
		this.ingredientListOverlay = ingredientListOverlay;
		this.recipeRegistry = recipeRegistry;
	}

	@SubscribeEvent
	public void onOverlayToggle(OverlayToggleEvent event) {
		GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
		ingredientListOverlay.updateScreen(currentScreen, true);
		leftAreaDispatcher.updateScreen(currentScreen, false);
	}

	@SubscribeEvent
	public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
		GuiScreen gui = event.getGui();
		ingredientListOverlay.updateScreen(gui, false);
		leftAreaDispatcher.updateScreen(gui, false);
	}

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		GuiScreen gui = event.getGui();
		ingredientListOverlay.updateScreen(gui, false);
		leftAreaDispatcher.updateScreen(gui, false);
	}

	@SubscribeEvent
	public void onDrawBackgroundEventPost(GuiScreenEvent.BackgroundDrawnEvent event) {
		GuiScreen gui = event.getGui();
		Minecraft minecraft = gui.mc;
		if (minecraft == null) {
			return;
		}
		boolean exclusionAreasChanged = guiScreenHelper.updateGuiExclusionAreas();
		ingredientListOverlay.updateScreen(gui, exclusionAreasChanged);
		leftAreaDispatcher.updateScreen(gui, exclusionAreasChanged);

		drawnOnBackground = true;
		ingredientListOverlay.drawScreen(minecraft, event.getMouseX(), event.getMouseY(), minecraft.getRenderPartialTicks());
		leftAreaDispatcher.drawScreen(minecraft, event.getMouseX(), event.getMouseY(), minecraft.getRenderPartialTicks());
	}

	/**
	 * Draws above most GuiContainer elements, but below the tooltips.
	 */
	@SubscribeEvent
	public void onDrawForegroundEvent(GuiContainerEvent.DrawForeground event) {
		GuiContainer gui = event.getGuiContainer();
		Minecraft minecraft = gui.mc;
		if (minecraft == null) {
			return;
		}
		ingredientListOverlay.drawOnForeground(minecraft, gui, event.getMouseX(), event.getMouseY());
		leftAreaDispatcher.drawOnForeground(gui, event.getMouseX(), event.getMouseY());
	}

	@SubscribeEvent
	public void onDrawScreenEventPost(GuiScreenEvent.DrawScreenEvent.Post event) {
		GuiScreen gui = event.getGui();
		Minecraft minecraft = gui.mc;
		if (minecraft == null) {
			return;
		}

		ingredientListOverlay.updateScreen(gui, false);
		leftAreaDispatcher.updateScreen(gui, false);

		if (!drawnOnBackground) {
			if (gui instanceof GuiContainer) {
				String guiName = gui.getClass().getName();
				missingBackgroundLogger.log(Level.WARN, guiName, "GUI did not draw the dark background layer behind itself, this may result in display issues: {}", guiName);
			}
			ingredientListOverlay.drawScreen(minecraft, event.getMouseX(), event.getMouseY(), minecraft.getRenderPartialTicks());
			leftAreaDispatcher.drawScreen(minecraft, event.getMouseX(), event.getMouseY(), minecraft.getRenderPartialTicks());
		}
		drawnOnBackground = false;

		if (gui instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) gui;
			if (recipeRegistry.getRecipeClickableArea(guiContainer, event.getMouseX() - guiContainer.getGuiLeft(), event.getMouseY() - guiContainer.getGuiTop()) != null) {
				String showRecipesText = Translator.translateToLocal("jei.tooltip.show.recipes");
				TooltipRenderer.drawHoveringText(minecraft, showRecipesText, event.getMouseX(), event.getMouseY());
			}
		}

		ingredientListOverlay.drawTooltips(minecraft, event.getMouseX(), event.getMouseY());
		leftAreaDispatcher.drawTooltips(minecraft, event.getMouseX(), event.getMouseY());
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
