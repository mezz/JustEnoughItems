package mezz.jei.forge.platform;

import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
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
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FluidHelper implements IPlatformFluidHelperInternal<FluidStack> {
    @Override
    public IIngredientTypeWithSubtypes<Fluid, FluidStack> getFluidIngredientType() {
        return ForgeTypes.FLUID_STACK;
    }

    @Override
    public IIngredientSubtypeInterpreter<FluidStack> getAllNbtSubtypeInterpreter() {
        return AllFluidNbt.INSTANCE;
    }

    @Override
    public IIngredientRenderer<FluidStack> createRenderer(long capacity, boolean showCapacity, int width, int height) {
        return new FluidTankRenderer<>(this, capacity, showCapacity, width, height);
    }

    @Override
    public int getColorTint(FluidStack ingredient) {
        Fluid fluid = ingredient.getFluid();
        FluidAttributes attributes = fluid.getAttributes();
        return attributes.getColor(ingredient);
    }

    @Override
    public long getAmount(FluidStack ingredient) {
        return ingredient.getAmount();
    }

    @Override
    public Optional<CompoundTag> getTag(FluidStack ingredient) {
        return Optional.ofNullable(ingredient.getTag());
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

    @Override
    public FluidStack create(Fluid fluid, long amount, @Nullable CompoundTag tag) {
        int intAmount = (int) Math.min(amount, Integer.MAX_VALUE);
        return new FluidStack(fluid, intAmount, tag);
    }

    @Override
    public FluidStack create(Fluid fluid, long amount) {
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
        copy.setAmount(FluidAttributes.BUCKET_VOLUME);
        return copy;
    }
}
