package mezz.jei.library.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.placement.IPlaceable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.gui.widgets.IScrollBoxWidget;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.api.gui.widgets.ITextWidget;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.elements.DrawableAnimated;
import mezz.jei.common.gui.elements.DrawableCombined;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.elements.OffsetDrawable;
import mezz.jei.common.gui.elements.TextWidget;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.library.gui.ingredients.CycleTicker;
import mezz.jei.library.gui.recipes.layout.builder.RecipeLayoutBuilder;
import mezz.jei.library.gui.widgets.ScrollBoxRecipeWidget;
import mezz.jei.library.gui.widgets.ScrollGridRecipeWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.FormattedText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RecipeLayout<R> implements IRecipeLayoutDrawable<R>, IRecipeExtrasBuilder {
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
	@Unmodifiable
	private final List<IRecipeSlotDrawable> allSlots;
	private final List<IDrawable> drawables;
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
			RecipeLayout<T> recipeLayout = builder.buildRecipeLayout(
				focuses,
				decorators,
				recipeBackground,
				recipeBorderPadding
			);
			recipeCategory.createRecipeExtras(recipeLayout, recipe, focuses);
			return Optional.of(recipeLayout);
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Error caught from Recipe Category: {}", recipeCategory.getRecipeType(), e);
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
		CycleTicker cycleTicker,
		IFocusGroup focuses
	) {
		this.recipeCategory = recipeCategory;
		this.recipeCategoryDecorators = recipeCategoryDecorators;
		this.drawables = new ArrayList<>();
		this.slottedWidgets = new ArrayList<>();
		this.allWidgets = new ArrayList<>();
		this.cycleTicker = cycleTicker;
		this.focuses = focuses;
		this.inputHandler = new RecipeLayoutInputHandler<>(this);

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

		recipeCategory.onDisplayedIngredientsUpdate(recipe, Collections.unmodifiableList(recipeCategorySlots), focuses);
	}

	@Override
	public void setPosition(int posX, int posY) {
		area = area.setPosition(posX, posY);
	}

	@Override
	public void drawRecipe(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		@SuppressWarnings("removal")
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
			if (background != null) {
				background.draw(guiGraphics);
			}

			// defensive push/pop to protect against recipe categories changing the last pose
			poseStack.pushPose();
			{
				recipeCategory.draw(recipe, recipeCategorySlotsView, guiGraphics, recipeMouseX, recipeMouseY);
				for (IRecipeSlotDrawable slot : recipeCategorySlots) {
					slot.draw(guiGraphics);
				}
				for (IRecipeWidget widget : allWidgets) {
					ScreenPosition position = widget.getPosition();
					poseStack.pushPose();
					{
						poseStack.translate(position.x(), position.y(), 0);
						widget.drawWidget(guiGraphics, recipeMouseX - position.x(), recipeMouseY - position.y());
					}
					poseStack.popPose();
				}

				// drawExtras and drawInfo often render text which messes with the color, this clears it
				RenderSystem.setShaderColor(1, 1, 1, 1);
			}
			poseStack.popPose();

			for (IDrawable drawable : drawables) {
				// defensive push/pop to protect against recipe category drawables changing the last pose
				poseStack.pushPose();
				{
					drawable.draw(guiGraphics);

					// rendered text often messes with the color, this clears it
					RenderSystem.setShaderColor(1, 1, 1, 1);
				}
				poseStack.popPose();
			}

			for (IRecipeCategoryDecorator<R> decorator : recipeCategoryDecorators) {
				// defensive push/pop to protect against recipe category decorators changing the last pose
				poseStack.pushPose();
				{
					decorator.draw(recipe, recipeCategory, recipeCategorySlotsView, guiGraphics, recipeMouseX, recipeMouseY);

					// rendered text often messes with the color, this clears it
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

			for (IRecipeWidget widget : allWidgets) {
				ScreenPosition position = widget.getPosition();
				widget.getTooltip(tooltip, recipeMouseX - position.x(), recipeMouseY - position.y());
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
	public IRecipeSlotDrawablesView getRecipeSlots() {
		return () -> Collections.unmodifiableList(recipeCategorySlots);
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

	@Override
	public void addDrawable(IDrawable drawable, int xPos, int yPos) {
		this.drawables.add(OffsetDrawable.create(drawable, xPos, yPos));
	}

	@Override
	public IPlaceable<?> addDrawable(IDrawable drawable) {
		OffsetDrawable offsetDrawable = new OffsetDrawable(drawable, 0, 0);
		this.drawables.add(offsetDrawable);
		return offsetDrawable;
	}

	@Override
	public void addWidget(IRecipeWidget widget) {
		this.allWidgets.add(widget);
		if (widget instanceof ISlottedRecipeWidget slottedWidget) {
			this.slottedWidgets.add(slottedWidget);
		}
	}

	@Override
	public void addSlottedWidget(ISlottedRecipeWidget widget, List<IRecipeSlotDrawable> slots) {
		this.allWidgets.add(widget);
		this.slottedWidgets.add(widget);
		this.recipeCategorySlots.removeAll(slots);
	}

	@Override
	public void addInputHandler(IJeiInputHandler inputHandler) {
		this.inputHandler.addInputHandler(inputHandler);
	}

	@Override
	public void addGuiEventListener(IJeiGuiEventListener guiEventListener) {
		this.inputHandler.addGuiEventListener(guiEventListener);
	}

	@Override
	public IScrollBoxWidget addScrollBoxWidget(int width, int height, int xPos, int yPos) {
		ScrollBoxRecipeWidget widget = new ScrollBoxRecipeWidget(width, height, xPos, yPos);
		addWidget(widget);
		addInputHandler(widget);
		return widget;
	}

	@Override
	public IScrollGridWidget addScrollGridWidget(List<IRecipeSlotDrawable> slots, int columns, int visibleRows) {
		ScrollGridRecipeWidget widget = ScrollGridRecipeWidget.create(slots, columns, visibleRows);
		addSlottedWidget(widget, slots);
		addInputHandler(widget);
		return widget;
	}

	@Override
	public IPlaceable<?> addRecipeArrow() {
		Textures textures = Internal.getTextures();
		IDrawable drawable = textures.getRecipeArrow();
		return addDrawable(drawable);
	}

	@Override
	public IPlaceable<?> addRecipePlusSign() {
		Textures textures = Internal.getTextures();
		IDrawable drawable = textures.getRecipePlusSign();
		return addDrawable(drawable);
	}

	@Override
	public IPlaceable<?> addAnimatedRecipeArrow(int ticksPerCycle) {
		Textures textures = Internal.getTextures();

		IDrawableStatic recipeArrowFilled = textures.getRecipeArrowFilled();
		IDrawable animatedFill = new DrawableAnimated(recipeArrowFilled, ticksPerCycle, IDrawableAnimated.StartDirection.LEFT, false);
		IDrawable drawableCombined = new DrawableCombined(textures.getRecipeArrow(), animatedFill);
		OffsetDrawable offsetDrawable = new OffsetDrawable(drawableCombined, 0, 0);
		return addDrawable(offsetDrawable);
	}

	@Override
	public IPlaceable<?> addAnimatedRecipeFlame(int cookTime) {
		Textures textures = Internal.getTextures();

		IDrawableStatic flameIcon = textures.getFlameIcon();
		IDrawableAnimated animatedFill = new DrawableAnimated(flameIcon, cookTime, IDrawableAnimated.StartDirection.TOP, true);

		IDrawable drawableCombined = new DrawableCombined(textures.getFlameEmptyIcon(), animatedFill);
		OffsetDrawable offsetDrawable = new OffsetDrawable(drawableCombined, 0, 0);
		return addDrawable(offsetDrawable);
	}

	@Override
	public ITextWidget addText(List<FormattedText> text, int maxWidth, int maxHeight) {
		TextWidget textWidget = new TextWidget(text, 0, 0, maxWidth, maxHeight);
		addWidget(textWidget);
		return textWidget;
	}
}
