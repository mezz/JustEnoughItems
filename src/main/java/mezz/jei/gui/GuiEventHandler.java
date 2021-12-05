package mezz.jei.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.events.EventBusHelper;
import mezz.jei.events.OverlayToggleEvent;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.LeftAreaDispatcher;
import mezz.jei.input.MouseUtil;
import mezz.jei.util.LimitedLogger;
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

	public void registerToEventBus() {
		EventBusHelper.registerWeakListener(this, OverlayToggleEvent.class, GuiEventHandler::onOverlayToggle);
		EventBusHelper.registerWeakListener(this, GuiScreenEvent.InitGuiEvent.Post.class, GuiEventHandler::onGuiInit);
		EventBusHelper.registerWeakListener(this, GuiOpenEvent.class, GuiEventHandler::onGuiOpen);
		EventBusHelper.registerWeakListener(this, GuiScreenEvent.BackgroundDrawnEvent.class, GuiEventHandler::onDrawBackgroundEventPost);
		EventBusHelper.registerWeakListener(this, GuiContainerEvent.DrawForeground.class, GuiEventHandler::onDrawForegroundEvent);
		EventBusHelper.registerWeakListener(this, GuiScreenEvent.DrawScreenEvent.Post.class, GuiEventHandler::onDrawScreenEventPost);
		EventBusHelper.registerWeakListener(this, TickEvent.ClientTickEvent.class, GuiEventHandler::onClientTick);
		EventBusHelper.registerWeakListener(this, GuiScreenEvent.PotionShiftEvent.class, GuiEventHandler::onPotionShiftEvent);
	}

	public void onOverlayToggle(OverlayToggleEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null) {
			return;
		}
		Screen currentScreen = minecraft.screen;
		ingredientListOverlay.updateScreen(currentScreen, true);
		leftAreaDispatcher.updateScreen(currentScreen, false);
	}

	public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
		Screen gui = event.getGui();
		ingredientListOverlay.updateScreen(gui, false);
		leftAreaDispatcher.updateScreen(gui, false);
	}

	public void onGuiOpen(GuiOpenEvent event) {
		Screen gui = event.getGui();
		ingredientListOverlay.updateScreen(gui, false);
		leftAreaDispatcher.updateScreen(gui, false);
	}

	public void onDrawBackgroundEventPost(GuiScreenEvent.BackgroundDrawnEvent event) {
		Screen gui = event.getGui();
		Minecraft minecraft = gui.getMinecraft();
		if (minecraft == null) {
			return;
		}
		boolean exclusionAreasChanged = guiScreenHelper.updateGuiExclusionAreas(gui);
		ingredientListOverlay.updateScreen(gui, exclusionAreasChanged);
		leftAreaDispatcher.updateScreen(gui, exclusionAreasChanged);

		drawnOnBackground = true;
		double mouseX = MouseUtil.getX();
		double mouseY = MouseUtil.getY();
		PoseStack poseStack = event.getMatrixStack();
		ingredientListOverlay.drawScreen(minecraft, poseStack, (int) mouseX, (int) mouseY, minecraft.getFrameTime());
		leftAreaDispatcher.drawScreen(minecraft, poseStack, (int) mouseX, (int) mouseY, minecraft.getFrameTime());
	}

	/**
	 * Draws above most ContainerScreen elements, but below the tooltips.
	 */
	public void onDrawForegroundEvent(GuiContainerEvent.DrawForeground event) {
		AbstractContainerScreen<?> gui = event.getGuiContainer();
		Minecraft minecraft = gui.getMinecraft();
		if (minecraft == null) {
			return;
		}
		ingredientListOverlay.drawOnForeground(minecraft, event.getMatrixStack(), gui, event.getMouseX(), event.getMouseY());
	}

	public void onDrawScreenEventPost(GuiScreenEvent.DrawScreenEvent.Post event) {
		Screen gui = event.getGui();
		Minecraft minecraft = gui.getMinecraft();
		if (minecraft == null) {
			return;
		}

		PoseStack poseStack = event.getMatrixStack();

		ingredientListOverlay.updateScreen(gui, false);
		leftAreaDispatcher.updateScreen(gui, false);

		if (!drawnOnBackground) {
			if (gui instanceof AbstractContainerScreen) {
				String guiName = gui.getClass().getName();
				missingBackgroundLogger.log(Level.WARN, guiName, "GUI did not draw the dark background layer behind itself, this may result in display issues: {}", guiName);
			}
			ingredientListOverlay.drawScreen(minecraft, poseStack, event.getMouseX(), event.getMouseY(), minecraft.getFrameTime());
			leftAreaDispatcher.drawScreen(minecraft, poseStack, event.getMouseX(), event.getMouseY(), minecraft.getFrameTime());
		}
		drawnOnBackground = false;

		if (gui instanceof AbstractContainerScreen<?> guiContainer) {
			IGuiClickableArea guiClickableArea = guiScreenHelper.getGuiClickableArea(guiContainer, event.getMouseX() - guiContainer.getGuiLeft(), event.getMouseY() - guiContainer.getGuiTop());
			if (guiClickableArea != null) {
				List<Component> tooltipStrings = guiClickableArea.getTooltipStrings();
				if (tooltipStrings.isEmpty()) {
					tooltipStrings = Collections.singletonList(new TranslatableComponent("jei.tooltip.show.recipes"));
				}
				TooltipRenderer.drawHoveringText(poseStack, tooltipStrings, event.getMouseX(), event.getMouseY(), minecraft.font);
			}
		}

		ingredientListOverlay.drawTooltips(minecraft, poseStack, event.getMouseX(), event.getMouseY());
		leftAreaDispatcher.drawTooltips(minecraft, poseStack, event.getMouseX(), event.getMouseY());
	}

	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			return;
		}

		ingredientListOverlay.handleTick();
	}

	public void onPotionShiftEvent(GuiScreenEvent.PotionShiftEvent event) {
		if (ingredientListOverlay.isListDisplayed()) {
			event.setCanceled(true);
		}
	}
}
