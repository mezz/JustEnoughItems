package mezz.jei.gui;

import java.util.Collections;
import java.util.List;

import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import mezz.jei.api.gui.IGuiClickableArea;
import mezz.jei.config.Constants;
import mezz.jei.events.OverlayToggleEvent;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.LeftAreaDispatcher;
import mezz.jei.input.MouseUtil;
import mezz.jei.util.Translator;

public class GuiEventHandler {
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
		GuiScreen currentScreen = Minecraft.getInstance().currentScreen;
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
		boolean exclusionAreasChanged = guiScreenHelper.updateGuiExclusionAreas();
		ingredientListOverlay.updateScreen(gui, exclusionAreasChanged);
		leftAreaDispatcher.updateScreen(gui, exclusionAreasChanged);

		drawnOnBackground = true;
		double mouseX = MouseUtil.getX();
		double mouseY = MouseUtil.getY();
		ingredientListOverlay.drawScreen(gui.mc, (int) mouseX, (int) mouseY, gui.mc.getRenderPartialTicks());
		leftAreaDispatcher.drawScreen(gui.mc, (int) mouseX, (int) mouseY, gui.mc.getRenderPartialTicks());
	}

	/**
	 * Draws above most GuiContainer elements, but below the tooltips.
	 */
	@SubscribeEvent
	public void onDrawForegroundEvent(GuiContainerEvent.DrawForeground event) {
		GuiContainer gui = event.getGuiContainer();
		ingredientListOverlay.drawOnForeground(gui, event.getMouseX(), event.getMouseY());
		leftAreaDispatcher.drawOnForeground(gui, event.getMouseX(), event.getMouseY());
	}

	@SubscribeEvent
	public void onDrawScreenEventPost(GuiScreenEvent.DrawScreenEvent.Post event) {
		GuiScreen gui = event.getGui();

		ingredientListOverlay.updateScreen(gui, false);
		leftAreaDispatcher.updateScreen(gui, false);

		if (!drawnOnBackground) {
			ingredientListOverlay.drawScreen(gui.mc, event.getMouseX(), event.getMouseY(), gui.mc.getRenderPartialTicks());
			leftAreaDispatcher.drawScreen(gui.mc, event.getMouseX(), event.getMouseY(), gui.mc.getRenderPartialTicks());
		}
		drawnOnBackground = false;

		if (gui instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) gui;
			IGuiClickableArea guiClickableArea = guiScreenHelper.getGuiClickableArea(guiContainer, event.getMouseX() - guiContainer.getGuiLeft(), event.getMouseY() - guiContainer.getGuiTop());
			if (guiClickableArea != null) {
				List<String> tooltipStrings = guiClickableArea.getTooltipStrings();
				if (tooltipStrings.isEmpty()) {
					tooltipStrings = Collections.singletonList(Translator.translateToLocal("jei.tooltip.show.recipes"));
				}
				TooltipRenderer.drawHoveringText(tooltipStrings, event.getMouseX(), event.getMouseY(), Constants.MAX_TOOLTIP_WIDTH);
			}
		}

		ingredientListOverlay.drawTooltips(gui.mc, event.getMouseX(), event.getMouseY());
		leftAreaDispatcher.drawTooltips(gui.mc, event.getMouseX(), event.getMouseY());
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
