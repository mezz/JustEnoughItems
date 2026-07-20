package mezz.jei.gui.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.ingredients.GuiIngredientProperties;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.handlers.DeleteItemInputHandler;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.util.AlignmentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * An ingredient grid displays a rectangular area of clickable recipe ingredients.
 * It does not draw a background or have external padding, those are left up to a higher-level element.
 */
public class IngredientGrid implements IRecipeFocusSource, IIngredientGrid {
	private static final int INGREDIENT_PADDING = 1;
	public static final int INGREDIENT_WIDTH = GuiIngredientProperties.getWidth(INGREDIENT_PADDING);
	public static final int INGREDIENT_HEIGHT = GuiIngredientProperties.getHeight(INGREDIENT_PADDING);

	private final IIngredientManager ingredientManager;
	private final IIngredientGridConfig gridConfig;
	private final boolean searchable;
	private final IngredientListRenderer ingredientListRenderer;
	private final DeleteItemInputHandler deleteItemHandler;
	private final IngredientGridTooltipHelper tooltipHelper;
	private Set<ImmutableRect2i> guiExclusionAreas = Set.of();
	@Nullable
	private ImmutablePoint2i mouseExclusionPoint;
	private ImmutableRect2i area = ImmutableRect2i.EMPTY;
	private int visibleSlotCount = 0;
	private int smoothScrollRowPixelOffset = 0;

	public IngredientGrid(
		IIngredientManager ingredientManager,
		IIngredientGridConfig gridConfig,
		IIngredientFilterConfig ingredientFilterConfig,
		IClientConfig clientConfig,
		IClientToggleState toggleState,
		IConnectionToServer serverConnection,
		IInternalKeyMappings keyBindings,
		IColorHelper colorHelper,
		boolean searchable
	) {
		this.ingredientManager = ingredientManager;
		this.gridConfig = gridConfig;
		this.searchable = searchable;
		this.ingredientListRenderer = new IngredientListRenderer(ingredientManager, searchable);
		this.tooltipHelper = new IngredientGridTooltipHelper(ingredientManager, ingredientFilterConfig, toggleState, keyBindings, colorHelper);
		this.deleteItemHandler = new DeleteItemInputHandler(this, toggleState, clientConfig, serverConnection, ingredientManager);
	}

	public IUserInputHandler getInputHandler() {
		return deleteItemHandler;
	}

	@Override
	public int size() {
		return visibleSlotCount;
	}

	@Override
	public int getColumnCount() {
		return this.area.width() / INGREDIENT_WIDTH;
	}

	@Override
	public int getRowCount() {
		return this.area.height() / INGREDIENT_HEIGHT;
	}

	public void updateBounds(ImmutableRect2i availableArea, Set<ImmutableRect2i> guiExclusionAreas, @Nullable ImmutablePoint2i mouseExclusionPoint) {
		this.area = calculateBounds(this.gridConfig, availableArea);
		this.guiExclusionAreas = guiExclusionAreas;
		this.mouseExclusionPoint = mouseExclusionPoint;
		this.visibleSlotCount = calculateAvailableSlotCount(
			this.area,
			this.guiExclusionAreas,
			this.mouseExclusionPoint
		);
		updateSlots(0);
	}

	private void updateSlots(int smoothScrollRowPixelOffset) {
		this.smoothScrollRowPixelOffset = smoothScrollRowPixelOffset;
		this.ingredientListRenderer.clear();

		List<SlotLayout> slotLayouts = calculateSlots(
			this.area,
			this.guiExclusionAreas,
			this.mouseExclusionPoint,
			smoothScrollRowPixelOffset
		);
		for (SlotLayout slotLayout : slotLayouts) {
			ImmutableRect2i slotArea = slotLayout.area();
			IngredientListSlot ingredientListSlot = new IngredientListSlot(
				slotArea.x(),
				slotArea.y(),
				slotArea.width(),
				slotArea.height(),
				INGREDIENT_PADDING
			);
			ingredientListSlot.setBlocked(slotLayout.blocked());
			this.ingredientListRenderer.add(ingredientListSlot);
		}
	}

	public static ImmutableSize2i calculateSize(IIngredientGridConfig config, ImmutableRect2i availableArea) {
		final int columns = Math.min(availableArea.getWidth() / INGREDIENT_WIDTH, config.getMaxColumns());
		final int rows = Math.min(availableArea.getHeight() / INGREDIENT_HEIGHT, config.getMaxRows());
		if (rows < config.getMinRows() || columns < config.getMinColumns()) {
			return ImmutableSize2i.EMPTY;
		}
		return new ImmutableSize2i(
			columns * INGREDIENT_WIDTH,
			rows * INGREDIENT_HEIGHT
		);
	}

	public static ImmutableRect2i calculateBounds(IIngredientGridConfig config, ImmutableRect2i availableArea) {
		ImmutableSize2i size = calculateSize(config, availableArea);
		return AlignmentUtil.align(size, availableArea, config.getHorizontalAlignment(), config.getVerticalAlignment());
	}

	public record SlotInfo(int total, int blocked) {
		public int available() {
			return total - blocked;
		}

		public float percentBlocked() {
			return blocked / (float) total;
		}
	}

	public static SlotInfo calculateBlockedSlotPercentage(IIngredientGridConfig config, ImmutableRect2i availableArea, Set<ImmutableRect2i> exclusionAreas) {
		ImmutableRect2i area = calculateBounds(config, availableArea);
		return calculateSlotInfo(area, exclusionAreas, null);
	}

	public static int calculateAvailableSlotCount(ImmutableRect2i area, Set<ImmutableRect2i> exclusionAreas, @Nullable ImmutablePoint2i mouseExclusionPoint) {
		return calculateSlotInfo(area, exclusionAreas, mouseExclusionPoint).available();
	}

	public static SlotInfo calculateSlotInfo(ImmutableRect2i area, Set<ImmutableRect2i> exclusionAreas, @Nullable ImmutablePoint2i mouseExclusionPoint) {
		int total = 0;
		int blocked = 0;
		List<SlotLayout> slotLayouts = calculateSlots(area, exclusionAreas, mouseExclusionPoint, 0);
		for (SlotLayout slotLayout : slotLayouts) {
			if (slotLayout.blocked()) {
				blocked++;
			}
			total++;
		}
		return new SlotInfo(total, blocked);
	}

	public static List<SlotLayout> calculateSlots(
		ImmutableRect2i area,
		Set<ImmutableRect2i> exclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint,
		int smoothScrollRowPixelOffset
	) {
		List<SlotLayout> slotLayouts = new ArrayList<>();
		int rowPixelOffset = clamp(smoothScrollRowPixelOffset, 0, INGREDIENT_HEIGHT - 1);
		for (int y = area.getY() - rowPixelOffset; y < area.getY() + area.getHeight(); y += INGREDIENT_HEIGHT) {
			for (int x = area.getX(); x < area.getX() + area.getWidth(); x += INGREDIENT_WIDTH) {
				ImmutableRect2i slotArea = new ImmutableRect2i(x, y, INGREDIENT_WIDTH, INGREDIENT_HEIGHT);
				slotLayouts.add(new SlotLayout(
					slotArea,
					isSlotBlocked(slotArea, exclusionAreas, mouseExclusionPoint)
				));
			}
		}
		return slotLayouts;
	}

	private static boolean isSlotBlocked(
		ImmutableRect2i stackArea,
		Set<ImmutableRect2i> exclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint
	) {
		return MathUtil.intersects(exclusionAreas, stackArea.expandBy(2)) ||
			(mouseExclusionPoint != null && stackArea.contains(mouseExclusionPoint));
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(value, max));
	}

	public record SlotLayout(ImmutableRect2i area, boolean blocked) {

	}

	public ImmutableRect2i getArea() {
		return area;
	}

	public void draw(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (this.smoothScrollRowPixelOffset > 0) {
			guiGraphics.enableScissor(
				this.area.x(),
				this.area.y(),
				this.area.x() + this.area.width(),
				this.area.y() + this.area.height()
			);
			try {
				drawContents(minecraft, guiGraphics, mouseX, mouseY);
			} finally {
				guiGraphics.disableScissor();
			}
		} else {
			drawContents(minecraft, guiGraphics, mouseX, mouseY);
		}
	}

	private void drawContents(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY) {
		RenderSystem.disableBlend();

		Optional<IngredientListSlot> highlightedSlot = getHighlightedSlot(minecraft, mouseX, mouseY);

		ingredientListRenderer.render(guiGraphics);

		highlightedSlot.ifPresent(s -> drawHighlight(guiGraphics, s.getArea()));
	}

	private Optional<IngredientListSlot> getHighlightedSlot(Minecraft minecraft, int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			if (!this.deleteItemHandler.shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
				return ingredientListRenderer.getSlots()
					.filter(s -> s.getArea().contains(mouseX, mouseY))
					.filter(s -> s.getOptionalElement().isPresent())
					.findFirst();
			}
		}
		return Optional.empty();
	}

	/**
	 * Matches the highlight code in {@link AbstractContainerScreen#renderSlotHighlight(GuiGraphics, int, int, int)}
	 * but with a custom area width and height
	 */
	public static void drawHighlight(GuiGraphics guiGraphics, ImmutableRect2i area) {
		guiGraphics.fillGradient(
			RenderType.guiOverlay(),
			area.getX(),
			area.getY(),
			area.getX() + area.getWidth(),
			area.getY() + area.getHeight(),
			0x80FFFFFF,
			0x80FFFFFF,
			0
		);
	}

	public void drawTooltips(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			if (this.deleteItemHandler.shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
				this.deleteItemHandler.drawTooltips(guiGraphics, mouseX, mouseY);
			} else {
				ingredientListRenderer.getSlots()
					.filter(s -> s.isMouseOver(mouseX, mouseY))
					.map(IngredientListSlot::getOptionalElement)
					.flatMap(Optional::stream)
					.findFirst()
					.ifPresent(element -> {
						drawTooltip(guiGraphics, mouseX, mouseY, element);
					});
			}
		}
	}

	private <T> void drawTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, IElement<T> element) {
		ITypedIngredient<T> typedIngredient = element.getTypedIngredient();
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);

		JeiTooltip tooltip = new JeiTooltip();
		element.getTooltip(tooltip, tooltipHelper, ingredientRenderer, ingredientHelper);
		if (searchable) {
			addCreativeTabs(tooltip, typedIngredient);
		}
		tooltip.draw(guiGraphics, mouseX, mouseY, typedIngredient, ingredientRenderer, ingredientManager);
	}

	private <T> void addCreativeTabs(ITooltipBuilder tooltipBuilder, ITypedIngredient<T> typedIngredient) {
		IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
		if (!clientConfig.isShowCreativeTabNamesEnabled()) {
			return;
		}

		ItemStack itemStack = typedIngredient.getItemStack().orElse(ItemStack.EMPTY);
		if (itemStack.isEmpty()) {
			return;
		}

		for (CreativeModeTab itemGroup : CreativeModeTabs.allTabs()) {
			if (!itemGroup.shouldDisplay() || itemGroup.getType() != CreativeModeTab.Type.CATEGORY) {
				continue;
			}
			if (itemGroup.contains(itemStack)) {
				Component displayName = itemGroup.getDisplayName();
				tooltipBuilder.add(displayName.copy().withStyle(ChatFormatting.BLUE));
			}
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return area.contains(mouseX, mouseY) &&
			guiExclusionAreas.stream()
				.noneMatch(area -> area.contains(mouseX, mouseY));
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		return ingredientListRenderer.getSlots()
			.filter(s -> s.isMouseOver(mouseX, mouseY))
			.map(IngredientListSlot::getClickableIngredient)
			.flatMap(Optional::stream);
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		return ingredientListRenderer.getSlots()
			.filter(s -> s.isMouseOver(mouseX, mouseY))
			.map(IngredientListSlot::getDraggableIngredient)
			.flatMap(Optional::stream);
	}

	public Stream<IngredientListSlot> getSlots() {
		return ingredientListRenderer.getSlots();
	}

	@Override
	public Stream<IElement<?>> getVisibleElements() {
		return this.ingredientListRenderer.getSlots()
			.map(IngredientListSlot::getOptionalElement)
			.flatMap(Optional::stream);
	}

	@Override
	public void tick() {
		getVisibleElements()
			.forEach(IElement::tick);
	}

	public <T> Stream<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
		return getVisibleElements()
			.map(IElement::getTypedIngredient)
			.map(i -> i.getIngredient(ingredientType))
			.flatMap(Optional::stream);
	}

	@Override
	public void set(int firstItemIndex, List<IElement<?>> ingredientList) {
		set(firstItemIndex, 0, ingredientList);
	}

	@Override
	public void set(int firstItemIndex, int smoothScrollRowPixelOffset, List<IElement<?>> ingredientList) {
		if (this.smoothScrollRowPixelOffset != smoothScrollRowPixelOffset) {
			updateSlots(smoothScrollRowPixelOffset);
		}
		this.ingredientListRenderer.set(firstItemIndex, ingredientList);
	}

	public boolean hasRoom() {
		return !this.area.isEmpty() &&
			this.ingredientListRenderer.getSlots()
				.findAny()
				.isPresent();
	}
}
