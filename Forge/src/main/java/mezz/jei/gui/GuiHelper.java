package mezz.jei.gui;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.ingredients.RegisteredIngredients;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.gui.elements.DrawableAnimated;
import mezz.jei.gui.elements.DrawableBlank;
import mezz.jei.gui.elements.DrawableBuilder;
import mezz.jei.gui.elements.DrawableIngredient;
import mezz.jei.gui.textures.Textures;
import mezz.jei.util.ErrorUtil;

public class GuiHelper implements IGuiHelper {
	private final RegisteredIngredients registeredIngredients;
	private final IDrawableStatic slotDrawable;

	public GuiHelper(RegisteredIngredients registeredIngredients, Textures textures) {
		this.registeredIngredients = registeredIngredients;
		this.slotDrawable = textures.getSlotDrawable();
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
	public <V> IDrawable createDrawableIngredient(IIngredientType<V> type, V ingredient) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		IIngredientRenderer<V> ingredientRenderer = registeredIngredients.getIngredientRenderer(type);
		return new DrawableIngredient<>(ingredient, ingredientRenderer);
	}

	@SuppressWarnings("removal")
	@Override
	public <V> IDrawable createDrawableIngredient(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		IIngredientType<V> ingredientType = registeredIngredients.getIngredientType(ingredient);
		IIngredientRenderer<V> ingredientRenderer = registeredIngredients.getIngredientRenderer(ingredientType);
		return new DrawableIngredient<>(ingredient, ingredientRenderer);
	}

	@Override
	public ICraftingGridHelper createCraftingGridHelper(int craftInputSlot1) {
		return new CraftingGridHelper(craftInputSlot1);
	}

	@Override
	public ITickTimer createTickTimer(int ticksPerCycle, int maxValue, boolean countDown) {
		return new TickTimer(ticksPerCycle, maxValue, countDown);
	}
}
