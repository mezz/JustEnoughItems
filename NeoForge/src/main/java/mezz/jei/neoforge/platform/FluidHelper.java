package mezz.jei.neoforge.platform;

import com.mojang.serialization.Codec;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.library.render.FluidTankRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackContentsFactory;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class FluidHelper implements IPlatformFluidHelperInternal<FluidStack> {
	@Override
	public IIngredientTypeWithSubtypes<Fluid, FluidStack> getFluidIngredientType() {
		return NeoForgeTypes.FLUID_STACK;
	}

	@Override
	public IIngredientRenderer<FluidStack> createRenderer(long capacity, boolean showCapacity, int width, int height) {
		return new FluidTankRenderer<>(this, capacity, showCapacity, width, height);
	}

	@Override
	public int getColorTint(FluidStack ingredient) {
		Fluid fluid = ingredient.getFluid();
		Minecraft minecraft = Minecraft.getInstance();
		ModelManager modelManager = minecraft.getModelManager();
		FluidStateModelSet fluidStateModelSet = modelManager.getFluidStateModelSet();
		FluidModel fluidModel = fluidStateModelSet.get(fluid.defaultFluidState());
		FluidTintSource tintSource = fluidModel.fluidTintSource();
		if (tintSource == null) {
			return 0xFFFFFFFF;
		}
		return tintSource.colorAsStack(ingredient);
	}

	@Override
	public long getAmount(FluidStack ingredient) {
		return ingredient.getAmount();
	}

	@Override
	public FluidStack copyWithAmount(FluidStack ingredient, long amount) {
		FluidStack copy = ingredient.copy();
		int intAmount = Math.toIntExact(amount);
		copy.setAmount(intAmount);
		return copy;
	}

	@Override
	public DataComponentPatch getComponentsPatch(FluidStack ingredient) {
		return ingredient.getComponentsPatch();
	}

	@Override
	public List<Component> getTooltip(FluidStack ingredient, @Nullable Player player, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag) {
		Fluid fluid = ingredient.getFluid();
		if (fluid.isSame(Fluids.EMPTY)) {
			return List.of();
		}

		return ingredient.getTooltipLines(tooltipContext, player, tooltipFlag);
	}

	@Override
	public long bucketVolume() {
		return FluidType.BUCKET_VOLUME;
	}

	@Override
	public Optional<TextureAtlasSprite> getStillFluidSprite(FluidStack fluidStack) {
		Fluid fluid = fluidStack.getFluid();
		Minecraft minecraft = Minecraft.getInstance();
		ModelManager modelManager = minecraft.getModelManager();
		FluidStateModelSet fluidStateModelSet = modelManager.getFluidStateModelSet();
		FluidModel fluidModel = fluidStateModelSet.get(fluid.defaultFluidState());
		Material.Baked stillMaterial = fluidModel.stillMaterial();
		TextureAtlasSprite sprite = stillMaterial.sprite();
		// noinspection OptionalOfNullableMisuse
		return Optional.ofNullable(sprite)
			.filter(s -> s.atlasLocation() != MissingTextureAtlasSprite.getLocation());
	}

	@Override
	public Component getDisplayName(FluidStack ingredient) {
		Component displayName = ingredient.getHoverName();

		Fluid fluid = ingredient.getFluid();
		if (!fluid.isSource(fluid.defaultFluidState())) {
			return Component.translatable("jei.tooltip.liquid.flowing", displayName);
		}
		return displayName;
	}

	@Override
	public FluidStack create(Holder<Fluid> fluid, long amount, DataComponentPatch components) {
		int intAmount = (int) Math.min(amount, Integer.MAX_VALUE);
		return new FluidStack(fluid, intAmount, components);
	}

	@Override
	public FluidStack create(Holder<Fluid> fluid, long amount) {
		int intAmount = (int) Math.min(amount, Integer.MAX_VALUE);
		return new FluidStack(fluid, intAmount);
	}

	@Override
	public FluidStack copy(FluidStack ingredient) {
		return ingredient.copy();
	}

	@Override
	public FluidStack normalize(FluidStack ingredient) {
		if (ingredient.getAmount() == FluidType.BUCKET_VOLUME) {
			return ingredient;
		}
		return ingredient.copyWithAmount(FluidType.BUCKET_VOLUME);
	}

	@Override
	public Optional<FluidStack> getContainedFluid(ITypedIngredient<?> ingredient) {
		return ingredient.getItemStack()
			.map(FluidUtil::getFirstStackContained)
			.filter(i -> !i.isEmpty());
	}

	@Override
	public Codec<FluidStack> getCodec() {
		return FluidStack.fixedAmountCodec(FluidType.BUCKET_VOLUME);
	}

	@Override
	public Optional<DisplayContentsFactory<FluidStack>> getDisplayContentsFactoryForStacks() {
		return Optional.of(FluidStackContentsFactory.INSTANCE);
	}
}
