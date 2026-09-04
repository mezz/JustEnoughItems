package mezz.jei.gui.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.LimitedLogger;
import mezz.jei.common.util.RectDebugger;
import mezz.jei.gui.input.IGuiInputLayer;
import mezz.jei.gui.input.MouseUtil;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GuiEventHandler {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final LimitedLogger missingBackgroundLogger = new LimitedLogger(LOGGER, Duration.ofHours(1));
	private static final int MOUSE_OUTSIDE_SCREEN = -1;

	private final IngredientListOverlay ingredientListOverlay;
	private final IScreenHelper screenHelper;
	private final BookmarkOverlay bookmarkOverlay;
	private final List<IGuiInputLayer> inputLayers;
	private boolean drawnOnBackground = false;

	public GuiEventHandler(
		IScreenHelper screenHelper,
		BookmarkOverlay bookmarkOverlay,
		IngredientListOverlay ingredientListOverlay,
		IGuiInputLayer... inputLayers
	) {
		this.screenHelper = screenHelper;
		this.bookmarkOverlay = bookmarkOverlay;
		this.ingredientListOverlay = ingredientListOverlay;
		this.inputLayers = List.of(inputLayers);
	}

	public void onGuiInit(Screen screen) {
		Set<ImmutableRect2i> guiExclusionAreas = screenHelper.getGuiExclusionAreas(screen)
			.map(ImmutableRect2i::new)
			.collect(Collectors.toUnmodifiableSet());
		ingredientListOverlay.getScreenPropertiesUpdater()
			.updateScreen(screen)
			.updateExclusionAreas(guiExclusionAreas)
			.update();
		bookmarkOverlay.getScreenPropertiesUpdater()
			.updateScreen(screen)
			.updateExclusionAreas(guiExclusionAreas)
			.update();
	}

	public void onGuiOpen(Screen screen) {
		ingredientListOverlay.getScreenPropertiesUpdater()
			.updateScreen(screen)
			.update();
		bookmarkOverlay.getScreenPropertiesUpdater()
			.updateScreen(screen)
			.update();
	}

	/**
	 * Updates the pinned tooltip before the screen can render its own tooltip.
	 */
	public void updateForScreenRender(Screen screen, int mouseX, int mouseY) {
		updateOverlayProperties(screen);
		bookmarkOverlay.getPreviewTooltipController().update(mouseX, mouseY);
	}

	public void onDrawBackgroundPost(Screen screen, PoseStack poseStack) {
		Minecraft minecraft = Minecraft.getInstance();
		Set<ImmutableRect2i> guiExclusionAreas = screenHelper.getGuiExclusionAreas(screen)
			.map(ImmutableRect2i::new)
			.collect(Collectors.toUnmodifiableSet());

		ingredientListOverlay.getScreenPropertiesUpdater()
			.updateScreen(screen)
			.updateExclusionAreas(guiExclusionAreas)
			.update();
		bookmarkOverlay.getScreenPropertiesUpdater()
			.updateScreen(screen)
			.updateExclusionAreas(guiExclusionAreas)
			.update();

		drawnOnBackground = true;
		double mouseX = MouseUtil.getX();
		double mouseY = MouseUtil.getY();
		boolean mouseOverInputLayer = this.inputLayers.stream()
			.anyMatch(inputLayer -> inputLayer.isMouseOver(mouseX, mouseY));
		int overlayMouseX = mouseOverInputLayer ? MOUSE_OUTSIDE_SCREEN : (int) mouseX;
		int overlayMouseY = mouseOverInputLayer ? MOUSE_OUTSIDE_SCREEN : (int) mouseY;
		ingredientListOverlay.drawScreen(minecraft, poseStack, overlayMouseX, overlayMouseY, minecraft.getFrameTime());
		bookmarkOverlay.drawScreen(minecraft, poseStack, overlayMouseX, overlayMouseY, minecraft.getFrameTime());
	}

	/**
	 * Draws above most ContainerScreen elements, but below the tooltips.
	 */
	public void onDrawForeground(AbstractContainerScreen<?> screen, PoseStack poseStack, int mouseX, int mouseY) {
		poseStack.pushPose();
		{
			IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
			poseStack.translate(-screenHelper.getGuiLeft(screen), -screenHelper.getGuiTop(screen), 0);
			onDrawForegroundAtIdentity(poseStack, mouseX, mouseY);
		}
		poseStack.popPose();
	}

	public void onDrawForegroundAtIdentity(PoseStack poseStack, int mouseX, int mouseY) {
		bookmarkOverlay.drawOnForeground(poseStack, mouseX, mouseY);
		ingredientListOverlay.drawOnForeground(poseStack, mouseX, mouseY);
	}

	public void onDrawScreenPost(Screen screen, PoseStack poseStack, int mouseX, int mouseY) {
		Minecraft minecraft = Minecraft.getInstance();

		updateOverlayProperties(screen);

		boolean mouseOverInputLayer = this.inputLayers.stream()
			.anyMatch(inputLayer -> inputLayer.isMouseOver(mouseX, mouseY));
		int overlayMouseX = mouseOverInputLayer ? MOUSE_OUTSIDE_SCREEN : mouseX;
		int overlayMouseY = mouseOverInputLayer ? MOUSE_OUTSIDE_SCREEN : mouseY;

		if (!drawnOnBackground) {
			if (screen instanceof AbstractContainerScreen) {
				String guiName = screen.getClass().getName();
				missingBackgroundLogger.log(Level.WARN, guiName, "GUI did not draw the dark background layer behind itself, this may result in display issues: {}", guiName);
			}
			ingredientListOverlay.drawScreen(minecraft, poseStack, overlayMouseX, overlayMouseY, minecraft.getFrameTime());
			bookmarkOverlay.drawScreen(minecraft, poseStack, overlayMouseX, overlayMouseY, minecraft.getFrameTime());
		}
		drawnOnBackground = false;

		if (!mouseOverInputLayer && screen instanceof AbstractContainerScreen<?> guiContainer) {
			IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
			int guiLeft = screenHelper.getGuiLeft(guiContainer);
			int guiTop = screenHelper.getGuiTop(guiContainer);
			this.screenHelper.getGuiClickableArea(guiContainer, mouseX - guiLeft, mouseY - guiTop)
				.filter(IGuiClickableArea::isTooltipEnabled)
				.findFirst()
				.ifPresent(area -> {
					JeiTooltip tooltip = new JeiTooltip();
					area.getTooltip(tooltip);
					if (tooltip.isEmpty()) {
						tooltip.add(Component.translatable("jei.tooltip.show.recipes"));
					}
					tooltip.draw(poseStack, mouseX, mouseY);
				});
		}

		if (!mouseOverInputLayer) {
			ingredientListOverlay.drawTooltips(minecraft, poseStack, mouseX, mouseY);
			bookmarkOverlay.drawTooltips(minecraft, poseStack, mouseX, mouseY);
		}

		for (int i = this.inputLayers.size() - 1; i >= 0; i--) {
			this.inputLayers.get(i).draw(poseStack, mouseX, mouseY);
		}

		if (DebugConfig.isDebugGuisEnabled()) {
			drawDebugInfoForScreen(screen, poseStack);
		}
	}

	private void updateOverlayProperties(Screen screen) {
		Set<ImmutableRect2i> guiExclusionAreas = screenHelper.getGuiExclusionAreas(screen)
			.map(ImmutableRect2i::new)
			.collect(Collectors.toUnmodifiableSet());
		ingredientListOverlay.getScreenPropertiesUpdater()
			.updateScreen(screen)
			.updateExclusionAreas(guiExclusionAreas)
			.update();
		bookmarkOverlay.getScreenPropertiesUpdater()
			.updateScreen(screen)
			.updateExclusionAreas(guiExclusionAreas)
			.update();
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
				Set<Rect2i> guiExclusionAreas = screenHelper.getGuiExclusionAreas(screen)
					.collect(Collectors.toUnmodifiableSet());

				RenderSystem.disableDepthTest();

				// draw the gui exclusion areas
				for (Rect2i area : guiExclusionAreas) {
					GuiComponent.fill(
						poseStack,
						area.getX(),
						area.getY(),
						area.getX() + area.getWidth(),
						area.getY() + area.getHeight(),
						JeiGuiColors.getColor(GuiColor.DEBUG_GUI_EXCLUSION_AREA)
					);
				}

				// draw the gui area
				GuiComponent.fill(
					poseStack,
					guiProperties.getGuiLeft(),
					guiProperties.getGuiTop(),
					guiProperties.getGuiLeft() + guiProperties.getGuiXSize(),
					guiProperties.getGuiTop() + guiProperties.getGuiYSize(),
					JeiGuiColors.getColor(GuiColor.DEBUG_GUI_AREA)
				);

				RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			});
	}
}
