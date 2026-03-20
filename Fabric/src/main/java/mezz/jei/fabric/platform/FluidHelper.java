package mezz.jei.fabric.platform;

import com.mojang.serialization.Codec;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.fabric.ingredients.fluid.JeiFluidIngredient;
import mezz.jei.library.render.FluidTankRenderer;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
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
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class FluidHelper implements IPlatformFluidHelperInternal<IJeiFluidIngredient> {
	private static final Codec<IJeiFluidIngredient> NORMALIZED_CODEC = Codec.lazyInitialized(() -> {
		return FluidVariant.CODEC.xmap(
			fluidVariant -> {
				return new JeiFluidIngredient(fluidVariant, FluidConstants.BUCKET);
			},
			IJeiFluidIngredient::getFluidVariant
		);
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
	public Optional<TextureAtlasSprite> getStillFluidSprite(IJeiFluidIngredient ingredient) {
		FluidVariant fluidVariant = ingredient.getFluidVariant();
		Fluid fluid = fluidVariant.getFluid();
		Minecraft minecraft = Minecraft.getInstance();
		ModelManager modelManager = minecraft.getModelManager();
		FluidStateModelSet fluidStateModelSet = modelManager.getFluidStateModelSet();
		FluidModel fluidModel = fluidStateModelSet.get(fluid.defaultFluidState());
		Material.Baked stillMaterial = fluidModel.stillMaterial();
		TextureAtlasSprite sprite = stillMaterial.sprite();
		return Optional.of(sprite);
	}

	@Override
	public Component getDisplayName(IJeiFluidIngredient ingredient) {
		FluidVariant fluidVariant = ingredient.getFluidVariant();
		Component displayName = FluidVariantAttributes.getName(fluidVariant);

		Fluid fluid = ingredient.getFluidVariant().getFluid();
		if (!fluid.isSource(fluid.defaultFluidState())) {
			return Component.translatable("jei.tooltip.liquid.flowing", displayName);
		}
		return displayName;
	}

	@Override
	public int getColorTint(IJeiFluidIngredient ingredient) {
		FluidVariant fluidVariant = ingredient.getFluidVariant();
		int fluidColor = FluidVariantRendering.getColor(fluidVariant);
		return fluidColor | 0xFF000000;
	}

	@Override
	public List<Component> getTooltip(IJeiFluidIngredient ingredient, @Nullable Player player, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag) {
		FluidVariant fluidVariant = ingredient.getFluidVariant();
		return FluidVariantRendering.getTooltip(fluidVariant, tooltipFlag);
	}

	@Override
	public long getAmount(IJeiFluidIngredient ingredient) {
		return ingredient.getAmount();
	}

	@Override
	public DataComponentPatch getComponentsPatch(IJeiFluidIngredient ingredient) {
		FluidVariant fluid = ingredient.getFluidVariant();
		return fluid.getComponentsPatch();
	}

	@Override
	public long bucketVolume() {
		return FluidConstants.BUCKET;
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
		return NORMALIZED_CODEC;
	}

	@Override
	public Optional<DisplayContentsFactory<IJeiFluidIngredient>> getDisplayContentsFactoryForStacks() {
		return Optional.empty();
	}
}
