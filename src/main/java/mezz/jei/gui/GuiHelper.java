package mezz.jei.gui;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableBuilder;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.Constants;
import mezz.jei.gui.elements.DrawableAnimated;
import mezz.jei.gui.elements.DrawableBlank;
import mezz.jei.gui.elements.DrawableBuilder;
import mezz.jei.gui.elements.DrawableIngredient;
import mezz.jei.util.ErrorUtil;
import net.minecraft.util.ResourceLocation;

public class GuiHelper implements IGuiHelper {
	private final IIngredientRegistry ingredientRegistry;
	private final IDrawableStatic slotDrawable;
	private final IDrawableStatic tabSelected;
	private final IDrawableStatic tabUnselected;
	private final IDrawableStatic shapelessIcon;
	private final IDrawableStatic arrowPrevious;
	private final IDrawableStatic arrowNext;
	private final IDrawableStatic recipeTransfer;

	public GuiHelper(IIngredientRegistry ingredientRegistry) {
		this.ingredientRegistry = ingredientRegistry;
		slotDrawable = createDrawable(Constants.RECIPE_BACKGROUND, 196, 93, 18, 18);

		tabSelected = createDrawable(Constants.RECIPE_BACKGROUND, 196, 15, 24, 24);
		tabUnselected = createDrawable(Constants.RECIPE_BACKGROUND, 220, 15, 24, 22);

		shapelessIcon = createDrawable(Constants.RECIPE_BACKGROUND, 196, 128, 16, 15);

		arrowPrevious = drawableBuilder(Constants.RECIPE_BACKGROUND, 196, 55, 5, 8)
			.addPadding(1, 0, 0, 0)
			.build();
		arrowNext = drawableBuilder(Constants.RECIPE_BACKGROUND, 204, 55, 5, 8)
			.addPadding(1, 0, 1, 0)
			.build();
		recipeTransfer = drawableBuilder(Constants.RECIPE_BACKGROUND, 212, 55, 6, 6)
			.addPadding(1, 0, 1, 0)
			.build();
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
	public IDrawableStatic getSlotDrawable() {
		return slotDrawable;
	}

	@Override
	public IDrawableStatic createBlankDrawable(int width, int height) {
		return new DrawableBlank(width, height);
	}

	@Override
	public <V> IDrawable createDrawableIngredient(V ingredient) {
		IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredient);
		return new DrawableIngredient<>(ingredient, ingredientRenderer);
	}

	@Override
	public ICraftingGridHelper createCraftingGridHelper(int craftInputSlot1, int craftOutputSlot) {
		return new CraftingGridHelper(craftInputSlot1, craftOutputSlot);
	}

	@Override
	public ITickTimer createTickTimer(int ticksPerCycle, int maxValue, boolean countDown) {
		return new TickTimer(ticksPerCycle, maxValue, countDown);
	}

	public IDrawableStatic getTabSelected() {
		return tabSelected;
	}

	public IDrawableStatic getTabUnselected() {
		return tabUnselected;
	}

	public IDrawableStatic getShapelessIcon() {
		return shapelessIcon;
	}

	public IDrawableStatic getArrowPrevious() {
		return arrowPrevious;
	}

	public IDrawableStatic getArrowNext() {
		return arrowNext;
	}

	public IDrawableStatic getRecipeTransfer() {
		return recipeTransfer;
	}
}
