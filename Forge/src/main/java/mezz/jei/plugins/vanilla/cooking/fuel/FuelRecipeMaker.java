package mezz.jei.plugins.vanilla.cooking.fuel;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.util.ErrorUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;

public final class FuelRecipeMaker {
	private static final Logger LOGGER = LogManager.getLogger();

	private FuelRecipeMaker() {
	}

	public static List<IJeiFuelingRecipe> getFuelRecipes(IIngredientManager ingredientManager) {
		return ingredientManager.getAllIngredients(VanillaTypes.ITEM).stream()
			.<IJeiFuelingRecipe>mapMulti((stack, consumer) -> {
				int burnTime = getBurnTime(stack);
				if (burnTime > 0) {
					consumer.accept(new FuelingRecipe(List.of(stack), burnTime));
				}
			})
			.sorted(Comparator.comparingInt(IJeiFuelingRecipe::getBurnTime))
			.toList();
	}

	private static int getBurnTime(ItemStack itemStack) {
		try {
			return ForgeHooks.getBurnTime(itemStack, null);
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
			LOGGER.error("Failed to check if item is fuel {}.", itemStackInfo, e);
			return 0;
		}
	}
}
