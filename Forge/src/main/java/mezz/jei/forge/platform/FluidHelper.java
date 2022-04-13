package mezz.jei.forge.platform;

import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.platform.IPlatformFluidHelper;
import mezz.jei.common.render.FluidTankRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public class FluidHelper implements IPlatformFluidHelper<FluidStack> {
    public static final FluidHelper INSTANCE = new FluidHelper();

    private FluidHelper() {}

    @Override
    public IIngredientTypeWithSubtypes<Fluid, FluidStack> getFluidIngredientType() {
        return ForgeTypes.FLUID_STACK;
    }

    @Override
    public IIngredientSubtypeInterpreter<FluidStack> getAllNbtSubtypeInterpreter() {
        return AllFluidNbt.INSTANCE;
    }

    @Override
    public IIngredientRenderer<FluidStack> createRenderer(int capacityMb, boolean showCapacity, int width, int height) {
        return new FluidTankRenderer<>(this, capacityMb, showCapacity, width, height);
    }

    @Override
    public int getColor(FluidStack ingredient) {
        Fluid fluid = ingredient.getFluid();
        FluidAttributes attributes = fluid.getAttributes();
        return attributes.getColor(ingredient);
    }

    @Override
    public long getAmount(FluidStack ingredient) {
        return ingredient.getAmount();
    }

    @Override
    public long bucketVolume() {
        return FluidAttributes.BUCKET_VOLUME;
    }

    @Override
    public TextureAtlasSprite getStillFluidSprite(FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        FluidAttributes attributes = fluid.getAttributes();
        ResourceLocation fluidStill = attributes.getStillTexture(fluidStack);

        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
    }

    @Override
    public Component getDisplayName(FluidStack ingredient) {
        return ingredient.getDisplayName();
    }

    private static class AllFluidNbt implements IIngredientSubtypeInterpreter<FluidStack> {
        public static final AllFluidNbt INSTANCE = new AllFluidNbt();

        private AllFluidNbt() {
        }

        @Override
        public String apply(FluidStack fluidStack, UidContext context) {
            CompoundTag nbtTagCompound = fluidStack.getTag();
            if (nbtTagCompound == null || nbtTagCompound.isEmpty()) {
                return IIngredientSubtypeInterpreter.NONE;
            }
            return nbtTagCompound.toString();
        }
    }
}
