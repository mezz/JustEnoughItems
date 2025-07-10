package mezz.jei.library.plugins.vanilla.grindstone;

import mezz.jei.api.recipe.vanilla.IJeiGrindstoneRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public record GrindstoneRecipe(
        List<ItemStack> topInputs,
        List<ItemStack> bottomInputs,
        List<ItemStack> outputs,
        float averageXpReward,
        @Nullable ResourceLocation uid,
        boolean isOutputRenderOnly
) implements IJeiGrindstoneRecipe {

    public GrindstoneRecipe(
            List<ItemStack> topInputs,
            List<ItemStack> bottomInputs,
            List<ItemStack> outputs,
            float averageXpReward,
            @Nullable ResourceLocation uid) {
        this(topInputs, bottomInputs, outputs, averageXpReward, uid, true);
    }

    @Override
    @Unmodifiable
    @NotNull
    public List<ItemStack> getTopInputs() {
        return topInputs;
    }

    @Override
    @Unmodifiable
    @NotNull
    public List<ItemStack> getBottomInputs() {
        return bottomInputs;
    }

    @Override
    @Unmodifiable
    @NotNull
    public List<ItemStack> getOutputs() {
        return outputs;
    }

    @Override
    @Unmodifiable
    public float getAverageXpReward() {
        return averageXpReward;
    }

    @Override
    @Nullable
    public ResourceLocation getUid() {
        return uid;
    }

    @Override
    @Unmodifiable
    public boolean isOutputRenderOnly() {
        return isOutputRenderOnly;
    }
}