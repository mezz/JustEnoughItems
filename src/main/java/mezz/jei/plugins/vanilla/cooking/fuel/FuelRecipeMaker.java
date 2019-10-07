package mezz.jei.plugins.vanilla.cooking.fuel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraftforge.common.ForgeHooks;
import net.minecraft.item.ItemStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class FuelRecipeMaker {
	private static final Logger LOGGER = LogManager.getLogger();

	private FuelRecipeMaker() {
	}

	public static List<FuelRecipe> getFuelRecipes(IIngredientManager ingredientManager, IJeiHelpers helpers) {
		IGuiHelper guiHelper = helpers.getGuiHelper();
		Collection<ItemStack> allItemStacks = ingredientManager.getAllIngredients(VanillaTypes.ITEM);
		List<FuelRecipe> fuelRecipes = new ArrayList<>();
		for (ItemStack stack : allItemStacks) {
			int burnTime = getBurnTime(stack);
			if (burnTime > 0) {
				fuelRecipes.add(new FuelRecipe(guiHelper, Collections.singleton(stack), burnTime));
			}
		}
		return fuelRecipes;
	}

	private static int getBurnTime(ItemStack itemStack) {
		try {
			return ForgeHooks.getBurnTime(itemStack);
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
			LOGGER.error("Failed to check if item is fuel {}.", itemStackInfo, e);
			return 0;
		}
	}
}
