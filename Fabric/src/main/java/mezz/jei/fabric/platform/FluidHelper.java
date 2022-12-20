package mezz.jei.fabric.platform;

import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.library.render.FluidTankRenderer;
import mezz.jei.fabric.ingredients.fluid.JeiFluidIngredient;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class FluidHelper implements IPlatformFluidHelperInternal<IJeiFluidIngredient> {
    @Override
    public IIngredientTypeWithSubtypes<Fluid, IJeiFluidIngredient> getFluidIngredientType() {
        return FabricTypes.FLUID_STACK;
    }

    @Override
    public IIngredientSubtypeInterpreter<IJeiFluidIngredient> getAllNbtSubtypeInterpreter() {
        return AllFluidNbt.INSTANCE;
    }

    @Override
    public IIngredientRenderer<IJeiFluidIngredient> createRenderer(long capacity, boolean showCapacity, int width, int height) {
        return new FluidTankRenderer<>(this, capacity, showCapacity, width, height);
    }

    @Override
    public Optional<TextureAtlasSprite> getStillFluidSprite(IJeiFluidIngredient ingredient) {
        FluidVariant fluidVariant = getFluidVariant(ingredient);
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluidVariant);
        return Optional.ofNullable(sprite);
    }

    @Override
    public Component getDisplayName(IJeiFluidIngredient ingredient) {
        FluidVariant fluidVariant = getFluidVariant(ingredient);
        return FluidVariantAttributes.getName(fluidVariant);
    }

    @Override
    public int getColorTint(IJeiFluidIngredient ingredient) {
        FluidVariant fluidVariant = getFluidVariant(ingredient);
        int fluidColor = FluidVariantRendering.getColor(fluidVariant);
        return fluidColor | 0xFF000000;
    }

    @Override
    public List<Component> getTooltip(IJeiFluidIngredient ingredient, TooltipFlag tooltipFlag) {
        FluidVariant fluidVariant = getFluidVariant(ingredient);
        return FluidVariantRendering.getTooltip(fluidVariant, tooltipFlag);
    }

    private FluidVariant getFluidVariant(IJeiFluidIngredient ingredient) {
        Fluid fluid = ingredient.getFluid();
        CompoundTag tag = ingredient.getTag().orElse(null);
        return FluidVariant.of(fluid, tag);
    }

    @Override
    public long getAmount(IJeiFluidIngredient ingredient) {
        return ingredient.getAmount();
    }

    @Override
    public Optional<CompoundTag> getTag(IJeiFluidIngredient ingredient) {
        return ingredient.getTag();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public long bucketVolume() {
        return FluidConstants.BUCKET;
    }

    @Override
    public IJeiFluidIngredient create(Fluid fluid, long amount, @Nullable CompoundTag tag) {
        return new JeiFluidIngredient(fluid, amount, tag);
    }

    @Override
    public IJeiFluidIngredient create(Fluid fluid, long amount) {
        return new JeiFluidIngredient(fluid, amount);
    }

    @Override
    public IJeiFluidIngredient copy(IJeiFluidIngredient ingredient) {
        CompoundTag tag = ingredient.getTag().orElse(null);
        return new JeiFluidIngredient(ingredient.getFluid(), ingredient.getAmount(), tag);
    }

    @Override
    public IJeiFluidIngredient normalize(IJeiFluidIngredient ingredient) {
        CompoundTag tag = ingredient.getTag().orElse(null);
        return new JeiFluidIngredient(ingredient.getFluid(), bucketVolume(), tag);
    }

    private static class AllFluidNbt implements IIngredientSubtypeInterpreter<IJeiFluidIngredient> {
        public static final AllFluidNbt INSTANCE = new AllFluidNbt();

        private AllFluidNbt() {
        }

        @Override
        public String apply(IJeiFluidIngredient storage, UidContext context) {
            return storage.getTag()
                .filter(tag -> !tag.isEmpty())
                .map(CompoundTag::toString)
                .orElse(IIngredientSubtypeInterpreter.NONE);
        }
    }
}
