package mezz.jei.gui;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.util.TickTimer;

public class GuiHelper implements IGuiHelper {
	private final IDrawableStatic slotDrawable;

	public GuiHelper() {
		ResourceLocation location = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");
		slotDrawable = createDrawable(location, 55, 16, 18, 18);
	}

	@Nonnull
	@Override
	public IDrawableStatic createDrawable(@Nonnull ResourceLocation resourceLocation, int u, int v, int width, int height) {
		return new DrawableResource(resourceLocation, u, v, width, height);
	}

	@Nonnull
	@Override
	public IDrawableStatic createDrawable(@Nonnull ResourceLocation resourceLocation, int u, int v, int width, int height, int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
		return new DrawableResource(resourceLocation, u, v, width, height, paddingTop, paddingBottom, paddingLeft, paddingRight);
	}

	@Nonnull
	@Override
	public IDrawableAnimated createAnimatedDrawable(@Nonnull IDrawableStatic drawable, int ticksPerCycle, @Nonnull IDrawableAnimated.StartDirection startDirection, boolean inverted) {
		return new DrawableAnimated(drawable, ticksPerCycle, startDirection, inverted);
	}

	@Nonnull
	@Override
	public IDrawableStatic getSlotDrawable() {
		return slotDrawable;
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
