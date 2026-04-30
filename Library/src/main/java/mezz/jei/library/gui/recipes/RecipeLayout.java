package mezz.jei.library.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
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
import mezz.jei.common.gui.elements.OffsetDrawable;
import mezz.jei.common.gui.elements.TextWidget;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.core.util.LimitedLogger;
import mezz.jei.library.gui.ingredients.CycleTicker;
import mezz.jei.library.gui.recipes.layout.builder.RecipeLayoutBuilder;
import mezz.jei.library.gui.widgets.ScrollBoxRecipeWidget;
import mezz.jei.library.gui.widgets.ScrollGridRecipeWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.FormattedText;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RecipeLayout<R> implements IRecipeLayoutDrawable<R>, IRecipeExtrasBuilder {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final LimitedLogger LIMITED_LOGGER = new LimitedLogger(LOGGER, Duration.ofSeconds(10));

	public static final int RECIPE_BUTTON_SIZE = 13;
	public static final int RECIPE_BUTTON_SPACING = 2;

	private final IRecipeCategory<R> recipeCategory;
	private final Collection<IRecipeCategoryDecorator<R>> recipeCategoryDecorators;
	/**
	 * Slots handled by the recipe category directly.
	 */
	private final List<IRecipeSlotDrawable> slots;
	/**
	 * All slots, including slots handled by the recipe category and widgets.
	 */
	private final IRecipeSlotsView recipeSlotsView;
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
	private boolean extrasCreated = false;

	private ImmutableRect2i area;

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
			return Optional.of(recipeLayout);
		} catch (RuntimeException | LinkageError e) {
			String recipeInfo = ErrorUtil.getRecipeInfo(recipeCategory, recipe);
			LOGGER.error("Recipe crashed during Recipe Layout creation:\n{}", recipeInfo, e);
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
		List<IRecipeSlotDrawable> slots,
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

		this.slots = slots;
		this.recipeSlotsView = new RecipeSlotsView(List.copyOf(slots));
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

		recipeCategory.onDisplayedIngredientsUpdate(recipe, Collections.unmodifiableList(this.slots), focuses);
	}

	public void ensureRecipeExtrasAreCreated() {
		if (!extrasCreated) {
			extrasCreated = true;
			recipeCategory.createRecipeExtras(this, recipe, focuses);
		}
	}

	@Override
	public void setPosition(int posX, int posY) {
		area = area.setPosition(posX, posY);
	}

	@Override
	public void drawRecipe(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		ensureRecipeExtrasAreCreated();

		recipeBackground.draw(guiGraphics, getRectWithBorder());

		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();

		IRecipeSlotsView recipeCategorySlotsView = () -> Collections.unmodifiableList(slots);

		var poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		{
			poseStack.translate(area.getX(), area.getY());

			// defensive push/pop to protect against recipe categories changing the last pose
			poseStack.pushMatrix();
			{
				recipeCategory.draw(recipe, recipeCategorySlotsView, guiGraphics, recipeMouseX, recipeMouseY);
				for (IRecipeSlotDrawable slot : slots) {
					slot.draw(guiGraphics);
				}
				for (IRecipeWidget widget : allWidgets) {
					ScreenPosition position = widget.getPosition();
					poseStack.pushMatrix();
					{
						poseStack.translate(position.x(), position.y());
						widget.drawWidget(guiGraphics, recipeMouseX - position.x(), recipeMouseY - position.y());
					}
					poseStack.popMatrix();
				}
			}
			poseStack.popMatrix();

			for (IDrawable drawable : drawables) {
				// defensive push/pop to protect against recipe category drawables changing the last pose
				poseStack.pushMatrix();
				{
					drawable.draw(guiGraphics);
				}
				poseStack.popMatrix();
			}

			for (IRecipeCategoryDecorator<R> decorator : recipeCategoryDecorators) {
				// defensive push/pop to protect against recipe category decorators changing the last pose
				poseStack.pushMatrix();
				{
					decorator.draw(recipe, recipeCategory, recipeCategorySlotsView, guiGraphics, recipeMouseX, recipeMouseY);
				}
				poseStack.popMatrix();
			}

			if (shapelessIcon != null) {
				shapelessIcon.draw(guiGraphics);
			}
		}
		poseStack.popMatrix();
	}

	@Override
	public void drawOverlays(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		ensureRecipeExtrasAreCreated();

		final int recipeMouseX = mouseX - area.getX();
		final int recipeMouseY = mouseY - area.getY();

		IRecipeSlotsView recipeCategorySlotsView = () -> Collections.unmodifiableList(slots);
		RecipeSlotUnderMouse hoveredSlotResult = getSlotUnderMouse(mouseX, mouseY).orElse(null);

		var poseStack = guiGraphics.pose();
		if (hoveredSlotResult != null) {
			IRecipeSlotDrawable hoveredSlot = hoveredSlotResult.slot();

			poseStack.pushMatrix();
			{
				ScreenPosition offset = hoveredSlotResult.offset();
				poseStack.translate(offset.x(), offset.y());
				hoveredSlot.drawHoverOverlays(guiGraphics);
			}
			poseStack.popMatrix();

			hoveredSlot.drawTooltip(guiGraphics, mouseX, mouseY);
		} else if (isMouseOver(mouseX, mouseY)) {
			JeiTooltip tooltip = new JeiTooltip();
			try {
				recipeCategory.getTooltip(tooltip, recipe, recipeCategorySlotsView, recipeMouseX, recipeMouseY);
				for (IRecipeCategoryDecorator<R> decorator : recipeCategoryDecorators) {
					decorator.decorateTooltips(tooltip, recipe, recipeCategory, recipeCategorySlotsView, recipeMouseX, recipeMouseY);
				}
			} catch (RuntimeException e) {
				LIMITED_LOGGER.log(
					Level.ERROR,
					"recipe.category.tooltip.crash",
					logger -> {
						logger.error(
							"Error while getting tooltip from recipe:\n{}",
							ErrorUtil.getRecipeInfo(recipeCategory, recipe),
							e
						);
					}
				);
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
	public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
		ensureRecipeExtrasAreCreated();
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
		for (IRecipeSlotDrawable slot : slots) {
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
	public Rect2i getSideButtonArea(int buttonIndex) {
		Rect2i buttonArea = recipeTransferButtonArea.toMutable();
		if (buttonIndex > 0) {
			int maxRows = (getRectWithBorder().getHeight() + RECIPE_BUTTON_SPACING) / (buttonArea.getHeight() + RECIPE_BUTTON_SPACING);
			int xIndex = buttonIndex / maxRows;
			int yIndex = buttonIndex % maxRows;
			int xOffset = xIndex * (buttonArea.getWidth() + RECIPE_BUTTON_SPACING);
			int yOffset = yIndex * (buttonArea.getHeight() + RECIPE_BUTTON_SPACING);

			buttonArea.setX(buttonArea.getX() + xOffset);
			buttonArea.setY(buttonArea.getY() - yOffset);
		}
		return buttonArea;
	}

	@Override
	public IRecipeSlotsView getRecipeSlotsView() {
		return recipeSlotsView;
	}

	@Override
	public IRecipeSlotDrawablesView getRecipeSlots() {
		ensureRecipeExtrasAreCreated();
		return () -> Collections.unmodifiableList(slots);
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
		ensureRecipeExtrasAreCreated();
		for (IRecipeWidget widget : allWidgets) {
			widget.tick();
		}
		if (cycleTicker.tick()) {
			for (IRecipeSlotDrawable slot : slots) {
				slot.clearDisplayOverrides();
			}
			recipeCategory.onDisplayedIngredientsUpdate(recipe, slots, focuses);
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
		this.slots.removeAll(slots);
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

	private record RecipeSlotsView(@Unmodifiable List<IRecipeSlotView> slots) implements IRecipeSlotsView {
		@Override
		public @Unmodifiable List<IRecipeSlotView> getSlotViews() {
			return slots;
		}
	}
}
