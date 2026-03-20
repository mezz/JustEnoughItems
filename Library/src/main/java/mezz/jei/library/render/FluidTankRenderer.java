package mezz.jei.library.render;

import com.google.common.base.Preconditions;
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
	private final long capacity;
	private final TooltipMode tooltipMode;
	private final int width;
	private final int height;

	enum TooltipMode {
		SHOW_AMOUNT,
		SHOW_AMOUNT_AND_CAPACITY,
		ITEM_LIST
	}

	public FluidTankRenderer(IPlatformFluidHelperInternal<T> fluidHelper) {
		this(fluidHelper, fluidHelper.bucketVolume(), TooltipMode.ITEM_LIST, 16, 16);
	}

	public FluidTankRenderer(IPlatformFluidHelperInternal<T> fluidHelper, long capacity, boolean showCapacity, int width, int height) {
		this(fluidHelper, capacity, showCapacity ? TooltipMode.SHOW_AMOUNT_AND_CAPACITY : TooltipMode.SHOW_AMOUNT, width, height);
	}

	private FluidTankRenderer(IPlatformFluidHelperInternal<T> fluidHelper, long capacity, TooltipMode tooltipMode, int width, int height) {
		Preconditions.checkArgument(capacity > 0, "capacity must be > 0");
		Preconditions.checkArgument(width > 0, "width must be > 0");
		Preconditions.checkArgument(height > 0, "height must be > 0");
		this.fluidHelper = fluidHelper;
		this.capacity = capacity;
		this.tooltipMode = tooltipMode;
		this.width = width;
		this.height = height;
	}

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, T fluidStack) {
		render(guiGraphics, fluidStack, 0, 0);
	}

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, T ingredient, int posX, int posY) {
		IIngredientTypeWithSubtypes<Fluid, T> type = fluidHelper.getFluidIngredientType();
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
					drawTiledSprite(guiGraphics, width, height, fluidColor, scaledAmount, fluidStillSprite, posX, posY);
				}
			});
	}

	private static void drawTiledSprite(GuiGraphicsExtractor guiGraphics, final int tiledWidth, final int tiledHeight, int color, int scaledAmount, TextureAtlasSprite sprite, int posX, int posY) {
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		SpriteContents spriteContents = sprite.contents();
		GuiSpriteScaling.Tile tileScaling = new GuiSpriteScaling.Tile(spriteContents.width(), spriteContents.height());

		posY = posY + tiledHeight - scaledAmount;

		guiGraphics.enableScissor(posX, posY, posX + tiledWidth, posY + scaledAmount);
		{
			renderHelper.blitTiledSprite(
				guiGraphics,
				RenderPipelines.GUI_TEXTURED,
				sprite,
				tileScaling,
				posX,
				posY,
				tiledWidth,
				scaledAmount,
				color
			);
		}
		guiGraphics.disableScissor();
	}

	@Override
	public List<Component> getTooltip(T fluidStack, TooltipFlag tooltipFlag) {
		IIngredientTypeWithSubtypes<Fluid, T> type = fluidHelper.getFluidIngredientType();
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
