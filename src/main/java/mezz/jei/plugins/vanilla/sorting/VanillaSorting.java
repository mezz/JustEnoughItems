package mezz.jei.plugins.vanilla.sorting;

import com.google.common.collect.Multimap;
import com.google.common.primitives.Booleans;
import mezz.jei.api.ingredients.ISortableIngredient;
import mezz.jei.config.Constants;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class VanillaSorting {
	private VanillaSorting() {

	}

	public static int minecraftSort(ISortableIngredient<?> o1, ISortableIngredient<?> o2) {
		boolean isMinecraft1 = Constants.MINECRAFT_NAME.equals(o1.getModName());
		boolean isMinecraft2 = Constants.MINECRAFT_NAME.equals(o2.getModName());
		return Booleans.compare(isMinecraft2, isMinecraft1);
	}

	public static int oreDictionarySort(ItemStack itemStack1, ItemStack itemStack2) {
		String oreName1 = getOreDictionaryName(itemStack1);
		if (oreName1 == null) {
			return 0;
		}
		String oreName2 = getOreDictionaryName(itemStack2);
		if (oreName2 == null) {
			return 0;
		}
		return oreName1.compareTo(oreName2);
	}

	public static int armorSort(ItemStack itemStack1, ItemStack itemStack2) {
		Item item1 = itemStack1.getItem();
		Item item2 = itemStack2.getItem();
		if (!(item1 instanceof ItemArmor) || !(item2 instanceof ItemArmor)) {
			return 0;
		}

		ItemArmor a1 = (ItemArmor) item1;
		ItemArmor a2 = (ItemArmor) item2;
		if (a1.armorType != a2.armorType) {
			return a2.armorType.compareTo(a1.armorType);
		} else if (a1.damageReduceAmount != a2.damageReduceAmount) {
			return Integer.compare(a2.damageReduceAmount, a1.damageReduceAmount);
		} else if (a1.toughness != a2.toughness) {
			return Float.compare(a2.toughness, a1.toughness);
		} else {
			int durability1 = itemStack1.getMaxDamage() <= 0 ? Integer.MAX_VALUE : itemStack1.getMaxDamage();
			int durability2 = itemStack2.getMaxDamage() <= 0 ? Integer.MAX_VALUE : itemStack2.getMaxDamage();
			return Integer.compare(durability2, durability1);
		}
	}

	public static int meleeSort(ItemStack itemStack1, ItemStack itemStack2) {
		Multimap<String, AttributeModifier> multimap1 = itemStack1.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
		Multimap<String, AttributeModifier> multimap2 = itemStack2.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);

		final String attackDamageName = SharedMonsterAttributes.ATTACK_DAMAGE.getName();
		final String attackSpeedName = SharedMonsterAttributes.ATTACK_SPEED.getName();

		Collection<AttributeModifier> damageMap1 = multimap1.get(attackDamageName);
		Collection<AttributeModifier> damageMap2 = multimap2.get(attackDamageName);
		if (damageMap1 == null || damageMap2 == null || damageMap1.isEmpty() || damageMap2.isEmpty()) {
			return 0;
		}

		double attackDamage1 = damageMap1.iterator().next().getAmount();
		double attackDamage2 = damageMap2.iterator().next().getAmount();
		int damageComparison = Double.compare(attackDamage2, attackDamage1);
		if (damageComparison != 0) {
			return damageComparison;
		}

		// Same damage, sort faster weapon first.
		Collection<AttributeModifier> speedMap1 = multimap1.get(attackSpeedName);
		Collection<AttributeModifier> speedMap2 = multimap2.get(attackSpeedName);
		if (speedMap1 != null && speedMap2 != null && !speedMap1.isEmpty() && !speedMap2.isEmpty()) {
			double speed1 = speedMap1.iterator().next().getAmount();
			double speed2 = speedMap2.iterator().next().getAmount();
			int speedComparison = Double.compare(speed2, speed1);
			if (speedComparison != 0) {
				return speedComparison;
			}
		}

		// Most durability if everything else is the same.
		int durability1 = itemStack1.getMaxDamage() <= 0 ? Integer.MAX_VALUE : itemStack1.getMaxDamage();
		int durability2 = itemStack2.getMaxDamage() <= 0 ? Integer.MAX_VALUE : itemStack2.getMaxDamage();
		return Integer.compare(durability2, durability1);
	}

	public static int toolSort(ItemStack itemStack1, ItemStack itemStack2) {
		Item item1 = itemStack1.getItem();
		Item item2 = itemStack2.getItem();

		String toolClass1 = getToolClass(itemStack1, item1);
		String toolClass2 = getToolClass(itemStack2, item2);
		if (toolClass1 == null || toolClass2 == null) {
			return 0;
		}

		int toolClassComparison = toolClass1.compareTo(toolClass2);
		if (toolClassComparison != 0) {
			return toolClassComparison;
		}

		// If they were the same type, sort with the better harvest level first.
		int harvestLevel1 = item1.getHarvestLevel(itemStack1, toolClass1, null, null);
		int harvestLevel2 = item2.getHarvestLevel(itemStack2, toolClass2, null, null);
		int toolLevelComparison = harvestLevel2 - harvestLevel1;
		if (toolLevelComparison != 0) {
			return Integer.compare(harvestLevel2, harvestLevel1);
		}

		// If all else is the same, sort the highest-durability tool first.
		// No durability is treated as basically infinite.
		int durability1 = itemStack1.getMaxDamage() <= 0 ? Integer.MAX_VALUE : itemStack1.getMaxDamage();
		int durability2 = itemStack2.getMaxDamage() <= 0 ? Integer.MAX_VALUE : itemStack2.getMaxDamage();
		return Integer.compare(durability2, durability1);
	}

	public static int nameSort(ISortableIngredient<?> o1, ISortableIngredient<?> o2) {
		String name1 = o1.getDisplayName();
		String name2 = o2.getDisplayName();
		return name1.compareTo(name2);
	}

	public static int damageSort(ItemStack o1, ItemStack o2) {
		int damage1 = o1.getItemDamage();
		int damage2 = o2.getItemDamage();
		return Integer.compare(damage1, damage2);
	}

	public static int modNameSort(ISortableIngredient<?> o1, ISortableIngredient<?> o2) {
		String modName1 = o1.getModName();
		String modName2 = o2.getModName();
		return modName1.compareTo(modName2);
	}

	@Nullable
	private static String getToolClass(ItemStack itemStack, Item item) {
		Set<String> toolClassSet = item.getToolClasses(itemStack);

		if (toolClassSet.contains("sword")) {
			//Swords are not "tools".
			Set<String> newClassSet = new HashSet<>();
			for (String toolClass: toolClassSet) {
				if (!toolClass.equals("sword")) {
					newClassSet.add(toolClass);
				}
			}
			toolClassSet = newClassSet;
		}

		//Minecraft hoes and shears don't have tool class names.
		if (toolClassSet.isEmpty()) {
			if (item instanceof ItemHoe) {
				return "hoe";
			}
			if (item instanceof ItemShears) {
				return "shears";
			}
			return null;
		}

		if (toolClassSet.size() == 1) {
			return toolClassSet.iterator().next();
		}

		//We have a preferred type to list tools under, primarily the pickaxe for harvest level.
		String[] prefOrder = {"pickaxe", "axe", "shovel", "hoe", "shears", "wrench"};
		for (String aPrefOrder : prefOrder) {
			if (toolClassSet.contains(aPrefOrder)) {
				return aPrefOrder;
			}
		}

		return toolClassSet.iterator().next();
	}

	@Nullable
	private static String getOreDictionaryName(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return null;
		}
		String chosenOreName = null;
		int most = 0;
		for (int oreId : OreDictionary.getOreIDs(itemStack)) {
			String oreName = OreDictionary.getOreName(oreId);
			String oreNameLowercase = oreName.toLowerCase(Locale.ENGLISH);
			int count = OreDictionary.getOres(oreName).size();
			if (count > most) {
				most = count;
				chosenOreName = oreNameLowercase;
			} else if (count == most && (chosenOreName == null || oreNameLowercase.compareTo(chosenOreName) < 0)) {
				chosenOreName = oreNameLowercase;
			}
		}
		return chosenOreName;
	}

}
