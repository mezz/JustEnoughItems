package mezz.jei.common.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.common.gui.overlay.IngredientListOverlay;
import mezz.jei.common.gui.overlay.bookmarks.LeftAreaDispatcher;
import mezz.jei.common.input.MouseUtil;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.LimitedLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.List;

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

	public void onGuiInit(Screen screen) {
		ingredientListOverlay.updateScreen(screen, false);
		leftAreaDispatcher.updateScreen(screen, false);
	}

	public void onGuiOpen(Screen screen) {
		ingredientListOverlay.updateScreen(screen, false);
		leftAreaDispatcher.updateScreen(screen, false);
	}

	public void onDrawBackgroundPost(Screen screen, PoseStack poseStack) {
		Minecraft minecraft = Minecraft.getInstance();
		boolean exclusionAreasChanged = guiScreenHelper.updateGuiExclusionAreas(screen);
		ingredientListOverlay.updateScreen(screen, exclusionAreasChanged);
		leftAreaDispatcher.updateScreen(screen, exclusionAreasChanged);

		drawnOnBackground = true;
		double mouseX = MouseUtil.getX();
		double mouseY = MouseUtil.getY();
		ingredientListOverlay.drawScreen(minecraft, poseStack, (int) mouseX, (int) mouseY, minecraft.getFrameTime());
		leftAreaDispatcher.drawScreen(minecraft, poseStack, (int) mouseX, (int) mouseY, minecraft.getFrameTime());
	}

	/**
	 * Draws above most ContainerScreen elements, but below the tooltips.
	 */
	public void onDrawForeground(AbstractContainerScreen<?> screen, PoseStack poseStack, int mouseX, int mouseY) {
		Minecraft minecraft = Minecraft.getInstance();
		ingredientListOverlay.drawOnForeground(minecraft, poseStack, screen, mouseX, mouseY);
	}

	public void onDrawScreenPost(Screen screen, PoseStack poseStack, int mouseX, int mouseY) {
		Minecraft minecraft = Minecraft.getInstance();

		ingredientListOverlay.updateScreen(screen, false);
		leftAreaDispatcher.updateScreen(screen, false);

		if (!drawnOnBackground) {
			if (screen instanceof AbstractContainerScreen) {
				String guiName = screen.getClass().getName();
				missingBackgroundLogger.log(Level.WARN, guiName, "GUI did not draw the dark background layer behind itself, this may result in display issues: {}", guiName);
			}
			ingredientListOverlay.drawScreen(minecraft, poseStack, mouseX, mouseY, minecraft.getFrameTime());
			leftAreaDispatcher.drawScreen(minecraft, poseStack, mouseX, mouseY, minecraft.getFrameTime());
		}
		drawnOnBackground = false;

		if (screen instanceof AbstractContainerScreen<?> guiContainer) {
			IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
			int guiLeft = screenHelper.getGuiLeft(guiContainer);
			int guiTop = screenHelper.getGuiTop(guiContainer);
			guiScreenHelper.getGuiClickableArea(guiContainer, mouseX - guiLeft, mouseY - guiTop)
				.map(IGuiClickableArea::getTooltipStrings)
				.ifPresent(tooltipStrings -> {
					if (tooltipStrings.isEmpty()) {
						tooltipStrings = List.of(new TranslatableComponent("jei.tooltip.show.recipes"));
					}
					TooltipRenderer.drawHoveringText(poseStack, tooltipStrings, mouseX, mouseY);
				});
		}

		ingredientListOverlay.drawTooltips(minecraft, poseStack, mouseX, mouseY);
		leftAreaDispatcher.drawTooltips(minecraft, poseStack, mouseX, mouseY);
	}

	public void onClientTick() {
		ingredientListOverlay.handleTick();
	}

	public boolean renderCompactPotionIndicators() {
		return ingredientListOverlay.isListDisplayed();
	}
}
