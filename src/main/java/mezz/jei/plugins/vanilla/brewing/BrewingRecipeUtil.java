package mezz.jei.plugins.vanilla.brewing;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;

import mezz.jei.Internal;
import mezz.jei.collect.SetMultiMap;

public class BrewingRecipeUtil {
	public static final ItemStack POTION = new ItemStack(Items.POTIONITEM);
	public static final ItemStack WATER_BOTTLE = PotionUtils.addPotionToItemStack(POTION.copy(), PotionTypes.WATER);

	private final Map<String, Integer> brewingStepCache = new HashMap<>(); // output potion -> brewing steps
	private final SetMultiMap<String, String> potionMap = new SetMultiMap<>(); // output potion -> input potions

	public BrewingRecipeUtil() {
		clearCache();
	}

	public void addRecipe(ItemStack inputPotion, ItemStack outputPotion) {
		String potionInputUid = Internal.getStackHelper().getUniqueIdentifierForStack(inputPotion);
		String potionOutputUid = Internal.getStackHelper().getUniqueIdentifierForStack(outputPotion);
		potionMap.put(potionOutputUid, potionInputUid);
		clearCache();
	}

	public int getBrewingSteps(ItemStack outputPotion) {
		String potionInputUid = Internal.getStackHelper().getUniqueIdentifierForStack(outputPotion);
		return getBrewingSteps(potionInputUid, new HashSet<>());
	}

	private void clearCache() {
		if (brewingStepCache.size() != 1) {
			brewingStepCache.clear();
			String waterBottleUid = Internal.getStackHelper().getUniqueIdentifierForStack(WATER_BOTTLE);
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
