package mezz.jei.common.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.api.runtime.util.IImmutableRect2i;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.gui.overlay.IngredientListOverlay;
import mezz.jei.common.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.common.input.MouseUtil;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.RectDebugger;
import mezz.jei.core.util.LimitedLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class GuiEventHandler {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final LimitedLogger missingBackgroundLogger = new LimitedLogger(LOGGER, Duration.ofHours(1));

	private final IngredientListOverlay ingredientListOverlay;
	private final IScreenHelper screenHelper;
	private final BookmarkOverlay bookmarkOverlay;
	private boolean drawnOnBackground = false;

	public GuiEventHandler(
		IScreenHelper screenHelper,
		BookmarkOverlay bookmarkOverlay,
		IngredientListOverlay ingredientListOverlay
	) {
		this.screenHelper = screenHelper;
		this.bookmarkOverlay = bookmarkOverlay;
		this.ingredientListOverlay = ingredientListOverlay;
	}

	public void onGuiInit(Screen screen) {
		ingredientListOverlay.updateScreen(screen, false);
		bookmarkOverlay.updateScreen(screen, false);
	}

	public void onGuiOpen(Screen screen) {
		ingredientListOverlay.updateScreen(screen, false);
		bookmarkOverlay.updateScreen(screen, false);
	}

	public void onDrawBackgroundPost(Screen screen, PoseStack poseStack) {
		Minecraft minecraft = Minecraft.getInstance();
		boolean exclusionAreasChanged = screenHelper.updateGuiExclusionAreas(screen);
		ingredientListOverlay.updateScreen(screen, exclusionAreasChanged);
		bookmarkOverlay.updateScreen(screen, exclusionAreasChanged);

		drawnOnBackground = true;
		double mouseX = MouseUtil.getX();
		double mouseY = MouseUtil.getY();
		ingredientListOverlay.drawScreen(minecraft, poseStack, (int) mouseX, (int) mouseY, minecraft.getFrameTime());
		bookmarkOverlay.drawScreen(minecraft, poseStack, (int) mouseX, (int) mouseY, minecraft.getFrameTime());
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
		bookmarkOverlay.updateScreen(screen, false);

		if (!drawnOnBackground) {
			if (screen instanceof AbstractContainerScreen) {
				String guiName = screen.getClass().getName();
				missingBackgroundLogger.log(Level.WARN, guiName, "GUI did not draw the dark background layer behind itself, this may result in display issues: {}", guiName);
			}
			ingredientListOverlay.drawScreen(minecraft, poseStack, mouseX, mouseY, minecraft.getFrameTime());
			bookmarkOverlay.drawScreen(minecraft, poseStack, mouseX, mouseY, minecraft.getFrameTime());
		}
		drawnOnBackground = false;

		if (screen instanceof AbstractContainerScreen<?> guiContainer) {
			IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
			int guiLeft = screenHelper.getGuiLeft(guiContainer);
			int guiTop = screenHelper.getGuiTop(guiContainer);
			this.screenHelper.getGuiClickableArea(guiContainer, mouseX - guiLeft, mouseY - guiTop)
				.filter(IGuiClickableArea::isTooltipEnabled)
				.map(IGuiClickableArea::getTooltipStrings)
				.ifPresent(tooltipStrings -> {
					if (tooltipStrings.isEmpty()) {
						tooltipStrings = List.of(Component.translatable("jei.tooltip.show.recipes"));
					}
					TooltipRenderer.drawHoveringText(poseStack, tooltipStrings, mouseX, mouseY);
				});
		}

		ingredientListOverlay.drawTooltips(minecraft, poseStack, mouseX, mouseY);
		bookmarkOverlay.drawTooltips(minecraft, poseStack, mouseX, mouseY);

		if (DebugConfig.isDebugModeEnabled()) {
			drawDebugInfoForScreen(screen, poseStack);
		}
	}

	public void onClientTick() {
		ingredientListOverlay.handleTick();
	}

	public boolean renderCompactPotionIndicators() {
		return ingredientListOverlay.isListDisplayed();
	}

	private void drawDebugInfoForScreen(Screen screen, PoseStack poseStack) {
		RectDebugger.INSTANCE.draw(poseStack);

		screenHelper.getGuiProperties(screen)
			.ifPresent(guiProperties -> {
				Set<? extends IImmutableRect2i> guiExclusionAreas = screenHelper.getGuiExclusionAreas();

				RenderSystem.disableDepthTest();

				// draw the gui exclusion areas
				for (IImmutableRect2i area : guiExclusionAreas) {
					GuiComponent.fill(
						poseStack,
						area.getX(),
						area.getY(),
						area.getX() + area.getWidth(),
						area.getY() + area.getHeight(),
						0x44FF0000
					);
				}

				// draw the gui area
				GuiComponent.fill(
					poseStack,
					guiProperties.getGuiLeft(),
					guiProperties.getGuiTop(),
					guiProperties.getGuiLeft() + guiProperties.getGuiXSize(),
					guiProperties.getGuiTop() + guiProperties.getGuiYSize(),
					0x22CCCC00
				);

				RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			});
	}
}
