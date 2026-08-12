package mezz.jei.gui.recipes;

import com.mojang.datafixers.util.Either;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.common.gui.IIngredientGridTooltipComponent;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.input.ClickableIngredientInternal;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import mezz.jei.gui.overlay.elements.TagIngredientElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

public class IngredientCandidateOverlay {
	private final IRecipeManager recipeManager;
	private final BooleanSupplier isRecipeCyclingPaused;

	private boolean visible;
	private @Nullable RecipeSlotUnderMouse sourceSlot;
	private ImmutableRect2i sourceArea = ImmutableRect2i.EMPTY;
	private @Nullable RecipeSlotTooltipPositioner positioner;
	private @Nullable IngredientCandidateTooltipComponent candidateComponent;

	public IngredientCandidateOverlay(IRecipeManager recipeManager, BooleanSupplier isRecipeCyclingPaused) {
		this.recipeManager = recipeManager;
		this.isRecipeCyclingPaused = isRecipeCyclingPaused;
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void hide() {
		this.visible = false;
		this.sourceSlot = null;
		this.sourceArea = ImmutableRect2i.EMPTY;
		this.positioner = null;
		this.candidateComponent = null;
	}

	public boolean show(RecipeSlotUnderMouse slotUnderMouse) {
		List<ITypedIngredient<?>> displayedIngredients = slotUnderMouse.slot()
			.getDisplayedIngredients()
			.toList();
		if (displayedIngredients.size() <= 1) {
			return false;
		}

		Rect2i relativeArea = slotUnderMouse.slot().getAreaIncludingBackground();
		this.sourceArea = new ImmutableRect2i(
			slotUnderMouse.offset().x() + relativeArea.getX(),
			slotUnderMouse.offset().y() + relativeArea.getY(),
			relativeArea.getWidth(),
			relativeArea.getHeight()
		);
		this.sourceSlot = slotUnderMouse;
		this.positioner = new RecipeSlotTooltipPositioner(this.sourceArea);
		this.candidateComponent = new IngredientCandidateTooltipComponent(this.recipeManager, displayedIngredients);
		this.visible = true;
		return true;
	}

	public void updateVisibility(double mouseX, double mouseY) {
		if (!this.visible || isMouseOver(mouseX, mouseY)) {
			return;
		}
		hide();
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		if (!this.visible) {
			return false;
		}
		if (this.sourceArea.contains(mouseX, mouseY)) {
			return true;
		}
		RecipeSlotTooltipPositioner positioner = this.positioner;
		return positioner != null && MathUtil.union(this.sourceArea, positioner.getTooltipArea()).contains(mouseX, mouseY);
	}

	public boolean isMouseOverTooltip(double mouseX, double mouseY) {
		if (!this.visible) {
			return false;
		}
		RecipeSlotTooltipPositioner positioner = this.positioner;
		return positioner != null && positioner.getTooltipArea().contains(mouseX, mouseY);
	}

	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (!this.visible) {
			return Stream.empty();
		}
		IngredientCandidateTooltipComponent candidateComponent = this.candidateComponent;
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
			if (action == RecipeSlotNavigation.Action.CANDIDATE_GROUP) {
				return Stream.empty();
			}
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
		IngredientCandidateTooltipComponent candidateComponent = this.candidateComponent;
		return this.visible && candidateComponent != null && candidateComponent.mouseScrolled(scrollDeltaY);
	}

	@SuppressWarnings("removal")
	public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		RecipeSlotUnderMouse sourceSlot = this.sourceSlot;
		RecipeSlotTooltipPositioner positioner = this.positioner;
		IngredientCandidateTooltipComponent candidateComponent = this.candidateComponent;
		if (!this.visible || sourceSlot == null || positioner == null || candidateComponent == null) {
			return;
		}

		JeiTooltip tooltip = new JeiTooltip();
		sourceSlot.slot().getTooltip(tooltip);
		replaceIngredientGrid(tooltip, candidateComponent);
		candidateComponent.setMousePosition(mouseX, mouseY);

		guiGraphics.nextStratum();
		tooltip.draw(guiGraphics, positioner.getAnchorX(), positioner.getAnchorY(), positioner);
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

	public void drawTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		IngredientCandidateTooltipComponent candidateComponent = this.candidateComponent;
		if (this.visible && candidateComponent != null) {
			candidateComponent.drawTooltip(guiGraphics, mouseX, mouseY);
		}
	}
}
