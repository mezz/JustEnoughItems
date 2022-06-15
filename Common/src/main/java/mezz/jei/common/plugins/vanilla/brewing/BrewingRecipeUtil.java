package mezz.jei.common.plugins.vanilla.brewing;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.core.collect.SetMultiMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BrewingRecipeUtil {
	public static final ItemStack POTION = new ItemStack(Items.POTION);
	public static final ItemStack WATER_BOTTLE = PotionUtils.setPotion(POTION.copy(), Potions.WATER);

	private final Map<String, Integer> brewingStepCache = new HashMap<>(); // output potion -> brewing steps
	private final SetMultiMap<String, String> potionMap = new SetMultiMap<>(); // output potion -> input potions
	private final IIngredientHelper<ItemStack> itemStackHelper;

	public BrewingRecipeUtil(IIngredientHelper<ItemStack> itemStackHelper) {
		this.itemStackHelper = itemStackHelper;
		clearCache();
	}

	public void addRecipe(List<ItemStack> inputPotions, ItemStack outputPotion) {
		String potionOutputUid = itemStackHelper.getUniqueId(outputPotion, UidContext.Recipe);
		for (ItemStack inputPotion : inputPotions) {
			String potionInputUid = itemStackHelper.getUniqueId(inputPotion, UidContext.Recipe);
			potionMap.put(potionOutputUid, potionInputUid);
		}
		clearCache();
	}

	public int getBrewingSteps(ItemStack outputPotion) {
		String potionInputUid = itemStackHelper.getUniqueId(outputPotion, UidContext.Recipe);
		return getBrewingSteps(potionInputUid, new HashSet<>());
	}

	private void clearCache() {
		if (brewingStepCache.size() != 1) {
			brewingStepCache.clear();
			String waterBottleUid = itemStackHelper.getUniqueId(WATER_BOTTLE, UidContext.Recipe);
			brewingStepCache.put(waterBottleUid, 0);
		}
	}

	private int getBrewingSteps(String potionOutputUid, Set<String> previousSteps) {
		Integer cachedBrewingSteps = brewingStepCache.get(potionOutputUid);
		if (cachedBrewingSteps != null) {
			return cachedBrewingSteps;
		}

		if (!previousSteps.add(potionOutputUid)) {
			return Integer.MAX_VALUE;
		}

		Collection<String> prevPotions = potionMap.get(potionOutputUid);
		int minPrevSteps = prevPotions.stream()
			.mapToInt(prevPotion -> getBrewingSteps(prevPotion, previousSteps))
			.min()
			.orElse(Integer.MAX_VALUE);

		int brewingSteps = minPrevSteps == Integer.MAX_VALUE ? Integer.MAX_VALUE : minPrevSteps + 1;
		brewingStepCache.put(potionOutputUid, brewingSteps);
		return brewingSteps;
	}
}
