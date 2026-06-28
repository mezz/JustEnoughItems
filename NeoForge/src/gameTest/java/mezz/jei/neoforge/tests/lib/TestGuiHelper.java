package mezz.jei.neoforge.tests.lib;

import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.widgets.IScrollBoxWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.library.gui.helpers.CraftingGridHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

public final class TestGuiHelper implements IGuiHelper {
	public static final TestGuiHelper INSTANCE = new TestGuiHelper();

	private static final TestDrawable DRAWABLE = new TestDrawable(16, 16);

	private TestGuiHelper() {
	}

	@Override
	public IDrawableBuilder drawableBuilder(Identifier id, int u, int v, int width, int height) {
		return new TestDrawableBuilder(width, height);
	}

	@Override
	public IDrawableStatic createDrawableSprite(TextureAtlas textureAtlas, Identifier spriteId) {
		return DRAWABLE;
	}

	@Override
	public IScalableDrawable createScalableDrawableSprite(TextureAtlas textureAtlas, Identifier spriteId) {
		return (guiGraphics, x, y, width, height) -> {
		};
	}

	@Override
	public IDrawableAnimated createAnimatedDrawable(IDrawableStatic drawable, int ticksPerCycle, IDrawableAnimated.StartDirection startDirection, boolean inverted) {
		return DRAWABLE;
	}

	@Override
	public IDrawableAnimated createAnimatedDrawable(IDrawableStatic drawable, ITickTimer tickTimer, IDrawableAnimated.StartDirection startDirection) {
		return DRAWABLE;
	}

	@Override
	public IDrawableStatic getSlotDrawable() {
		return DRAWABLE;
	}

	@Override
	public IDrawableStatic getOutputSlot() {
		return DRAWABLE;
	}

	@Override
	public IDrawableStatic getRecipeArrow() {
		return DRAWABLE;
	}

	@Override
	public IDrawableStatic getRecipeArrowFilled() {
		return DRAWABLE;
	}

	@Override
	public IDrawableAnimated createAnimatedRecipeArrow(int ticksPerCycle) {
		return DRAWABLE;
	}

	@Override
	public IDrawableStatic getRecipePlusSign() {
		return DRAWABLE;
	}

	@Override
	public IDrawableStatic getRecipeFlameFilled() {
		return DRAWABLE;
	}

	@Override
	public IDrawableStatic getRecipeFlameEmpty() {
		return DRAWABLE;
	}

	@Override
	public IDrawableAnimated createAnimatedRecipeFlame(int ticksPerCycle) {
		return DRAWABLE;
	}

	@Override
	public IDrawableStatic createBlankDrawable(int width, int height) {
		return new TestDrawable(width, height);
	}

	@Override
	public <V> IDrawable createDrawableIngredient(IIngredientType<V> type, V ingredient) {
		return DRAWABLE;
	}

	@Override
	public <V> IDrawable createDrawableIngredient(ITypedIngredient<V> ingredient) {
		return DRAWABLE;
	}

	@Override
	public ICraftingGridHelper createCraftingGridHelper() {
		return CraftingGridHelper.INSTANCE;
	}

	@Override
	public IScrollBoxWidget createScrollBoxWidget(int width, int height, int xPos, int yPos) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ITickTimer createTickTimer(int ticksPerCycle, int maxValue, boolean countDown) {
		throw new UnsupportedOperationException();
	}

	private record TestDrawable(int width, int height) implements IDrawableStatic, IDrawableAnimated {
		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
		}

		@Override
		public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
		}
	}

	private record TestDrawableBuilder(int width, int height) implements IDrawableBuilder {
		@Override
		public IDrawableBuilder setTextureSize(int width, int height) {
			return this;
		}

		@Override
		public IDrawableBuilder addPadding(int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
			return this;
		}

		@Override
		public IDrawableBuilder trim(int trimTop, int trimBottom, int trimLeft, int trimRight) {
			return this;
		}

		@Override
		public IDrawableStatic build() {
			return new TestDrawable(width, height);
		}

		@Override
		public IDrawableAnimated buildAnimated(int ticksPerCycle, IDrawableAnimated.StartDirection startDirection, boolean inverted) {
			return new TestDrawable(width, height);
		}

		@Override
		public IDrawableAnimated buildAnimated(ITickTimer tickTimer, IDrawableAnimated.StartDirection startDirection) {
			return new TestDrawable(width, height);
		}
	}
}
