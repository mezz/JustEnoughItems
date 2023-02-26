package mezz.jei.library.gui;

import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableAnimated;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.common.gui.elements.DrawableIngredient;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.TickTimer;
import mezz.jei.library.gui.elements.DrawableBuilder;
import net.minecraft.resources.ResourceLocation;

public class GuiHelper implements IGuiHelper {
	private final IIngredientManager ingredientManager;
	private final IDrawableStatic slotDrawable;

	public GuiHelper(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
		Textures textures = Internal.getTextures();
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
		IIngredientRenderer<V> ingredientRenderer = ingredientManager.getIngredientRenderer(type);
		return new DrawableIngredient<>(ingredient, ingredientRenderer);
	}

	@Override
	public ICraftingGridHelper createCraftingGridHelper() {
		return new CraftingGridHelper();
	}

	@Override
	public ITickTimer createTickTimer(int ticksPerCycle, int maxValue, boolean countDown) {
		return new TickTimer(ticksPerCycle, maxValue, countDown);
	}
}
