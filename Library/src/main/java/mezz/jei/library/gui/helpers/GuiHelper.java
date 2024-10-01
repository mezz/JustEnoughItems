package mezz.jei.library.gui.helpers;

import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.gui.widgets.IScrollBoxWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableAnimated;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.common.gui.elements.DrawableCombined;
import mezz.jei.common.gui.elements.DrawableIngredient;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.TickTimer;
import mezz.jei.library.gui.elements.DrawableBuilder;
import mezz.jei.library.gui.widgets.AbstractScrollWidget;
import mezz.jei.library.gui.widgets.DrawableWidget;
import mezz.jei.library.gui.widgets.ScrollBoxRecipeWidget;
import mezz.jei.library.gui.widgets.ScrollGridWidgetFactory;
import net.minecraft.resources.ResourceLocation;

public class GuiHelper implements IGuiHelper {
	private final IIngredientManager ingredientManager;

	public GuiHelper(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	@Override
	public IDrawableBuilder drawableBuilder(ResourceLocation resourceLocation, int u, int v, int width, int height) {
		return new DrawableBuilder(resourceLocation, u, v, width, height);
	}

	@Override
	public IDrawableAnimated createAnimatedDrawable(IDrawableStatic drawable, int ticksPerCycle, IDrawableAnimated.StartDirection startDirection, boolean inverted) {
		ErrorUtil.checkNotNull(drawable, "drawable");
		ErrorUtil.checkNotNull(startDirection, "startDirection");
		return new DrawableAnimated(drawable, ticksPerCycle, startDirection, inverted);
	}

	@Override
	public IDrawableAnimated createAnimatedDrawable(IDrawableStatic drawable, ITickTimer tickTimer, IDrawableAnimated.StartDirection startDirection) {
		ErrorUtil.checkNotNull(drawable, "drawable");
		ErrorUtil.checkNotNull(tickTimer, "tickTimer");
		ErrorUtil.checkNotNull(startDirection, "startDirection");
		return new DrawableAnimated(drawable, tickTimer, startDirection);
	}

	@Override
	public IDrawableStatic getSlotDrawable() {
		Textures textures = Internal.getTextures();
		return textures.getSlot();
	}

	@Override
	public IDrawableStatic getOutputSlot() {
		Textures textures = Internal.getTextures();
		return textures.getOutputSlot();
	}

	@Override
	public IDrawableStatic getRecipeArrow() {
		Textures textures = Internal.getTextures();
		return textures.getRecipeArrow();
	}

	@Override
	public IDrawableStatic getRecipeArrowFilled() {
		Textures textures = Internal.getTextures();
		return textures.getRecipeArrowFilled();
	}

	@Override
	public IDrawableAnimated createAnimatedRecipeArrow(int ticksPerCycle) {
		IDrawableAnimated animatedFill = createAnimatedDrawable(getRecipeArrowFilled(), ticksPerCycle, IDrawableAnimated.StartDirection.LEFT, false);
		return new DrawableCombined(getRecipeArrow(), animatedFill);
	}

	@Override
	public IDrawableStatic getRecipePlusSign() {
		Textures textures = Internal.getTextures();
		return textures.getRecipePlusSign();
	}

	@Override
	public IDrawableStatic getRecipeFlameEmpty() {
		Textures textures = Internal.getTextures();
		return textures.getFlameEmptyIcon();
	}

	@Override
	public IDrawableStatic getRecipeFlameFilled() {
		Textures textures = Internal.getTextures();
		return textures.getFlameIcon();
	}

	@Override
	public IDrawableAnimated createAnimatedRecipeFlame(int ticksPerCycle) {
		IDrawableAnimated animatedFill = createAnimatedDrawable(getRecipeFlameFilled(), ticksPerCycle, IDrawableAnimated.StartDirection.TOP, true);
		return new DrawableCombined(getRecipeFlameEmpty(), animatedFill);
	}

	@Override
	public IRecipeWidget createWidgetFromDrawable(IDrawable drawable, int xPos, int yPos) {
		return new DrawableWidget(drawable, xPos, yPos);
	}

	@Override
	public IDrawableStatic createBlankDrawable(int width, int height) {
		return new DrawableBlank(width, height);
	}

	@Override
	public <V> IDrawable createDrawableIngredient(IIngredientType<V> type, V ingredient) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		IIngredientRenderer<V> ingredientRenderer = ingredientManager.getIngredientRenderer(type);
		ITypedIngredient<V> typedIngredient = ingredientManager.createTypedIngredient(type, ingredient)
			.orElseThrow(() -> {
				String info = ErrorUtil.getIngredientInfo(ingredient, type, ingredientManager);
				return new IllegalArgumentException(String.format("Ingredient is invalid and cannot be used as a drawable ingredient: %s", info));
			});
		return new DrawableIngredient<>(typedIngredient, ingredientRenderer);
	}

	@Override
	public <V> IDrawable createDrawableIngredient(ITypedIngredient<V> ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		IIngredientType<V> type = ingredient.getType();
		IIngredientRenderer<V> ingredientRenderer = ingredientManager.getIngredientRenderer(type);
		return new DrawableIngredient<>(ingredient, ingredientRenderer);
	}

	@Override
	public ICraftingGridHelper createCraftingGridHelper() {
		return CraftingGridHelper.INSTANCE;
	}

	@SuppressWarnings("removal")
	@Override
	public mezz.jei.api.gui.widgets.IScrollGridWidgetFactory<?> createScrollGridFactory(int columns, int visibleRows) {
		return new ScrollGridWidgetFactory<>(columns, visibleRows);
	}

	@SuppressWarnings("removal")
	@Override
	public IScrollBoxWidget createScrollBoxWidget(IDrawable contents, int visibleHeight, int xPos, int yPos) {
		ScrollBoxRecipeWidget widget = new ScrollBoxRecipeWidget(contents.getWidth() + getScrollBoxScrollbarExtraWidth(), visibleHeight, xPos, yPos);
		widget.setContents(contents);
		return widget;
	}

	@Override
	public IScrollBoxWidget createScrollBoxWidget(int width, int height, int xPos, int yPos) {
		return new ScrollBoxRecipeWidget(width, height, xPos, yPos);
	}

	@SuppressWarnings("removal")
	@Override
	public int getScrollBoxScrollbarExtraWidth() {
		return AbstractScrollWidget.getScrollBoxScrollbarExtraWidth();
	}

	@Override
	public ITickTimer createTickTimer(int ticksPerCycle, int maxValue, boolean countDown) {
		return new TickTimer(ticksPerCycle, maxValue, countDown);
	}
}
