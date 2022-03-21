package mezz.jei.api.recipe.vanilla;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * There is no vanilla registry of Anvil Recipes,
 * so JEI creates these Anvil recipes to use internally.
 *
 * Create your own with {@link IVanillaRecipeFactory#createAnvilRecipe}
 */
public interface IJeiAnvilRecipe {
	/**
	 * Get the inputs that go into the left slot of the Anvil.
	 *
	 * @since 9.5.0
	 */
	@Unmodifiable
	List<ItemStack> getLeftInputs();

	/**
	 * Get the inputs that go into the right slot of the Anvil.
	 *
	 * @since 9.5.0
	 */
	@Unmodifiable
	List<ItemStack> getRightInputs();

	/**
	 * Get the outputs of the Anvil recipe.
	 *
	 * @since 9.5.0
	 */
	@Unmodifiable
	List<ItemStack> getOutputs();
}
