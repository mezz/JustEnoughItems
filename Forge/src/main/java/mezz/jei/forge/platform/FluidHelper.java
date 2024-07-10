package mezz.jei.forge.platform;

import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.library.render.FluidTankRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FluidHelper implements IPlatformFluidHelperInternal<FluidStack> {
	@Override
	public IIngredientTypeWithSubtypes<Fluid, FluidStack> getFluidIngredientType() {
		return ForgeTypes.FLUID_STACK;
	}

	@Override
	public IIngredientRenderer<FluidStack> createRenderer(long capacity, boolean showCapacity, int width, int height) {
		return new FluidTankRenderer<>(this, capacity, showCapacity, width, height);
	}

	@Override
	public int getColorTint(FluidStack ingredient) {
		Fluid fluid = ingredient.getFluid();
		IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid);
		return renderProperties.getTintColor(ingredient);
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
		// TODO: update when Forge has FluidStack DataComponents
		return DataComponentPatch.EMPTY;
	}

	@Override
	public List<Component> getTooltip(FluidStack ingredient, TooltipFlag tooltipFlag) {
		List<Component> tooltip = new ArrayList<>();
		Fluid fluid = ingredient.getFluid();
		if (fluid.isSame(Fluids.EMPTY)) {
			return tooltip;
		}

		Component displayName = getDisplayName(ingredient);
		tooltip.add(displayName);

		if (tooltipFlag.isAdvanced()) {
			ResourceLocation resourceLocation = ForgeRegistries.FLUIDS.getKey(fluid);
			if (resourceLocation != null) {
				MutableComponent advancedId = Component.literal(resourceLocation.toString())
					.withStyle(ChatFormatting.DARK_GRAY);
				tooltip.add(advancedId);
			}
		}

		return tooltip;
	}

	@Override
	public long bucketVolume() {
		return FluidType.BUCKET_VOLUME;
	}

	@Override
	public Optional<TextureAtlasSprite> getStillFluidSprite(FluidStack fluidStack) {
		Fluid fluid = fluidStack.getFluid();
		IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid);
		ResourceLocation fluidStill = renderProperties.getStillTexture(fluidStack);
		return Optional.ofNullable(fluidStill)
			.map(f -> Minecraft.getInstance()
				.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
				.apply(f)
			)
			.filter(s -> s.atlasLocation() != MissingTextureAtlasSprite.getLocation());
	}

	@Override
	public Component getDisplayName(FluidStack ingredient) {
		return ingredient.getDisplayName();
	}

	@Override
	public FluidStack create(Holder<Fluid> fluid, long amount) {
		int intAmount = (int) Math.min(amount, Integer.MAX_VALUE);
		return new FluidStack(fluid.value(), intAmount);
	}

	@Override
	public FluidStack create(Holder<Fluid> fluid, long amount, DataComponentPatch components) {
		// TODO: update when Forge has FluidStack DataComponents
		int intAmount = (int) Math.min(amount, Integer.MAX_VALUE);
		return new FluidStack(fluid.value(), intAmount);
	}

	@Override
	public FluidStack copy(FluidStack ingredient) {
		return ingredient.copy();
	}

	@Override
	public FluidStack normalize(FluidStack ingredient) {
		FluidStack copy = this.copy(ingredient);
		copy.setAmount(FluidType.BUCKET_VOLUME);
		return copy;
	}

	@Override
	public Optional<FluidStack> getContainedFluid(ITypedIngredient<?> ingredient) {
		// TODO: update when Forge has item capabilities for fluid containers
		return Optional.empty();
	}
}
