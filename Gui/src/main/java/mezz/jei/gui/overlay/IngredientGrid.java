package mezz.jei.gui.overlay;

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
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.handlers.DeleteItemInputHandler;
import mezz.jei.gui.overlay.elements.IElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * An ingredient grid displays a rectangular area of clickable recipe ingredients.
 * It does not draw a background or have external padding, those are left up to a higher-level element.
 */
public class IngredientGrid implements IRecipeFocusSource, IIngredientGrid {
	private final IIngredientManager ingredientManager;
	private final IIngredientGridConfig gridConfig;
	private final boolean searchable;
	private final IngredientListRenderer ingredientListRenderer;
	private final DeleteItemInputHandler deleteItemHandler;
	private final IngredientGridTooltipHelper tooltipHelper;
	private Set<ImmutableRect2i> guiExclusionAreas = Set.of();
	private ImmutableRect2i area = ImmutableRect2i.EMPTY;

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

	public int size() {
		return this.ingredientListRenderer.size();
	}

	public void updateBounds(ImmutableRect2i availableArea, Set<ImmutableRect2i> guiExclusionAreas, @Nullable ImmutablePoint2i mouseExclusionPoint) {
		this.ingredientListRenderer.clear();

		this.area = IngredientGridLayout.calculateBounds(this.gridConfig, availableArea);
		this.guiExclusionAreas = guiExclusionAreas;

		List<IngredientGridLayout.SlotLayout> slotLayouts = IngredientGridLayout.calculateSlots(
			this.area,
			guiExclusionAreas,
			mouseExclusionPoint
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

	public void draw(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		Optional<IngredientListSlot> highlightedSlot = getHighlightedSlot(minecraft, mouseX, mouseY);
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		highlightedSlot.ifPresent(s -> drawHighlight(guiGraphics, s.getArea(), screenHelper.getSlotHighlightBackSprite()));

		ingredientListRenderer.render(guiGraphics);

		highlightedSlot.ifPresent(s -> drawHighlight(guiGraphics, s.getArea(), screenHelper.getSlotHighlightFrontSprite()));
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

	private static void drawHighlight(GuiGraphicsExtractor guiGraphics, ImmutableRect2i area, Identifier sprite) {
		guiGraphics.blitSprite(
			RenderPipelines.GUI_TEXTURED,
			sprite,
			area.getX() - 4,
			area.getY() - 4,
			area.getWidth() + 8,
			area.getHeight() + 8
		);
	}

	public void drawTooltips(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			if (this.deleteItemHandler.shouldDeleteItemOnClick(minecraft, mouseX, mouseY)) {
				this.deleteItemHandler.drawTooltips(guiGraphics, mouseX, mouseY);
			} else {
				ingredientListRenderer.getSlots()
					.filter(s -> s.isMouseOver(mouseX, mouseY))
					.map(IngredientListSlot::getOptionalElement)
					.flatMap(Optional::stream)
					.findFirst()
					.ifPresent(element -> drawTooltip(guiGraphics, mouseX, mouseY, element));
			}
		}
	}

	private <T> void drawTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, IElement<T> element) {
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

	public Stream<IElement<?>> getVisibleElements() {
		return this.ingredientListRenderer.getSlots()
			.map(IngredientListSlot::getOptionalElement)
			.flatMap(Optional::stream);
	}

	public <T> Stream<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
		return getVisibleElements()
			.map(IElement::getTypedIngredient)
			.map(i -> i.getIngredient(ingredientType))
			.flatMap(Optional::stream);
	}

	public void set(int firstItemIndex, List<IElement<?>> ingredientList) {
		this.ingredientListRenderer.set(firstItemIndex, ingredientList);
	}

	public boolean hasRoom() {
		return !this.area.isEmpty();
	}
}
