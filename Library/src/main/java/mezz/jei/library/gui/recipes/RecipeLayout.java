package mezz.jei.library.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.library.gui.ingredients.CycleTicker;
import mezz.jei.library.gui.recipes.layout.builder.RecipeLayoutBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.renderer.Rect2i;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RecipeLayout<R> implements IRecipeLayoutDrawable<R> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int DEFAULT_RECIPE_BORDER_PADDING = 4;
	public static final int RECIPE_BUTTON_SIZE = 13;
	public static final int RECIPE_BUTTON_SPACING = 2;

	private final IRecipeCategory<R> recipeCategory;
	private final Collection<IRecipeCategoryDecorator<R>> recipeCategoryDecorators;
	/**
	 * Slots handled by the recipe category directly.
	 */
	private final List<IRecipeSlotDrawable> recipeCategorySlots;
	/**
	 * All slots, including slots handled by the recipe category and widgets.
	 */
	private final List<IRecipeSlotDrawable> allSlots;
	private final List<ISlottedRecipeWidget> slottedWidgets;
	private final CycleTicker cycleTicker;
	private final IFocusGroup focuses;
	private final List<IRecipeWidget> allWidgets;
	private final R recipe;
	private final IScalableDrawable recipeBackground;
	private final int recipeBorderPadding;
	private final ImmutableRect2i recipeTransferButtonArea;
	private final @Nullable ShapelessIcon shapelessIcon;
	private final RecipeLayoutInputHandler<R> inputHandler;

	private ImmutableRect2i area;

	public static <T> Optional<IRecipeLayoutDrawable<T>> create(
		IRecipeCategory<T> recipeCategory,
		Collection<IRecipeCategoryDecorator<T>> decorators,
		T recipe,
		IFocusGroup focuses,
		IIngredientManager ingredientManager
	) {
		DrawableNineSliceTexture recipeBackground = Internal.getTextures().getRecipeBackground();
		return create(
			recipeCategory,
			decorators,
			recipe,
			focuses,
			ingredientManager,
			recipeBackground,
			DEFAULT_RECIPE_BORDER_PADDING
		);
	}

	public static <T> Optional<IRecipeLayoutDrawable<T>> create(
		IRecipeCategory<T> recipeCategory,
		Collection<IRecipeCategoryDecorator<T>> decorators,
		T recipe,
		IFocusGroup focuses,
		IIngredientManager ingredientManager,
		IScalableDrawable recipeBackground,
		int recipeBorderPadding
	) {
		RecipeLayoutBuilder<T> builder = new RecipeLayoutBuilder<>(recipeCategory, recipe, ingredientManager);
		try {
			recipeCategory.setRecipe(builder, recipe, focuses);
			recipeCategory.createRecipeExtras(builder, recipe, focuses);
			RecipeLayout<T> recipeLayout = builder.buildRecipeLayout(
				focuses,
				decorators,
				recipeBackground,
				recipeBorderPadding
			);
			return Optional.of(recipeLayout);
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Error caught from Recipe Category: {}", recipeCategory.getRecipeType().getUid(), e);
		}
		return Optional.empty();
	}

	public RecipeLayout(
		IRecipeCategory<R> recipeCategory,
		Collection<IRecipeCategoryDecorator<R>> recipeCategoryDecorators,
		R recipe,
		IScalableDrawable recipeBackground,
		int recipeBorderPadding,
		@Nullable ShapelessIcon shapelessIcon,
		ImmutablePoint2i recipeTransferButtonPos,
		List<IRecipeSlotDrawable> recipeCategorySlots,
		List<IRecipeSlotDrawable> allSlots,
		List<ISlottedRecipeWidget> slottedWidgets,
		List<IRecipeWidget> widgets,
		List<IJeiInputHandler> inputHandlers,
		List<IJeiGuiEventListener> guiEventListeners,
		CycleTicker cycleTicker,
		IFocusGroup focuses
	) {
		this.recipeCategory = recipeCategory;
		this.recipeCategoryDecorators = recipeCategoryDecorators;
		this.slottedWidgets = Collections.unmodifiableList(slottedWidgets);
		this.cycleTicker = cycleTicker;
		this.focuses = focuses;
		this.inputHandler = new RecipeLayoutInputHandler<>(this, inputHandlers, guiEventListeners);

		Set<IRecipeWidget> allWidgets = new HashSet<>(widgets);
		allWidgets.addAll(slottedWidgets);
		this.allWidgets = List.copyOf(allWidgets);

		this.recipeCategorySlots = recipeCategorySlots;
		this.allSlots = Collections.unmodifiableList(allSlots);
		this.recipeBorderPadding = recipeBorderPadding;
		this.area = new ImmutableRect2i(
			0,
			0,
			recipeCategory.getWidth(),
			recipeCategory.getHeight()
		);

		this.recipeTransferButtonArea = new ImmutableRect2i(
			recipeTransferButtonPos.x(),
			recipeTransferButtonPos.y(),
			RECIPE_BUTTON_SIZE,
			RECIPE_BUTTON_SIZE
		);

		this.recipe = recipe;
		this.recipeBackground = recipeBackground;
		this.shapelessIcon = shapelessIcon;

		recipeCategory.onDisplayedIngredientsUpdate(recipe, recipeCategorySlots, focuses);
	}

	@Override
	public void setPosition(int posX, int posY) {
		area = area.setPosition(posX, posY);
	}

	@Override
	public void drawRecipe(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		IDrawable background = recipeCategory.getBackground();

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		recipeBackground.draw(guiGraphics, getRectWithBorder());

		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();

		IRecipeSlotsView recipeCategorySlotsView = () -> Collections.unmodifiableList(recipeCategorySlots);

		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			poseStack.translate(area.getX(), area.getY(), 0);
			background.draw(guiGraphics);

			// defensive push/pop to protect against recipe categories changing the last pose
			poseStack.pushPose();
			{
				for (IRecipeSlotDrawable slot : recipeCategorySlots) {
					slot.draw(guiGraphics);
				}
				recipeCategory.draw(recipe, recipeCategorySlotsView, guiGraphics, recipeMouseX, recipeMouseY);
				for (IRecipeWidget widget : allWidgets) {
					ScreenPosition position = widget.getPosition();
					poseStack.pushPose();
					{
						poseStack.translate(position.x(), position.y(), 0);
						widget.draw(guiGraphics, recipeMouseX, recipeMouseY);
					}
					poseStack.popPose();
				}

				// drawExtras and drawInfo often render text which messes with the color, this clears it
				RenderSystem.setShaderColor(1, 1, 1, 1);
			}
			poseStack.popPose();

			for (IRecipeCategoryDecorator<R> decorator : recipeCategoryDecorators) {
				// defensive push/pop to protect against recipe category decorators changing the last pose
				poseStack.pushPose();
				{
					decorator.draw(recipe, recipeCategory, recipeCategorySlotsView, guiGraphics, recipeMouseX, recipeMouseY);

					// drawExtras and drawInfo often render text which messes with the color, this clears it
					RenderSystem.setShaderColor(1, 1, 1, 1);
				}
				poseStack.popPose();
			}

			if (shapelessIcon != null) {
				shapelessIcon.draw(guiGraphics);
			}
		}
		poseStack.popPose();

		RenderSystem.disableBlend();
	}

	@Override
	public void drawOverlays(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		final int recipeMouseX = mouseX - area.getX();
		final int recipeMouseY = mouseY - area.getY();

		RenderSystem.disableBlend();

		IRecipeSlotsView recipeCategorySlotsView = () -> Collections.unmodifiableList(recipeCategorySlots);
		RecipeSlotUnderMouse hoveredSlotResult = getSlotUnderMouse(mouseX, mouseY).orElse(null);

		var poseStack = guiGraphics.pose();
		if (hoveredSlotResult != null) {
			IRecipeSlotDrawable hoveredSlot = hoveredSlotResult.slot();

			poseStack.pushPose();
			{
				ScreenPosition offset = hoveredSlotResult.offset();
				poseStack.translate(offset.x(), offset.y(), 0);
				hoveredSlot.drawHoverOverlays(guiGraphics);
			}
			poseStack.popPose();

			JeiTooltip tooltip = new JeiTooltip();
			hoveredSlot.getTooltip(tooltip);
			tooltip.draw(guiGraphics, mouseX, mouseY);
		} else if (isMouseOver(mouseX, mouseY)) {
			JeiTooltip tooltip = new JeiTooltip();
			recipeCategory.getTooltip(tooltip, recipe, recipeCategorySlotsView, recipeMouseX, recipeMouseY);
			for (IRecipeCategoryDecorator<R> decorator : recipeCategoryDecorators) {
				decorator.decorateTooltips(tooltip, recipe, recipeCategory, recipeCategorySlotsView, recipeMouseX, recipeMouseY);
			}

			if (tooltip.isEmpty() && shapelessIcon != null) {
				if (shapelessIcon.isMouseOver(recipeMouseX, recipeMouseY)) {
					shapelessIcon.addTooltip(tooltip);
				}
			}
			tooltip.draw(guiGraphics, mouseX, mouseY);
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return MathUtil.contains(area, mouseX, mouseY);
	}

	@Override
	public Rect2i getRect() {
		return area.toMutable();
	}

	@Override
	public Rect2i getRectWithBorder() {
		return area.expandBy(recipeBorderPadding).toMutable();
	}

	@Override
	public <T> Optional<T> getIngredientUnderMouse(int mouseX, int mouseY, IIngredientType<T> ingredientType) {
		return getSlotUnderMouse(mouseX, mouseY)
			.map(RecipeSlotUnderMouse::slot)
			.flatMap(slot -> slot.getDisplayedIngredient(ingredientType));
	}

	@Override
	public Optional<IRecipeSlotDrawable> getRecipeSlotUnderMouse(double mouseX, double mouseY) {
		return getSlotUnderMouse(mouseX, mouseY)
			.map(RecipeSlotUnderMouse::slot);
	}

	@Override
	public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();

		for (ISlottedRecipeWidget widget : slottedWidgets) {
			ScreenPosition position = widget.getPosition();
			double relativeMouseX = recipeMouseX - position.x();
			double relativeMouseY = recipeMouseY - position.y();
			Optional<RecipeSlotUnderMouse> slotResult = widget.getSlotUnderMouse(relativeMouseX, relativeMouseY);
			if (slotResult.isPresent()) {
				return slotResult
					.map(slot -> slot.addOffset(area.x(), area.y()));
			}
		}
		for (IRecipeSlotDrawable slot : recipeCategorySlots) {
			if (slot.isMouseOver(recipeMouseX, recipeMouseY)) {
				return Optional.of(new RecipeSlotUnderMouse(slot, area.getScreenPosition()));
			}
		}
		return Optional.empty();
	}

	@Override
	public IRecipeCategory<R> getRecipeCategory() {
		return recipeCategory;
	}

	@Override
	public Rect2i getRecipeTransferButtonArea() {
		return recipeTransferButtonArea.toMutable();
	}

	@Override
	public Rect2i getRecipeBookmarkButtonArea() {
		Rect2i area = getRecipeTransferButtonArea();
		area.setPosition(area.getX(), area.getY() - area.getHeight() - RECIPE_BUTTON_SPACING);
		return area;
	}

	@SuppressWarnings("RedundantUnmodifiable")
	@Override
	public IRecipeSlotsView getRecipeSlotsView() {
		return () -> Collections.unmodifiableList(allSlots);
	}

	@Override
	public R getRecipe() {
		return recipe;
	}

	@Override
	public IJeiInputHandler getInputHandler() {
		return inputHandler;
	}

	@Override
	public void tick() {
		for (IRecipeWidget widget : allWidgets) {
			widget.tick();
		}
		if (cycleTicker.tick()) {
			for (IRecipeSlotDrawable slot : recipeCategorySlots) {
				slot.clearDisplayOverrides();
			}
			recipeCategory.onDisplayedIngredientsUpdate(recipe, recipeCategorySlots, focuses);
		}
	}
}
