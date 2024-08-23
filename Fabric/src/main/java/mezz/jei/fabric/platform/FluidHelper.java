package mezz.jei.fabric.platform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.fabric.ingredients.fluid.JeiFluidIngredient;
import mezz.jei.library.render.FluidSlotRenderer;
import mezz.jei.library.render.FluidTankRenderer;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class FluidHelper implements IPlatformFluidHelperInternal<IJeiFluidIngredient> {
	private static final Codec<Long> POSITIVE_LONG = Codec.LONG.validate((integer) -> {
		if (integer.compareTo((long) 1) >= 0 && integer.compareTo(Long.MAX_VALUE) <= 0) {
			return DataResult.success(integer);
		}
		return DataResult.error(() -> "Value must be positive: " + integer);
	});

	private static final Codec<IJeiFluidIngredient> CODEC = Codec.lazyInitialized(() -> {
		return RecordCodecBuilder.create((builder) -> {
			return builder.group(
					FluidVariant.CODEC.fieldOf("variant")
						.forGetter(IJeiFluidIngredient::getFluidVariant),
					POSITIVE_LONG.fieldOf("amount")
						.forGetter(IJeiFluidIngredient::getAmount)
				)
				.apply(builder, JeiFluidIngredient::new);
		});
	});

	@Override
	public IIngredientTypeWithSubtypes<Fluid, IJeiFluidIngredient> getFluidIngredientType() {
		return FabricTypes.FLUID_STACK;
	}

	@Override
	public IIngredientRenderer<IJeiFluidIngredient> createRenderer(long capacity, boolean showCapacity, int width, int height) {
		return new FluidTankRenderer<>(this, capacity, showCapacity, width, height);
	}

	@Override
	public IIngredientRenderer<IJeiFluidIngredient> createSlotRenderer(long capacity) {
		return new FluidSlotRenderer<>(this, capacity);
	}

	@Override
	public Optional<TextureAtlasSprite> getStillFluidSprite(IJeiFluidIngredient ingredient) {
		FluidVariant fluidVariant = ingredient.getFluidVariant();
		TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluidVariant);
		return Optional.ofNullable(sprite);
	}

	@Override
	public Component getDisplayName(IJeiFluidIngredient ingredient) {
		FluidVariant fluidVariant = ingredient.getFluidVariant();
		return FluidVariantAttributes.getName(fluidVariant);
	}

	@Override
	public int getColorTint(IJeiFluidIngredient ingredient) {
		FluidVariant fluidVariant = ingredient.getFluidVariant();
		int fluidColor = FluidVariantRendering.getColor(fluidVariant);
		return fluidColor | 0xFF000000;
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, IJeiFluidIngredient ingredient, TooltipFlag tooltipFlag) {
		FluidVariant fluidVariant = ingredient.getFluidVariant();
		List<Component> components = FluidVariantRendering.getTooltip(fluidVariant, tooltipFlag);
		tooltip.addAll(components);
	}

	@Override
	public long getAmount(IJeiFluidIngredient ingredient) {
		return ingredient.getAmount();
	}

	@Override
	public DataComponentPatch getComponentsPatch(IJeiFluidIngredient ingredient) {
		FluidVariant fluid = ingredient.getFluidVariant();
		return fluid.getComponents();
	}

	@Override
	public long bucketVolume() {
		return FluidConstants.BUCKET;
	}

	@Override
	public String unit() {
		return "dl";
	}

	@Override
	public IJeiFluidIngredient create(Holder<Fluid> fluid, long amount, DataComponentPatch patch) {
		FluidVariant fluidVariant = FluidVariant.of(fluid.value(), patch);
		return new JeiFluidIngredient(fluidVariant, amount);
	}

	@Override
	public IJeiFluidIngredient create(Holder<Fluid> fluid, long amount) {
		FluidVariant fluidVariant = FluidVariant.of(fluid.value());
		return new JeiFluidIngredient(fluidVariant, amount);
	}

	@Override
	public IJeiFluidIngredient copy(IJeiFluidIngredient ingredient) {
		return new JeiFluidIngredient(ingredient.getFluidVariant(), ingredient.getAmount());
	}

	@Override
	public IJeiFluidIngredient normalize(IJeiFluidIngredient ingredient) {
		if (ingredient.getAmount() == bucketVolume()) {
			return ingredient;
		}
		return new JeiFluidIngredient(ingredient.getFluidVariant(), bucketVolume());
	}

	@Override
	public Optional<IJeiFluidIngredient> getContainedFluid(ITypedIngredient<?> ingredient) {
		return ingredient.getItemStack()
			.map(ContainerItemContext::withConstant)
			.map(c -> c.find(FluidStorage.ITEM))
			.map(Storage::iterator)
			.filter(Iterator::hasNext)
			.map(Iterator::next)
			.map(view -> {
				FluidVariant resource = view.getResource();
				return new JeiFluidIngredient(resource, view.getAmount());
			});
	}

	@Override
	public IJeiFluidIngredient copyWithAmount(IJeiFluidIngredient ingredient, long amount) {
		return new JeiFluidIngredient(ingredient.getFluidVariant(), amount);
	}

	@Override
	public Codec<IJeiFluidIngredient> getCodec() {
		return CODEC;
	}
}
