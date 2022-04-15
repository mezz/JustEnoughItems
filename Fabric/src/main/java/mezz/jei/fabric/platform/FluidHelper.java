package mezz.jei.fabric.platform;

import mezz.jei.api.fabric.FabricTypes;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.platform.IPlatformFluidHelper;
import mezz.jei.common.render.FluidTankRenderer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("UnstableApiUsage")
public class FluidHelper implements IPlatformFluidHelper<StorageView<FluidVariant>> {
    @Override
    public IIngredientTypeWithSubtypes<Fluid, StorageView<FluidVariant>> getFluidIngredientType() {
        return FabricTypes.FLUID_STORAGE;
    }

    @Override
    public IIngredientSubtypeInterpreter<StorageView<FluidVariant>> getAllNbtSubtypeInterpreter() {
        return AllFluidNbt.INSTANCE;
    }

    @Override
    public IIngredientRenderer<StorageView<FluidVariant>> createRenderer(long capacity, boolean showCapacity, int width, int height) {
        return new FluidTankRenderer<>(this, capacity, showCapacity, width, height);
    }

    @Override
    public TextureAtlasSprite getStillFluidSprite(StorageView<FluidVariant> ingredient) {
        FluidRenderHandlerRegistry registry = FluidRenderHandlerRegistry.INSTANCE;
        FluidVariant resource = ingredient.getResource();
        Fluid fluid = resource.getFluid();
        FluidRenderHandler handler = registry.get(fluid);
        FluidState defaultFluidState = fluid.defaultFluidState();
        TextureAtlasSprite[] fluidSprites = handler.getFluidSprites(null, null, defaultFluidState);
        return fluidSprites[0];
    }

    @Override
    public Component getDisplayName(StorageView<FluidVariant> ingredient) {
        // TODO: better Fabric Fluid display name
        FluidVariant resource = ingredient.getResource();
        Fluid fluid = resource.getFluid();
        ResourceLocation key = Registry.FLUID.getKey(fluid);
        String path = key.getPath();
        path = path.replace("_", " ");
        path = StringUtils.capitalize(path);
        return new TextComponent(path);
    }

    @Override
    public int getColor(StorageView<FluidVariant> ingredient) {
        FluidRenderHandlerRegistry registry = FluidRenderHandlerRegistry.INSTANCE;
        FluidVariant resource = ingredient.getResource();
        Fluid fluid = resource.getFluid();
        FluidRenderHandler handler = registry.get(fluid);
        FluidState defaultFluidState = fluid.defaultFluidState();
        return handler.getFluidColor(null, null, defaultFluidState);
    }

    @Override
    public long getAmount(StorageView<FluidVariant> ingredient) {
        return ingredient.getAmount();
    }

    @Override
    public long bucketVolume() {
        return FluidConstants.BUCKET;
    }

    private static class AllFluidNbt implements IIngredientSubtypeInterpreter<StorageView<FluidVariant>> {
        public static final AllFluidNbt INSTANCE = new AllFluidNbt();

        private AllFluidNbt() {
        }

        @Override
        public String apply(StorageView<FluidVariant> storage, UidContext context) {
            FluidVariant resource = storage.getResource();
            CompoundTag nbtTagCompound = resource.getNbt();
            if (nbtTagCompound == null || nbtTagCompound.isEmpty()) {
                return IIngredientSubtypeInterpreter.NONE;
            }
            return nbtTagCompound.toString();
        }
    }
}
