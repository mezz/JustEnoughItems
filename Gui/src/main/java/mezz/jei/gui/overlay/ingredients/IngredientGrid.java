package mezz.jei.gui.overlay.ingredients;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.common.config.IWorldConfig;
import mezz.jei.gui.ingredients.GuiIngredientProperties;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.handlers.DeleteItemInputHandler;
import mezz.jei.gui.overlay.elements.IElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * An ingredient grid displays a rectangular area of clickable recipe ingredients.
 * The containing control draws the background and adds padding around the grid.
 */
public class IngredientGrid implements IIngredientGrid {
	public static final int INGREDIENT_WIDTH = IngredientGridLayout.INGREDIENT_WIDTH;
	public static final int INGREDIENT_HEIGHT = IngredientGridLayout.INGREDIENT_HEIGHT;

	private final IIngredientManager ingredientManager;
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
		IIngredientFilterConfig ingredientFilterConfig,
		IClientConfig clientConfig,
		IWorldConfig worldConfig,
		IConnectionToServer serverConnection,
		IInternalKeyMappings keyBindings,
		IColorHelper colorHelper,
		boolean searchable
	) {
		this.ingredientManager = ingredientManager;
		this.searchable = searchable;
		this.ingredientListRenderer = new IngredientListRenderer(ingredientManager, searchable);
		this.tooltipHelper = new IngredientGridTooltipHelper(ingredientManager, ingredientFilterConfig, worldConfig, keyBindings, colorHelper);
		this.deleteItemHandler = new DeleteItemInputHandler(this, worldConfig, clientConfig, serverConnection, ingredientManager);
	}

	public IInputTarget getInputHandler() {
		return deleteItemHandler;
	}

	@Override
	public int size() {
		return visibleSlotCount;
	}

	@Override
	public int getColumnCount() {
		return this.area.width() / IngredientGridLayout.INGREDIENT_WIDTH;
	}

	@Override
	public int getRowCount() {
		return this.area.height() / IngredientGridLayout.INGREDIENT_HEIGHT;
	}

	@Override
	public int getVisibleHeight() {
		return this.area.height();
	}

	/** Applies the grid bounds already calculated by the navigation layout. */
	public void updateBounds(
		ImmutableRect2i gridArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint
	) {
		this.area = gridArea;
		this.guiExclusionAreas = guiExclusionAreas;
		this.mouseExclusionPoint = mouseExclusionPoint;
		this.visibleSlotCount = IngredientGridLayout.calculateAvailableSlotCount(
			this.area,
			this.guiExclusionAreas
		);
		updateSlots(0);
	}

	private void updateSlots(int smoothScrollRowPixelOffset) {
		this.smoothScrollRowPixelOffset = smoothScrollRowPixelOffset;
		this.ingredientListRenderer.clear();

		List<IngredientGridLayout.SlotLayout> slotLayouts = IngredientGridLayout.calculateSlots(
			this.area,
			this.guiExclusionAreas,
			this.mouseExclusionPoint,
			smoothScrollRowPixelOffset
		);
		for (IngredientGridLayout.SlotLayout slotLayout : slotLayouts) {
			ImmutableRect2i slotArea = slotLayout.area();
			IngredientListSlot ingredientListSlot = new IngredientListSlot(
				slotArea.x(),
				slotArea.y(),
				slotArea.width(),
				slotArea.height(),
				IngredientGridLayout.INGREDIENT_PADDING
			);
			ingredientListSlot.setBlocked(slotLayout.blocked());
			this.ingredientListRenderer.add(ingredientListSlot);
		}
	}

	public ImmutableRect2i getArea() {
		return area;
	}

	public void draw(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY, boolean drawSlotBackgrounds) {
		if (this.smoothScrollRowPixelOffset > 0 || this.area.height() % IngredientGridLayout.INGREDIENT_HEIGHT != 0) {
			mezz.jei.common.gui.ScissorHelper.enableScissor(poseStack,
				this.area.x(),
				this.area.y(),
				this.area.x() + this.area.width(),
				this.area.y() + this.area.height()
			);
			try {
				drawContents(minecraft, poseStack, mouseX, mouseY, drawSlotBackgrounds);
			} finally {
				mezz.jei.common.gui.ScissorHelper.disableScissor();
			}
		} else {
			drawContents(minecraft, poseStack, mouseX, mouseY, drawSlotBackgrounds);
		}
	}

	private void drawContents(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY, boolean drawSlotBackgrounds) {
		if (drawSlotBackgrounds) {
			IDrawableStatic slotBackground = Internal.getTextures().getSlot();
			this.ingredientListRenderer.getSlots()
				.map(IngredientListSlot::getArea)
				.forEach(area -> slotBackground.draw(poseStack, area.x(), area.y()));
		}
		RenderSystem.disableBlend();

		Optional<IngredientListSlot> highlightedSlot = getHighlightedSlot(minecraft, mouseX, mouseY);

		this.ingredientListRenderer.render(poseStack);

		highlightedSlot.ifPresent(s -> drawHighlight(poseStack, s.getArea()));
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
	 * Matches the highlight code in {@link AbstractContainerScreen#renderSlotHighlight(PoseStack, int, int, int)}
	 * but with a custom area width and height
	 */
	public static void drawHighlight(PoseStack poseStack, ImmutableRect2i area) {
		RenderSystem.disableDepthTest();
		RenderSystem.colorMask(true, true, true, false);
		GuiComponent.fill(poseStack, area.getX(), area.getY(), area.getX() + area.getWidth(), area.getY() + area.getHeight(), 0x80FFFFFF);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableDepthTest();
	}

	public void drawTooltips(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			if (this.deleteItemHandler.shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
				this.deleteItemHandler.drawTooltips(poseStack, mouseX, mouseY);
			} else {
				ingredientListRenderer.getSlots()
					.filter(s -> s.isMouseOver(mouseX, mouseY))
					.map(IngredientListSlot::getOptionalElement)
					.flatMap(Optional::stream)
					.findFirst()
					.ifPresent(element -> drawTooltip(poseStack, mouseX, mouseY, element));
			}
		}
	}

	private <T> void drawTooltip(PoseStack poseStack, int mouseX, int mouseY, IElement<T> element) {
		ITypedIngredient<T> typedIngredient = element.getTypedIngredient();
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);

		JeiTooltip tooltip = new JeiTooltip();
		element.getTooltip(tooltip, tooltipHelper, ingredientRenderer, ingredientHelper);
		if (searchable) {
			addCreativeTabs(tooltip, typedIngredient, ingredientHelper);
		}
		tooltip.draw(poseStack, mouseX, mouseY, typedIngredient, ingredientRenderer, ingredientManager);
	}
	private static <T> void addCreativeTabs(
		ITooltipBuilder tooltipBuilder,
		ITypedIngredient<T> typedIngredient,
		IIngredientHelper<T> ingredientHelper
	) {
		IClientConfig clientConfig = Internal.getClientConfigs().getClientConfig();
		if (!clientConfig.showCreativeTabNamesEnabled().get()) {
			return;
		}

		T ingredient = typedIngredient.getIngredient();
		for (String creativeTabName : ingredientHelper.getCreativeTabNames(ingredient)) {
			tooltipBuilder.add(Component.literal(creativeTabName).withStyle(ChatFormatting.BLUE));
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
		if (!isMouseOver(mouseX, mouseY)) {
			return Stream.empty();
		}
		return ingredientListRenderer.getSlots()
			.filter(s -> s.isMouseOver(mouseX, mouseY))
			.map(IngredientListSlot::getClickableIngredient)
			.flatMap(Optional::stream);
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		if (!isMouseOver(mouseX, mouseY)) {
			return Stream.empty();
		}
		return ingredientListRenderer.getSlots()
			.filter(s -> s.isMouseOver(mouseX, mouseY))
			.map(IngredientListSlot::getDraggableIngredient)
			.flatMap(Optional::stream);
	}

	public Stream<IngredientListSlot> getSlots() {
		return ingredientListRenderer.getSlots();
	}

	public List<IngredientListSlot> getAllSlots() {
		return ingredientListRenderer.getAllSlots();
	}

	@Override
	public Stream<IElement<?>> getVisibleElements() {
		return this.ingredientListRenderer.getSlots()
			.map(IngredientListSlot::getOptionalElement)
			.flatMap(Optional::stream);
	}

	@Override
	public void tick() {

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
