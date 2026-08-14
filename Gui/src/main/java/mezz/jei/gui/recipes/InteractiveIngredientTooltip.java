package mezz.jei.gui.recipes;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.datafixers.util.Either;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.IIngredientGridTooltipComponent;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.RecipeSlotCandidatesTooltipComponent;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.gui.input.ClickableIngredientInternal;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import mezz.jei.gui.overlay.elements.TagIngredientElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

public class InteractiveIngredientTooltip {
	private static final int SCREEN_DIM_COLOR = 0x40000000;
	private static final int NAVIGATION_BACKGROUND_PADDING = 2;

	private final IRecipeManager recipeManager;
	private final IIngredientManager ingredientManager;
	private final BooleanSupplier isRecipeCyclingPaused;
	private final IScalableDrawable navigationBackground;

	private boolean visible;
	private @Nullable RecipeSlotUnderMouse sourceSlot;
	private @Nullable RecipeSlotTooltipPositioner positioner;
	private @Nullable InteractiveIngredientGridTooltipComponent candidateComponent;
	private int anchorX;
	private int anchorY;

	public InteractiveIngredientTooltip(
		IRecipeManager recipeManager,
		IIngredientManager ingredientManager,
		BooleanSupplier isRecipeCyclingPaused
	) {
		this.recipeManager = recipeManager;
		this.ingredientManager = ingredientManager;
		this.isRecipeCyclingPaused = isRecipeCyclingPaused;
		this.navigationBackground = Internal.getTextures().getButtonPressedHighlight();
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void hide() {
		this.visible = false;
		this.sourceSlot = null;
		this.positioner = null;
		this.candidateComponent = null;
	}

	public boolean show(RecipeSlotUnderMouse slotUnderMouse, double mouseX, double mouseY) {
		List<ITypedIngredient<?>> displayedIngredients = slotUnderMouse.slot()
			.getDisplayedIngredients()
			.toList();
		if (displayedIngredients.size() <= 1) {
			return false;
		}

		this.sourceSlot = slotUnderMouse;
		this.positioner = new RecipeSlotTooltipPositioner();
		this.candidateComponent = new InteractiveIngredientGridTooltipComponent(this.recipeManager, displayedIngredients);
		this.anchorX = (int) mouseX;
		this.anchorY = (int) mouseY;
		this.visible = true;
		return true;
	}

	public boolean isMouseOverTooltip(double mouseX, double mouseY) {
		if (!this.visible) {
			return false;
		}
		RecipeSlotTooltipPositioner positioner = this.positioner;
		return positioner != null && getNavigationArea(positioner).contains(mouseX, mouseY);
	}

	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (!this.visible) {
			return Stream.empty();
		}
		InteractiveIngredientGridTooltipComponent candidateComponent = this.candidateComponent;
		if (candidateComponent != null) {
			Optional<IClickableIngredientInternal<?>> candidate = candidateComponent
				.getIngredientUnderMouse(mouseX, mouseY)
				.findFirst();
			if (candidate.isPresent()) {
				return candidate.stream();
			}
		}
		RecipeSlotUnderMouse sourceSlot = this.sourceSlot;
		if (sourceSlot != null && sourceSlot.isMouseOver(mouseX, mouseY)) {
			RecipeSlotNavigation.Action action = RecipeSlotNavigation.getAction(
				sourceSlot.slot(),
				this.isRecipeCyclingPaused.getAsBoolean()
			);
			return sourceSlot.slot()
				.getDisplayedIngredient()
				.<IClickableIngredientInternal<?>>map(ingredient -> createSourceIngredient(ingredient, sourceSlot, action))
				.stream();
		}
		return Stream.empty();
	}

	private <T> IClickableIngredientInternal<T> createSourceIngredient(
		ITypedIngredient<T> ingredient,
		RecipeSlotUnderMouse sourceSlot,
		RecipeSlotNavigation.Action action
	) {
		IElement<T> element;
		if (action == RecipeSlotNavigation.Action.TAG_RECIPE) {
			element = sourceSlot.slot()
				.getTagKey()
				.<IElement<T>>map(tagKey -> new TagIngredientElement<>(
					ingredient,
					tagKey,
					this.recipeManager,
					this.isRecipeCyclingPaused
				))
				.orElseGet(() -> new IngredientElement<>(ingredient));
		} else {
			element = new IngredientElement<>(ingredient);
		}
		return new ClickableIngredientInternal<>(element, sourceSlot::isMouseOver, false, true);
	}

	public boolean mouseScrolled(double scrollDeltaY) {
		InteractiveIngredientGridTooltipComponent candidateComponent = this.candidateComponent;
		return this.visible && candidateComponent != null && candidateComponent.mouseScrolled(scrollDeltaY);
	}

	public boolean isMouseOverGrid(double mouseX, double mouseY) {
		InteractiveIngredientGridTooltipComponent candidateComponent = this.candidateComponent;
		return this.visible && candidateComponent != null && candidateComponent.isMouseOver(mouseX, mouseY);
	}

	public boolean startScrollbarDrag(double mouseX, double mouseY) {
		InteractiveIngredientGridTooltipComponent candidateComponent = this.candidateComponent;
		return this.visible && candidateComponent != null && candidateComponent.startScrollbarDrag(mouseX, mouseY);
	}

	public boolean mouseDragged(double mouseY) {
		InteractiveIngredientGridTooltipComponent candidateComponent = this.candidateComponent;
		return this.visible && candidateComponent != null && candidateComponent.mouseDragged(mouseY);
	}

	public boolean isDraggingScrollbar() {
		InteractiveIngredientGridTooltipComponent candidateComponent = this.candidateComponent;
		return this.visible && candidateComponent != null && candidateComponent.isDraggingScrollbar();
	}

	public void stopScrollbarDrag() {
		InteractiveIngredientGridTooltipComponent candidateComponent = this.candidateComponent;
		if (candidateComponent != null) {
			candidateComponent.stopScrollbarDrag();
		}
	}

	@SuppressWarnings("removal")
	public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		RecipeSlotUnderMouse sourceSlot = this.sourceSlot;
		RecipeSlotTooltipPositioner positioner = this.positioner;
		InteractiveIngredientGridTooltipComponent candidateComponent = this.candidateComponent;
		if (!this.visible || sourceSlot == null || positioner == null || candidateComponent == null) {
			return;
		}

		JeiTooltip tooltip = new JeiTooltip();
		sourceSlot.slot().getTooltip(tooltip);
		showCandidateInstruction(tooltip);
		replaceIngredientGrid(tooltip, candidateComponent);
		candidateComponent.setMousePosition(mouseX, mouseY);

		ImmutableRect2i navigationArea = getNavigationArea(positioner);
		guiGraphics.nextStratum();
		guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), SCREEN_DIM_COLOR);
		guiGraphics.nextStratum();
		if (!navigationArea.isEmpty()) {
			this.navigationBackground.draw(
				guiGraphics,
				navigationArea.x(),
				navigationArea.y(),
				navigationArea.width(),
				navigationArea.height()
			);
		}
		guiGraphics.nextStratum();
		tooltip.draw(guiGraphics, this.anchorX, this.anchorY, positioner);
		candidateComponent.getTypedIngredientUnderMouse(mouseX, mouseY)
			.ifPresent(ingredient -> drawIngredientTooltip(guiGraphics, mouseX, mouseY, ingredient));

		if (getNavigationArea(positioner).contains(mouseX, mouseY)) {
			if (candidateComponent.isDraggingScrollbar()) {
				guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
			} else if (candidateComponent.getTypedIngredientUnderMouse(mouseX, mouseY).isPresent() ||
				candidateComponent.isMouseOverScrollbar(mouseX, mouseY)
			) {
				guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
			} else {
				guiGraphics.requestCursor(CursorTypes.ARROW);
			}
		}
	}

	private <T> void drawIngredientTooltip(
		GuiGraphicsExtractor guiGraphics,
		int mouseX,
		int mouseY,
		ITypedIngredient<T> ingredient
	) {
		IIngredientType<T> ingredientType = ingredient.getType();
		IIngredientRenderer<T> ingredientRenderer = this.ingredientManager.getIngredientRenderer(ingredientType);
		JeiTooltip tooltip = new JeiTooltip();
		SafeIngredientUtil.getRichTooltip(tooltip, this.ingredientManager, ingredientRenderer, ingredient);
		guiGraphics.nextStratum();
		tooltip.draw(guiGraphics, mouseX, mouseY, ingredient, ingredientRenderer, this.ingredientManager);
	}

	private static ImmutableRect2i getNavigationArea(RecipeSlotTooltipPositioner positioner) {
		ImmutableRect2i tooltipArea = positioner.getTooltipArea();
		if (tooltipArea.isEmpty()) {
			return ImmutableRect2i.EMPTY;
		}
		return tooltipArea.expandBy(NAVIGATION_BACKGROUND_PADDING);
	}

	private static void showCandidateInstruction(ITooltipBuilder tooltip) {
		tooltip.getLines().stream()
			.map(line -> line.right())
			.flatMap(Optional::stream)
			.filter(RecipeSlotCandidatesTooltipComponent.class::isInstance)
			.map(RecipeSlotCandidatesTooltipComponent.class::cast)
			.forEach(RecipeSlotCandidatesTooltipComponent::forceVisible);
	}

	private static void replaceIngredientGrid(ITooltipBuilder tooltip, TooltipComponent candidateComponent) {
		List<Either<FormattedText, TooltipComponent>> lines = tooltip.getLines();
		IngredientGridReplacement.replace(
			lines,
			line -> line.right()
				.filter(IIngredientGridTooltipComponent.class::isInstance)
				.isPresent(),
			Either.right(candidateComponent)
		);
	}

}
