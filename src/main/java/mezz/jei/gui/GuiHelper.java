package mezz.jei.gui;

import com.google.common.base.Preconditions;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.config.Constants;
import mezz.jei.util.TickTimer;
import net.minecraft.util.ResourceLocation;

public class GuiHelper implements IGuiHelper {
	private final IDrawableStatic slotDrawable;
	private final IDrawableStatic tabSelected;
	private final IDrawableStatic tabUnselected;
	private final IDrawableStatic shapelessIcon;

	private final ResourceLocation recipeBackgroundResource;
	private final ResourceLocation recipeBackgroundTallResource;

	public GuiHelper() {
		ResourceLocation location = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");
		this.slotDrawable = createDrawable(location, 55, 16, 18, 18);

		recipeBackgroundResource = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_RECIPE_BACKGROUND_PATH);
		recipeBackgroundTallResource = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_RECIPE_BACKGROUND_TALL_PATH);

		tabSelected = createDrawable(recipeBackgroundResource, 196, 15, 24, 24);
		tabUnselected = createDrawable(recipeBackgroundResource, 220, 15, 24, 22);

		shapelessIcon = createDrawable(recipeBackgroundResource, 196, 0, 19, 15);
	}

	@Override
	public IDrawableStatic createDrawable(ResourceLocation resourceLocation, int u, int v, int width, int height) {
		Preconditions.checkNotNull(resourceLocation, "resourceLocation cannot be null");

		return new DrawableResource(resourceLocation, u, v, width, height, 0, 0, 0, 0, 256, 256);
	}

	@Override
	public IDrawableStatic createDrawable(ResourceLocation resourceLocation, int u, int v, int width, int height, int textureWidth, int textureHeight) {
		Preconditions.checkNotNull(resourceLocation, "resourceLocation cannot be null");

		return new DrawableResource(resourceLocation, u, v, width, height, 0, 0, 0, 0, textureWidth, textureHeight);
	}

	@Override
	public IDrawableStatic createDrawable(ResourceLocation resourceLocation, int u, int v, int width, int height, int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
		Preconditions.checkNotNull(resourceLocation, "resourceLocation cannot be null");

		return new DrawableResource(resourceLocation, u, v, width, height, paddingTop, paddingBottom, paddingLeft, paddingRight, 256, 256);
	}

	@Override
	public IDrawableAnimated createAnimatedDrawable(IDrawableStatic drawable, int ticksPerCycle, IDrawableAnimated.StartDirection startDirection, boolean inverted) {
		Preconditions.checkNotNull(drawable, "drawable cannot be null");
		Preconditions.checkNotNull(startDirection, "startDirection cannot be null");

		IDrawableAnimated.StartDirection animationStartDirection = startDirection;
		if (inverted) {
			if (startDirection == IDrawableAnimated.StartDirection.TOP) {
				animationStartDirection = IDrawableAnimated.StartDirection.BOTTOM;
			} else if (startDirection == IDrawableAnimated.StartDirection.BOTTOM) {
				animationStartDirection = IDrawableAnimated.StartDirection.TOP;
			} else if (startDirection == IDrawableAnimated.StartDirection.LEFT) {
				animationStartDirection = IDrawableAnimated.StartDirection.RIGHT;
			} else {
				animationStartDirection = IDrawableAnimated.StartDirection.LEFT;
			}
		}

		int tickTimerMaxValue;
		if (animationStartDirection == IDrawableAnimated.StartDirection.TOP || animationStartDirection == IDrawableAnimated.StartDirection.BOTTOM) {
			tickTimerMaxValue = drawable.getHeight();
		} else {
			tickTimerMaxValue = drawable.getWidth();
		}
		ITickTimer tickTimer = createTickTimer(ticksPerCycle, tickTimerMaxValue, !inverted);
		return new DrawableAnimated(drawable, tickTimer, animationStartDirection);
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

	public ResourceLocation getRecipeBackgroundResource() {
		return recipeBackgroundResource;
	}

	public ResourceLocation getRecipeBackgroundTallResource() {
		return recipeBackgroundTallResource;
	}
}
