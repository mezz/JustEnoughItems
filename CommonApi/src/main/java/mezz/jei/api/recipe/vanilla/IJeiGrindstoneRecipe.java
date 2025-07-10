package mezz.jei.api.recipe.vanilla;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * There is no vanilla registry of Grindstone Recipes,
 * so JEI creates these Grindstone recipes to use internally.
 *
 * Create your own with {@link IVanillaRecipeFactory#createGrindstoneRecipe}
 */
public interface IJeiGrindstoneRecipe {
    /**
     * Get the inputs that go into the top slot of the Grindstone.
     *
     * @since 21.3.2.9999
     */
    @Unmodifiable
    List<ItemStack> getTopInputs();

    /**
     * Get the inputs that go into the bottom slot of the Grindstone.
     *
     * @since 21.3.2.9999
     */
    @Unmodifiable
    List<ItemStack> getBottomInputs();

    /**
     * Get the outputs of the Anvil recipe.
     *
     * @since 21.3.2.9999
     */
    @Unmodifiable
    List<ItemStack> getOutputs();

    /**
     * The average amount of XP that a player receives.
     *
     * @since 21.3.2.9999
     */
    @Unmodifiable
    float getAverageXpReward();

    /**
     * Unique ID for this recipe.
     * @since 21.3.2.9999
     */
    @Nullable
    ResourceLocation getUid();

    /**
     * Make the output render only, to avoid displaying unnecessary crafting recipes.
     *
     * @since 21.3.2.9999
     */
    @Unmodifiable
    boolean isOutputRenderOnly();
}
