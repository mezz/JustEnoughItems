package mezz.jei.plugins.vanilla.brewing;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.collect.SetMultiMap;

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
		Integer brewingSteps = brewingStepCache.get(potionOutputUid);
		if (brewingSteps == null) {
			previousSteps.add(potionOutputUid);
			Collection<String> prevPotions = potionMap.get(potionOutputUid);
			if (!prevPotions.isEmpty()) {
				int minPrevSteps = Integer.MAX_VALUE;
				for (String prevPotion : prevPotions) {
					if (!previousSteps.contains(prevPotion)) {
						int prevSteps = getBrewingSteps(prevPotion, previousSteps);
						minPrevSteps = Math.min(minPrevSteps, prevSteps);
					}
				}
				if (minPrevSteps < Integer.MAX_VALUE) {
					brewingSteps = minPrevSteps + 1;
					brewingStepCache.put(potionOutputUid, brewingSteps);
				}
			}
		}

		if (brewingSteps == null) {
			return Integer.MAX_VALUE;
		} else {
			return brewingSteps;
		}
	}
}
