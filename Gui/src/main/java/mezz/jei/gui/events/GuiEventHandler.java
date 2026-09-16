package mezz.jei.gui.events;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.MouseUtil;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.RectDebugger;
import mezz.jei.common.input.IGuiInputLayer;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GuiEventHandler {
	private final IngredientListOverlay ingredientListOverlay;
	private final IScreenHelper screenHelper;
	private final BookmarkOverlay bookmarkOverlay;
	private final List<IGuiInputLayer> inputLayers;
	private boolean drawnOnBackground;

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

	public void onClientTick() {
		ingredientListOverlay.tick();
		bookmarkOverlay.tick();
	}

	/**
	 * Updates input layers before the screen can render its tooltip.
	 */
	public void updateForScreenRender(Screen screen, int mouseX, int mouseY) {
		IGuiProperties guiProperties = screenHelper.getGuiProperties(screen).orElse(null);
		updateOverlayProperties(screen, guiProperties);
		this.inputLayers.forEach(inputLayer -> inputLayer.update(mouseX, mouseY));
	}

	/**
	 * Draws the JEI overlays before non-container screen contents are drawn.
	 */
	public void drawForScreenBackground(Screen screen, GuiGraphics guiGraphics) {
		@Nullable
		IGuiProperties guiProperties = screenHelper.getGuiProperties(screen).orElse(null);
		updateOverlayProperties(screen, guiProperties);
		drawOverlayBackgrounds(guiGraphics);
		if (!(screen instanceof AbstractContainerScreen<?>)) {
			int mouseX = (int) MouseUtil.getX();
			int mouseY = (int) MouseUtil.getY();
			drawOverlayContents(guiGraphics, mouseX, mouseY);
		}
		drawnOnBackground = true;
	}

	/**
	 * Draws container overlay contents after the screen contents, then draws JEI tooltips and input layers.
	 */
	public void drawForScreenForeground(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
		@Nullable
		IGuiProperties guiProperties = screenHelper.getGuiProperties(screen).orElse(null);
		if (screen instanceof AbstractContainerScreen<?>) {
			drawOnScreenForeground(guiGraphics, mouseX, mouseY);
			drawOverlayContents(guiGraphics, mouseX, mouseY);
		} else if (!drawnOnBackground) {
			updateOverlayProperties(screen, guiProperties);
			drawOverlayBackgrounds(guiGraphics);
			drawOverlayContents(guiGraphics, mouseX, mouseY);
		}
		drawnOnBackground = false;
		drawPostForeground(screen, guiProperties, guiGraphics, mouseX, mouseY);
	}

	private void updateOverlayProperties(Screen screen, @Nullable IGuiProperties guiProperties) {
		Set<ImmutableRect2i> guiExclusionAreas = screenHelper.getGuiExclusionAreas(screen)
			.map(ImmutableRect2i::new)
			.collect(Collectors.toUnmodifiableSet());
		ingredientListOverlay.getScreenPropertiesUpdater()
			.updateGuiProperties(guiProperties)
			.updateExclusionAreas(guiExclusionAreas)
			.update();
		bookmarkOverlay.getScreenPropertiesUpdater()
			.updateGuiProperties(guiProperties)
			.updateExclusionAreas(guiExclusionAreas)
			.update();
	}

	private void drawOverlayBackgrounds(GuiGraphics guiGraphics) {
		ingredientListOverlay.drawBackground(guiGraphics);
		bookmarkOverlay.drawBackground(guiGraphics);
	}

	private void drawOnScreenForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		bookmarkOverlay.drawOnForeground(guiGraphics, mouseX, mouseY);
		ingredientListOverlay.drawOnForeground(guiGraphics, mouseX, mouseY);
	}

	private void drawOverlayContents(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		Minecraft minecraft = Minecraft.getInstance();

		DeltaTracker deltaTracker = minecraft.getTimer();
		float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(false);

		ingredientListOverlay.drawForeground(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
		bookmarkOverlay.drawForeground(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
	}

	private void drawPostForeground(Screen screen, @Nullable IGuiProperties guiProperties, GuiGraphics guiGraphics, int mouseX, int mouseY) {
		Minecraft minecraft = Minecraft.getInstance();
		boolean mouseOverInputLayer = this.inputLayers.stream()
			.anyMatch(inputLayer -> inputLayer.isMouseOver(mouseX, mouseY));

		if (!mouseOverInputLayer && guiProperties != null && screen instanceof AbstractContainerScreen<?> guiContainer) {
			int guiLeft = guiProperties.guiLeft();
			int guiTop = guiProperties.guiTop();
			this.screenHelper.getGuiClickableArea(guiContainer, mouseX - guiLeft, mouseY - guiTop)
				.filter(IGuiClickableArea::isTooltipEnabled)
				.findFirst()
				.ifPresent(area -> {
					JeiTooltip tooltip = new JeiTooltip();
					area.getTooltip(tooltip);
					if (tooltip.isEmpty()) {
						tooltip.add(Component.translatable("jei.tooltip.show.recipes"));
					}
					tooltip.draw(guiGraphics, mouseX, mouseY);
				});
		}

		if (!mouseOverInputLayer) {
			ingredientListOverlay.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
			bookmarkOverlay.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
		}

		for (int i = this.inputLayers.size() - 1; i >= 0; i--) {
			this.inputLayers.get(i).draw(guiGraphics, mouseX, mouseY);
		}

		if (DebugConfig.isDebugGuisEnabled()) {
			drawDebugInfoForScreen(screen, guiProperties, guiGraphics);
		}
	}

	public boolean renderCompactPotionIndicators() {
		return ingredientListOverlay.isListDisplayed();
	}

	private void drawDebugInfoForScreen(Screen screen, @Nullable IGuiProperties guiProperties, GuiGraphics guiGraphics) {
		RectDebugger.INSTANCE.draw(guiGraphics);

		if (guiProperties != null) {
			Set<Rect2i> guiExclusionAreas = screenHelper.getGuiExclusionAreas(screen)
				.collect(Collectors.toUnmodifiableSet());

			RenderSystem.disableDepthTest();

			// draw the gui exclusion areas
			for (Rect2i area : guiExclusionAreas) {
				guiGraphics.fill(
					RenderType.gui(),
					area.getX(),
					area.getY(),
					area.getX() + area.getWidth(),
					area.getY() + area.getHeight(),
					JeiGuiColors.getColor(GuiColor.DEBUG_GUI_EXCLUSION_AREA)
				);
			}

			// draw the gui area
			guiGraphics.fill(
				RenderType.gui(),
				guiProperties.guiLeft(),
				guiProperties.guiTop(),
				guiProperties.guiRight(),
				guiProperties.guiBottom(),
				JeiGuiColors.getColor(GuiColor.DEBUG_GUI_AREA)
			);

			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}
	}
}
