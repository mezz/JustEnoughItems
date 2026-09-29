package mezz.jei.api.recipe.vanilla;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.Nonnegative;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fueling recipes represent items that can be used as fuel in the Furnace, Smoker, Blast Furnace, etc.
 *
 * JEI automatically creates a fueling recipe for anything that has a burn time.
 *
 * @since 9.5.0
 */
@ApiStatus.NonExtendable
public interface IJeiFuelingRecipe {
	/**
	 * @return the inputs that act as a fuel
	 */
	@Unmodifiable
	List<ItemStack> getInputs();

	/**
	 * @return the fuel's burn time in ticks for this furnace type. Always greater than 0.
	 */
	@Nonnegative
	int getBurnTime();

	/**
	 * @return the number of items this fuel can cook, including partial items.
	 * Uses a standard 200-tick recipe and accounts for the fuel's cooking speed
	 * in this furnace type. Recipes with different cooking times may yield a different count.
	 *
	 * @implSpec The default assumes normal cooking speed for compatibility with older implementations.
	 *
	 * @since 31.8.0
	 */
	@Nonnegative
	default float getSmeltCount() {
		return getBurnTime() / 200f;
	}
}
