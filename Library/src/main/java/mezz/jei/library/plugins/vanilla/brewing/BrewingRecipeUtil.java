package mezz.jei.library.plugins.vanilla.brewing;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.core.collect.SetMultiMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BrewingRecipeUtil {
	public static final ItemStack POTION = new ItemStack(Items.POTION);
	public static final ItemStack WATER_BOTTLE = PotionContents.createItemStack(POTION.getItem(), Potions.WATER);

	private final Map<Object, Integer> brewingStepCache = new HashMap<>(); // output potion -> brewing steps
	private final SetMultiMap<Object, Object> potionMap = new SetMultiMap<>(); // output potion -> input potions
	private final IIngredientHelper<ItemStack> itemStackHelper;

	public BrewingRecipeUtil(IIngredientHelper<ItemStack> itemStackHelper) {
		this.itemStackHelper = itemStackHelper;
		clearCache();
	}

	public void addRecipe(List<ItemStack> inputPotions, ItemStack outputPotion) {
		Object potionOutputUid = itemStackHelper.getUid(outputPotion, UidContext.Recipe);
		for (ItemStack inputPotion : inputPotions) {
			Object potionInputUid = itemStackHelper.getUid(inputPotion, UidContext.Recipe);
			potionMap.put(potionOutputUid, potionInputUid);
		}
		clearCache();
	}

	public int getBrewingSteps(ItemStack outputPotion) {
		Object potionInputUid = itemStackHelper.getUid(outputPotion, UidContext.Recipe);
		return getBrewingSteps(potionInputUid, new HashSet<>());
	}

	private void clearCache() {
		if (brewingStepCache.size() != 1) {
			brewingStepCache.clear();
			Object waterBottleUid = itemStackHelper.getUid(WATER_BOTTLE, UidContext.Recipe);
			brewingStepCache.put(waterBottleUid, 0);
		}
	}

	private int getBrewingSteps(Object potionOutputUid, Set<Object> previousSteps) {
		Integer cachedBrewingSteps = brewingStepCache.get(potionOutputUid);
		if (cachedBrewingSteps != null) {
			return cachedBrewingSteps;
		}

		if (!previousSteps.add(potionOutputUid)) {
			return Integer.MAX_VALUE;
		}

		Collection<Object> prevPotionUids = potionMap.get(potionOutputUid);
		int minPrevSteps = prevPotionUids.stream()
			.mapToInt(prevPotion -> getBrewingSteps(prevPotion, previousSteps))
			.min()
			.orElse(Integer.MAX_VALUE);

		int brewingSteps = minPrevSteps == Integer.MAX_VALUE ? Integer.MAX_VALUE : minPrevSteps + 1;
		brewingStepCache.put(potionOutputUid, brewingSteps);
		return brewingSteps;
	}
}
