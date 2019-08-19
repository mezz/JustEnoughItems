package mezz.jei.gui;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.config.Constants;
import mezz.jei.events.OverlayToggleEvent;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.LeftAreaDispatcher;
import mezz.jei.input.MouseUtil;
import mezz.jei.util.LimitedLogger;
import mezz.jei.util.Translator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiEventHandler {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final LimitedLogger missingBackgroundLogger = new LimitedLogger(LOGGER, Duration.ofHours(1));

	private final IngredientListOverlay ingredientListOverlay;
	private final GuiScreenHelper guiScreenHelper;
	private final LeftAreaDispatcher leftAreaDispatcher;
	private boolean drawnOnBackground = false;

	public GuiEventHandler(
		GuiScreenHelper guiScreenHelper,
		LeftAreaDispatcher leftAreaDispatcher,
		IngredientListOverlay ingredientListOverlay
	) {
		this.guiScreenHelper = guiScreenHelper;
		this.leftAreaDispatcher = leftAreaDispatcher;
		this.ingredientListOverlay = ingredientListOverlay;
	}

	@SubscribeEvent
	public void onOverlayToggle(OverlayToggleEvent event) {
		Screen currentScreen = Minecraft.getInstance().currentScreen;
		ingredientListOverlay.updateScreen(currentScreen, true);
		leftAreaDispatcher.updateScreen(currentScreen, false);
	}

	@SubscribeEvent
	public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
		Screen gui = event.getGui();
		ingredientListOverlay.updateScreen(gui, false);
		leftAreaDispatcher.updateScreen(gui, false);
	}

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		Screen gui = event.getGui();
		ingredientListOverlay.updateScreen(gui, false);
		leftAreaDispatcher.updateScreen(gui, false);
	}

	@SubscribeEvent
	public void onDrawBackgroundEventPost(GuiScreenEvent.BackgroundDrawnEvent event) {
		Screen gui = event.getGui();
		Minecraft minecraft = gui.getMinecraft();
		if (minecraft == null) {
			return;
		}
		boolean exclusionAreasChanged = guiScreenHelper.updateGuiExclusionAreas();
		ingredientListOverlay.updateScreen(gui, exclusionAreasChanged);
		leftAreaDispatcher.updateScreen(gui, exclusionAreasChanged);

		drawnOnBackground = true;
		double mouseX = MouseUtil.getX();
		double mouseY = MouseUtil.getY();
		ingredientListOverlay.drawScreen(minecraft, (int) mouseX, (int) mouseY, minecraft.getRenderPartialTicks());
		leftAreaDispatcher.drawScreen(minecraft, (int) mouseX, (int) mouseY, minecraft.getRenderPartialTicks());
	}

	/**
	 * Draws above most ContainerScreen elements, but below the tooltips.
	 */
	@SubscribeEvent
	public void onDrawForegroundEvent(GuiContainerEvent.DrawForeground event) {
		ContainerScreen gui = event.getGuiContainer();
		Minecraft minecraft = gui.getMinecraft();
		if (minecraft == null) {
			return;
		}
		ingredientListOverlay.drawOnForeground(minecraft, gui, event.getMouseX(), event.getMouseY());
		leftAreaDispatcher.drawOnForeground(gui, event.getMouseX(), event.getMouseY());
	}

	@SubscribeEvent
	public void onDrawScreenEventPost(GuiScreenEvent.DrawScreenEvent.Post event) {
		Screen gui = event.getGui();
		Minecraft minecraft = gui.getMinecraft();
		if (minecraft == null) {
			return;
		}

		ingredientListOverlay.updateScreen(gui, false);
		leftAreaDispatcher.updateScreen(gui, false);

		if (!drawnOnBackground) {
			if (gui instanceof ContainerScreen) {
				String guiName = gui.getClass().getName();
				missingBackgroundLogger.log(Level.WARN, guiName, "GUI did not draw the dark background layer behind itself, this may result in display issues: {}", guiName);
			}
			ingredientListOverlay.drawScreen(minecraft, event.getMouseX(), event.getMouseY(), minecraft.getRenderPartialTicks());
			leftAreaDispatcher.drawScreen(minecraft, event.getMouseX(), event.getMouseY(), minecraft.getRenderPartialTicks());
		}
		drawnOnBackground = false;

		if (gui instanceof ContainerScreen) {
			ContainerScreen guiContainer = (ContainerScreen) gui;
			IGuiClickableArea guiClickableArea = guiScreenHelper.getGuiClickableArea(guiContainer, event.getMouseX() - guiContainer.getGuiLeft(), event.getMouseY() - guiContainer.getGuiTop());
			if (guiClickableArea != null) {
				List<String> tooltipStrings = guiClickableArea.getTooltipStrings();
				if (tooltipStrings.isEmpty()) {
					tooltipStrings = Collections.singletonList(Translator.translateToLocal("jei.tooltip.show.recipes"));
				}
				TooltipRenderer.drawHoveringText(tooltipStrings, event.getMouseX(), event.getMouseY(), Constants.MAX_TOOLTIP_WIDTH);
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
		if (ingredientListOverlay.isListDisplayed()) {
			event.setCanceled(true);
		}
	}
}
