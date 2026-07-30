package mezz.jei.library.render;

import com.google.common.base.Preconditions;
import mezz.jei.api.gui.drawable.TilingDirection;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class FluidTankRenderer<T> implements IIngredientRenderer<T> {
	private static final NumberFormat nf = NumberFormat.getIntegerInstance();
	private static final int MIN_FLUID_HEIGHT = 1; // ensure tiny amounts of fluid are still visible

	private final IPlatformFluidHelperInternal<T> fluidHelper;
	private final IIngredientTypeWithSubtypes<Fluid, T> type;
	private final long capacity;
	private final TooltipMode tooltipMode;
	private final int width;
	private final int height;
	private final TilingDirection tilingDirection;

	enum TooltipMode {
		SHOW_AMOUNT,
		SHOW_AMOUNT_AND_CAPACITY,
		ITEM_LIST
	}

	public FluidTankRenderer(IPlatformFluidHelperInternal<T> fluidHelper) {
		this(fluidHelper, fluidHelper.getFluidIngredientType(), fluidHelper.bucketVolume(), TooltipMode.ITEM_LIST, 16, 16, TilingDirection.UP_RIGHT);
	}

	public FluidTankRenderer(
		IPlatformFluidHelperInternal<T> fluidHelper,
		IIngredientTypeWithSubtypes<Fluid, T> type,
		long capacity,
		boolean showCapacity,
		int width,
		int height,
		TilingDirection tilingDirection
	) {
		this(
			fluidHelper,
			type,
			capacity,
			getTooltipMode(showCapacity),
			width,
			height,
			tilingDirection
		);
	}

	private static TooltipMode getTooltipMode(boolean showCapacity) {
		if (showCapacity) {
			return TooltipMode.SHOW_AMOUNT_AND_CAPACITY;
		}
		return TooltipMode.SHOW_AMOUNT;
	}

	private FluidTankRenderer(
		IPlatformFluidHelperInternal<T> fluidHelper,
		IIngredientTypeWithSubtypes<Fluid, T> type,
		long capacity,
		TooltipMode tooltipMode,
		int width,
		int height,
		TilingDirection tilingDirection
	) {
		Preconditions.checkArgument(capacity > 0, "capacity must be > 0");
		Preconditions.checkArgument(width > 0, "width must be > 0");
		Preconditions.checkArgument(height > 0, "height must be > 0");
		Preconditions.checkNotNull(type, "type");
		Preconditions.checkNotNull(tilingDirection, "tilingDirection");
		this.fluidHelper = fluidHelper;
		this.type = type;
		this.capacity = capacity;
		this.tooltipMode = tooltipMode;
		this.width = width;
		this.height = height;
		this.tilingDirection = tilingDirection;
	}

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, T fluidStack) {
		render(guiGraphics, fluidStack, 0, 0);
	}

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, T ingredient, int posX, int posY) {
		Fluid fluid = type.getBase(ingredient);
		if (fluid.isSame(Fluids.EMPTY)) {
			return;
		}

		fluidHelper.getStillFluidSprite(ingredient)
			.ifPresent(fluidStillSprite -> {
				int fluidColor = fluidHelper.getColorTint(ingredient);

				long amount = fluidHelper.getAmount(ingredient);
				if (amount > 0) {
					long longScaledAmount = (amount * height) / capacity;
					int scaledAmount = Math.clamp(longScaledAmount, MIN_FLUID_HEIGHT, height);
					drawTiledSprite(
						guiGraphics,
						width,
						height,
						fluidColor,
						scaledAmount,
						fluidStillSprite,
						tilingDirection,
						posX,
						posY
					);
				}
			});
	}

	private static void drawTiledSprite(
		GuiGraphicsExtractor guiGraphics,
		final int tiledWidth,
		final int tiledHeight,
		int color,
		int scaledAmount,
		TextureAtlasSprite sprite,
		TilingDirection tilingDirection,
		int posX,
		int posY
	) {
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		SpriteContents spriteContents = sprite.contents();
		int spriteWidth = spriteContents.width();
		int spriteHeight = spriteContents.height();
		GuiSpriteScaling.Tile tileScaling = new GuiSpriteScaling.Tile(spriteWidth, spriteHeight);

		posY = posY + tiledHeight - scaledAmount;
		int xShift = getXShift(tilingDirection, tiledWidth, spriteWidth);
		int yShift = getYShift(tilingDirection, scaledAmount, spriteHeight);

		guiGraphics.enableScissor(posX, posY, posX + tiledWidth, posY + scaledAmount);
		{
			renderHelper.blitTiledSprite(
				guiGraphics,
				RenderPipelines.GUI_TEXTURED,
				sprite,
				tileScaling,
				posX - xShift,
				posY - yShift,
				tiledWidth + xShift,
				scaledAmount + yShift,
				color
			);
		}
		guiGraphics.disableScissor();
	}

	private static int getXShift(TilingDirection tilingDirection, int desiredWidth, int spriteWidth) {
		return switch (tilingDirection) {
			case DOWN_RIGHT, UP_RIGHT -> 0;
			case DOWN_LEFT, UP_LEFT -> getShift(desiredWidth, spriteWidth);
		};
	}

	private static int getYShift(TilingDirection tilingDirection, int desiredHeight, int spriteHeight) {
		return switch (tilingDirection) {
			case DOWN_RIGHT, DOWN_LEFT -> 0;
			case UP_RIGHT, UP_LEFT -> getShift(desiredHeight, spriteHeight);
		};
	}

	private static int getShift(int desired, int sprite) {
		int remainder = desired % sprite;
		if (remainder == 0) {
			return 0;
		}
		return sprite - remainder;
	}

	@Override
	public List<Component> getTooltip(T fluidStack, TooltipFlag tooltipFlag) {
		Fluid fluidType = type.getBase(fluidStack);
		if (fluidType.isSame(Fluids.EMPTY)) {
			return new ArrayList<>();
		}

		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		Item.TooltipContext tooltipContext = Item.TooltipContext.of(minecraft.level);
		List<Component> tooltip = new ArrayList<>(fluidHelper.getTooltip(fluidStack, player, tooltipContext, tooltipFlag));

		long amount = fluidHelper.getAmount(fluidStack);
		long milliBuckets = (amount * 1000) / fluidHelper.bucketVolume();

		if (tooltipMode == TooltipMode.SHOW_AMOUNT_AND_CAPACITY) {
			MutableComponent amountString = Component.translatable("jei.tooltip.liquid.amount.with.capacity", nf.format(milliBuckets), nf.format(capacity));
			tooltip.add(amountString.withStyle(ChatFormatting.GRAY));
		} else if (tooltipMode == TooltipMode.SHOW_AMOUNT) {
			MutableComponent amountString = Component.translatable("jei.tooltip.liquid.amount", nf.format(milliBuckets));
			tooltip.add(amountString.withStyle(ChatFormatting.GRAY));
		}

		return tooltip;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}
}
