package mezz.jei.library.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.elements.OffsetDrawable;
import mezz.jei.common.gui.elements.TextWidget;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.LimitedLogger;
import mezz.jei.library.gui.ingredients.CycleTicker;
import mezz.jei.library.gui.ingredients.RecipeSlot;
import mezz.jei.library.gui.recipes.layout.builder.RecipeLayoutBuilder;
import mezz.jei.library.gui.widgets.ScrollBoxRecipeWidget;
import mezz.jei.library.gui.widgets.ScrollGridRecipeWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
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
	private boolean displayedIngredientsUpdatePending;

	private ImmutableRect2i area;

	public static <T> Optional<IRecipeLayoutDrawable<T>> create(
		IRecipeCategory<T> recipeCategory,
		Collection<IRecipeCategoryDecorator<T>> recipeCategoryDecorators,
		T recipe,
		IFocusGroup focuses,
		IIngredientManager ingredientManager
	) {
		DrawableNineSliceTexture recipeBackground = Internal.getTextures().getRecipeBackground();
		return create(
			recipeCategory,
			recipeCategoryDecorators,
			recipe,
			focuses,
			ingredientManager,
			recipeBackground,
			DEFAULT_RECIPE_BORDER_PADDING
		);
	}

	public static <T> Optional<IRecipeLayoutDrawable<T>> create(
		IRecipeCategory<T> recipeCategory,
		Collection<IRecipeCategoryDecorator<T>> recipeCategoryDecorators,
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
				recipeCategoryDecorators,
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

		this.recipeCategorySlots = new ArrayList<>(recipeCategorySlots);
		this.recipeSlotsView = new RecipeSlotsView(Collections.unmodifiableList(allSlots));
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

		for (IRecipeSlotDrawable slot : allSlots) {
			if (slot instanceof RecipeSlot recipeSlot) {
				recipeSlot.setDisplayOverridesChangedListener(this::onDisplayOverridesChanged);
			}
		}
		updateDisplayedIngredients(false);
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
	public void drawRecipe(PoseStack poseStack, int mouseX, int mouseY) {
		ensureRecipeExtrasAreCreated();
		@SuppressWarnings("removal")
		IDrawable background = recipeCategory.getBackground();

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		recipeBackground.draw(poseStack, getRectWithBorder());

		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();

		IRecipeSlotsView recipeCategorySlotsView = () -> Collections.unmodifiableList(recipeCategorySlots);

		poseStack.pushPose();
		{
			poseStack.translate(area.getX(), area.getY(), 0);
			if (background != null) {
				background.draw(poseStack);
			}

			// defensive push/pop to protect against recipe categories changing the last pose
			poseStack.pushPose();
			{
				recipeCategory.draw(recipe, recipeCategorySlotsView, poseStack, recipeMouseX, recipeMouseY);
				for (IRecipeSlotDrawable slot : recipeCategorySlots) {
					slot.draw(poseStack, false);
				}
				for (IRecipeWidget widget : allWidgets) {
					Rect2i widgetArea = widget.getArea();
					poseStack.pushPose();
					{
						poseStack.translate(widgetArea.getX(), widgetArea.getY(), 0);
						widget.drawWidget(poseStack, recipeMouseX - widgetArea.getX(), recipeMouseY - widgetArea.getY());
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
					drawable.draw(poseStack);

					// rendered text often messes with the color, this clears it
					RenderSystem.setShaderColor(1, 1, 1, 1);
				}
				poseStack.popPose();
			}

			for (IRecipeCategoryDecorator<R> decorator : recipeCategoryDecorators) {
				// defensive push/pop to protect against recipe category decorators changing the last pose
				poseStack.pushPose();
				{
					decorator.draw(recipe, recipeCategory, recipeCategorySlotsView, poseStack, recipeMouseX, recipeMouseY);

					// rendered text often messes with the color, this clears it
					RenderSystem.setShaderColor(1, 1, 1, 1);
				}
				poseStack.popPose();
			}

			if (shapelessIcon != null) {
				shapelessIcon.draw(poseStack);
			}
		}
		poseStack.popPose();

		RenderSystem.disableBlend();
	}

	@Override
	public void drawOverlays(PoseStack poseStack, int mouseX, int mouseY) {
		ensureRecipeExtrasAreCreated();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		final int recipeMouseX = mouseX - area.getX();
		final int recipeMouseY = mouseY - area.getY();

		RenderSystem.disableBlend();

		IRecipeSlotsView recipeCategorySlotsView = () -> Collections.unmodifiableList(recipeCategorySlots);
		RecipeSlotUnderMouse hoveredSlotResult = getSlotUnderMouse(mouseX, mouseY).orElse(null);

		if (hoveredSlotResult != null) {
			drawSlotTooltip(poseStack, mouseX, mouseY, hoveredSlotResult);
		} else if (isMouseOver(mouseX, mouseY)) {
			JeiTooltip tooltip = new JeiTooltip();
			try {
				recipeCategory.getTooltip(tooltip, recipe, recipeCategorySlotsView, recipeMouseX, recipeMouseY);
				for (IRecipeCategoryDecorator<R> decorator : recipeCategoryDecorators) {
					List<Component> components = tooltip.getLegacyComponents();
					var results = decorator.decorateExistingTooltips(components, recipe, recipeCategory, recipeCategorySlotsView, recipeMouseX, recipeMouseY);
					if (results != components) {
						tooltip = new JeiTooltip();
						tooltip.addAll(results);
					}
					decorator.decorateTooltips(tooltip, recipe, recipeCategory, recipeCategorySlotsView, recipeMouseX, recipeMouseY);
				}
			} catch (RuntimeException e) {
				LIMITED_LOGGER.log(
					Level.ERROR,
					"recipe.category.tooltip.crash",
					logger -> logger.error(
						"Error while getting tooltip from recipe:\n{}",
						ErrorUtil.getRecipeInfo(recipeCategory, recipe),
						e
					)
				);
			}

			for (IRecipeWidget widget : allWidgets) {
				Rect2i widgetArea = widget.getArea();
				widget.getTooltip(tooltip, recipeMouseX - widgetArea.getX(), recipeMouseY - widgetArea.getY());
			}

			if (tooltip.isEmpty() && shapelessIcon != null) {
				shapelessIcon.addTooltipStrings(tooltip, recipeMouseX, recipeMouseY);
			}
			tooltip.draw(poseStack, mouseX, mouseY);
		}
	}

	@SuppressWarnings("removal")
	private void drawSlotTooltip(PoseStack poseStack, int mouseX, int mouseY, RecipeSlotUnderMouse hoveredSlotResult) {
		IRecipeSlotDrawable hoveredSlot = hoveredSlotResult.slot();

		poseStack.pushPose();
		{
			poseStack.translate(hoveredSlotResult.x(), hoveredSlotResult.y(), 0);
			hoveredSlot.drawHoverOverlays(poseStack);
		}
		poseStack.popPose();

		hoveredSlot.drawTooltip(poseStack, mouseX, mouseY);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return area.contains(mouseX, mouseY);
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
		ensureRecipeExtrasAreCreated();
		final double recipeMouseX = mouseX - area.getX();
		final double recipeMouseY = mouseY - area.getY();

		for (ISlottedRecipeWidget widget : slottedWidgets) {
			Rect2i widgetArea = widget.getArea();
			double relativeMouseX = recipeMouseX - widgetArea.getX();
			double relativeMouseY = recipeMouseY - widgetArea.getY();
			Optional<RecipeSlotUnderMouse> slotResult = widget.getSlotUnderMouse(relativeMouseX, relativeMouseY);
			if (slotResult.isPresent()) {
				return slotResult
					.map(slot -> slot.addOffset(area.getX(), area.getY()));
			}
		}
		for (IRecipeSlotDrawable slot : recipeCategorySlots) {
			if (slot.isMouseOver(recipeMouseX, recipeMouseY)) {
				return Optional.of(new RecipeSlotUnderMouse(slot, area.getX(), area.getY()));
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
		return getSideButtonArea(1);
	}

	@Override
	public Rect2i getSideButtonArea(int buttonIndex) {
		Rect2i buttonArea = recipeTransferButtonArea.toMutable();
		if (buttonIndex > 0) {
			int maxRows = (getRectWithBorder().getHeight() + RECIPE_BUTTON_SPACING) / (buttonArea.getHeight() + RECIPE_BUTTON_SPACING);
			maxRows = Math.max(1, maxRows);
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
		ensureRecipeExtrasAreCreated();
		for (IRecipeWidget widget : allWidgets) {
			widget.tick();
		}
		boolean ingredientsCycled = cycleTicker.tick();
		if (ingredientsCycled || displayedIngredientsUpdatePending) {
			updateDisplayedIngredients(ingredientsCycled);
		}
	}

	private void onDisplayOverridesChanged() {
		displayedIngredientsUpdatePending = true;
	}

	private void updateDisplayedIngredients(boolean clearDisplayOverrides) {
		try {
			if (clearDisplayOverrides) {
				for (IRecipeSlotDrawable slot : recipeCategorySlots) {
					slot.clearDisplayOverrides();
				}
			}
			recipeCategory.onDisplayedIngredientsUpdate(
				recipe,
				Collections.unmodifiableList(recipeCategorySlots),
				focuses
			);
		} finally {
			// Ignore notifications caused by the category's own update to avoid a reentrant update loop.
			displayedIngredientsUpdatePending = false;
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

	private record RecipeSlotsView(@Unmodifiable List<IRecipeSlotView> allSlots) implements IRecipeSlotsView {
		@Override
		public @Unmodifiable List<IRecipeSlotView> getSlotViews() {
			return allSlots;
		}
	}
}
