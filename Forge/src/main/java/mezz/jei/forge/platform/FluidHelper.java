package mezz.jei.forge.platform;

import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.render.FluidTankRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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
        IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid);
        return renderProperties.getTintColor(ingredient);
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
    public TextureAtlasSprite getStillFluidSprite(FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation fluidStill = renderProperties.getStillTexture(fluidStack);

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
        copy.setAmount(FluidType.BUCKET_VOLUME);
        return copy;
    }
}
