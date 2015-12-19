package mezz.jei.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.util.Log;
import mezz.jei.util.TickTimer;

public class GuiHelper implements IGuiHelper {
	private final IDrawableStatic slotDrawable;

	public GuiHelper() {
		ResourceLocation location = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");
		slotDrawable = createDrawable(location, 55, 16, 18, 18);
	}

	@Nonnull
	@Override
	public IDrawableStatic createDrawable(@Nullable ResourceLocation resourceLocation, int u, int v, int width, int height) {
		if (resourceLocation == null) {
			Log.error("Null resourceLocation, returning blank drawable", new NullPointerException());
			return createBlankDrawable(width, height);
		}
		return new DrawableResource(resourceLocation, u, v, width, height);
	}

	@Nonnull
	@Override
	public IDrawableStatic createDrawable(@Nullable ResourceLocation resourceLocation, int u, int v, int width, int height, int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
		if (resourceLocation == null) {
			Log.error("Null resourceLocation, returning blank drawable", new NullPointerException());
			return createBlankDrawable(width, height);
		}
		return new DrawableResource(resourceLocation, u, v, width, height, paddingTop, paddingBottom, paddingLeft, paddingRight);
	}

	@Nonnull
	@Override
	public IDrawableAnimated createAnimatedDrawable(@Nullable IDrawableStatic drawable, int ticksPerCycle, @Nullable IDrawableAnimated.StartDirection startDirection, boolean inverted) {
		if (drawable == null) {
			Log.error("Null drawable, returning blank drawable", new NullPointerException());
			return new DrawableBlank(0, 0);
		}
		if (startDirection == null) {
			Log.error("Null startDirection, defaulting to Top", new NullPointerException());
			startDirection = IDrawableAnimated.StartDirection.TOP;
		}

		if (inverted) {
			if (startDirection == IDrawableAnimated.StartDirection.TOP) {
				startDirection = IDrawableAnimated.StartDirection.BOTTOM;
			} else if (startDirection == IDrawableAnimated.StartDirection.BOTTOM) {
				startDirection = IDrawableAnimated.StartDirection.TOP;
			} else if (startDirection == IDrawableAnimated.StartDirection.LEFT) {
				startDirection = IDrawableAnimated.StartDirection.RIGHT;
			} else {
				startDirection = IDrawableAnimated.StartDirection.LEFT;
			}
		}

		int tickTimerMaxValue;
		if (startDirection == IDrawableAnimated.StartDirection.TOP || startDirection == IDrawableAnimated.StartDirection.BOTTOM) {
			tickTimerMaxValue = drawable.getHeight();
		} else {
			tickTimerMaxValue = drawable.getWidth();
		}
		ITickTimer tickTimer = createTickTimer(ticksPerCycle, tickTimerMaxValue, !inverted);
		return new DrawableAnimated(drawable, tickTimer, startDirection);
	}

	@Nonnull
	@Override
	public IDrawableStatic getSlotDrawable() {
		return slotDrawable;
	}

	@Nonnull
	@Override
	public IDrawableStatic createBlankDrawable(int width, int height) {
		return new DrawableBlank(width, height);
	}

	@Nonnull
	@Override
	public ICraftingGridHelper createCraftingGridHelper(int craftInputSlot1, int craftOutputSlot) {
		return new CraftingGridHelper(craftInputSlot1, craftOutputSlot);
	}

	@Nonnull
	@Override
	public ITickTimer createTickTimer(int ticksPerCycle, int maxValue, boolean countDown) {
		return new TickTimer(ticksPerCycle, maxValue, countDown);
	}
}
