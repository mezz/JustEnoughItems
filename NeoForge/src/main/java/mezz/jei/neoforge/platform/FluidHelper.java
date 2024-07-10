package mezz.jei.neoforge.platform;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.library.render.FluidTankRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
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
		IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid);
		return renderProperties.getTintColor(ingredient);
	}

	@Override
	public long getAmount(FluidStack ingredient) {
		return ingredient.getAmount();
	}

	@Override
	public FluidStack setAmount(FluidStack ingredient, long amount) {
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
	public List<Component> getTooltip(FluidStack ingredient, TooltipFlag tooltipFlag) {
		List<Component> tooltip = new ArrayList<>();
		Fluid fluid = ingredient.getFluid();
		if (fluid.isSame(Fluids.EMPTY)) {
			return tooltip;
		}

		Component displayName = getDisplayName(ingredient);
		tooltip.add(displayName);

		if (tooltipFlag.isAdvanced()) {
			Registry<Fluid> fluidRegistry = RegistryUtil.getRegistry(Registries.FLUID);
			ResourceLocation resourceLocation = fluidRegistry.getKey(fluid);
			if (resourceLocation != null &&  resourceLocation != BuiltInRegistries.FLUID.getDefaultKey()) {
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
		//noinspection OptionalOfNullableMisuse
		return Optional.ofNullable(fluidStill)
			.map(f -> Minecraft.getInstance()
				.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
				.apply(f)
			)
			.filter(s -> s.atlasLocation() != MissingTextureAtlasSprite.getLocation());
	}

	@Override
	public Component getDisplayName(FluidStack ingredient) {
		return ingredient.getHoverName();
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
		FluidStack copy = this.copy(ingredient);
		copy.setAmount(FluidType.BUCKET_VOLUME);
		return copy;
	}

	@Override
	public Optional<FluidStack> getContainedFluid(ITypedIngredient<?> ingredient) {
		return ingredient.getItemStack()
			.flatMap(i -> Optional.ofNullable(i.getCapability(Capabilities.FluidHandler.ITEM)))
			.map(c -> c.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE));
	}
}
