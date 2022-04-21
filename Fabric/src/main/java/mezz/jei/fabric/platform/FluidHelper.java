package mezz.jei.fabric.platform;

import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.fabric.ingredients.fluid.JeiFluidIngredient;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.render.FluidTankRenderer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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
    public TextureAtlasSprite getStillFluidSprite(IJeiFluidIngredient ingredient) {
        FluidRenderHandlerRegistry registry = FluidRenderHandlerRegistry.INSTANCE;
        Fluid fluid = ingredient.getFluid();
        FluidRenderHandler handler = registry.get(fluid);
        FluidState defaultFluidState = fluid.defaultFluidState();
        TextureAtlasSprite[] fluidSprites = handler.getFluidSprites(null, null, defaultFluidState);
        return fluidSprites[0];
    }

    @Override
    public Component getDisplayName(IJeiFluidIngredient ingredient) {
        // TODO: better Fabric Fluid display name
        Fluid fluid = ingredient.getFluid();
        ResourceLocation key = Registry.FLUID.getKey(fluid);
        String path = key.getPath();
        path = path.replace("_", " ");
        path = StringUtils.capitalize(path);
        return new TextComponent(path);
    }

    @Override
    public int getColorTint(IJeiFluidIngredient ingredient) {
        FluidRenderHandlerRegistry registry = FluidRenderHandlerRegistry.INSTANCE;
        Fluid fluid = ingredient.getFluid();
        FluidRenderHandler handler = registry.get(fluid);
        FluidState defaultFluidState = fluid.defaultFluidState();
        int fluidColor = handler.getFluidColor(null, null, defaultFluidState);
        return fluidColor | 0xFF000000;
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
