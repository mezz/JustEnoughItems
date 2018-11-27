package mezz.jei.ingredients;

import mezz.jei.config.Constants;
import mezz.jei.gui.ingredients.IIngredientListElement;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraftforge.oredict.OreDictionary;
import mezz.jei.config.Config;
import mezz.jei.util.Translator;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShears;
//import scala.Int;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Arrays;
import java.util.Collection;

import com.google.common.collect.Multimap;

public final class IngredientListElementComparator implements Comparator<IIngredientListElement> {
	//public static final IngredientListElementComparator INSTANCE = new IngredientListElementComparator();

	public static ArrayList<SortEntry> entries = new ArrayList<>();
	private ArrayList<SortEntry> list = new ArrayList<>();
	private static boolean areDefaultEntriesLoaded = false;

	public IngredientListElementComparator() {
		if (entries.size() == 0) {
			initConfig(); // emergency initializer, probably happened already.
		}
		loadConfig(Config.getSortOrder());
	}

	public IngredientListElementComparator(String itemSortConfig) {
		if (entries.size() == 0) {
			initConfig(); // emergency initializer, probably happened already.
		}
		loadConfig(itemSortConfig);
	}

	public static class SortEntry {
		public String name;
		// External interfaces only have access to ItemStacks, sorry fluids.
		public Comparator<ItemStack> itemStackComparator;
		// This is the internal comparator, fluids supported here.
		public Comparator<IIngredientListElement> ingredientComparator;

		public SortEntry(String name, Comparator<ItemStack> comparator) {
			this.name = name;
			this.itemStackComparator = comparator;
		}

		public SortEntry(String name) {
			this.name = name;
		}

		public String getLocalisedName() {
			return Translator.translateToLocal(name);
		}

		public String getTooltip() {
			String tipname = name + ".tip";
			String tip = Translator.translateToLocal(tipname);
			return !tip.equals(tipname) ? tip : null;
		}
	}

	@Override
	public int compare(IIngredientListElement o1, IIngredientListElement o2) {
		//o1.getIngredient()
		for (SortEntry entry : list) {
			int comparison = 0;
			if (entry.ingredientComparator != null) {
				comparison = entry.ingredientComparator.compare(o1, o2);
			} else if (entry.itemStackComparator != null) {
				ItemStack itemStack1 = getItemStack(o1);
				ItemStack itemStack2 = getItemStack(o2);
				int haveItemStack1 = itemStack1 != null ? 1 : 0;
				int haveItemStack2 = itemStack2 != null ? 1 : 0;
				if (itemStack1 == null || itemStack2 == null) {
					comparison = haveItemStack2 - haveItemStack1;
				} else {
					comparison = entry.itemStackComparator.compare(itemStack1, itemStack2);
				}
			}
			if (comparison != 0) {
				return comparison;
			}
		}
		return 0;
	}

	public static SortEntry find(String name) {
		for (SortEntry entry : entries) {
			if (entry.name.equals(name)) {
				return entry;
			}
		}
		return null;
	}

	public static void add(String name, Comparator<ItemStack> comparator) {
		SortEntry existingEntry = find(name);
		if (existingEntry == null) {
			SortEntry newEntry = new SortEntry(name, comparator);
			entries.add(newEntry);
			existingEntry = newEntry;
		}
	}

	private static void addListElementComparison(String name, Comparator<IIngredientListElement> comparator) {
		SortEntry existingEntry = find(name);
		if (existingEntry == null) {
			SortEntry newEntry = new SortEntry(name);
			newEntry.ingredientComparator = comparator;
			entries.add(newEntry);
			existingEntry = newEntry;
		}
	}

	public static String initConfig(/* ConfigTagParent tag */) {
		// minecraft, mod, id, default, meta, name
		if (areDefaultEntriesLoaded) {
			return getSaveString(entries); // Default value
		}

		// Don't load the entries list twice.
		areDefaultEntriesLoaded = true;
		
		//The order these are called sets the default order.
		addToolComparator();
		addMeleeComparator();
		addArmorComparator();
		addDictionaryComparator();
		addMinecraftComparator();
		addModComparator();
		addNameComparator();
		addDamageComparator();
		addIdComparator();
		addDefaultOrderComparator();
		
		
		return getSaveString(entries); // Default value
	}
	
	private static void addMinecraftComparator() {
		addListElementComparison("minecraft", new Comparator<IIngredientListElement>() {
			@Override
			public int compare(IIngredientListElement o1, IIngredientListElement o2) {
				boolean isMinecraft1 = Constants.MINECRAFT_NAME.equals(o1.getModNameForSorting());
				boolean isMinecraft2 = Constants.MINECRAFT_NAME.equals(o2.getModNameForSorting());
				return isMinecraft1 == isMinecraft2 ? 0 : isMinecraft1 ? -1 : 1;
			}
		});
	}
	
	private static void addModComparator() {
		addListElementComparison("mod", new Comparator<IIngredientListElement>() {
			@Override
			public int compare(IIngredientListElement o1, IIngredientListElement o2) {
				String modName1 = o1.getModNameForSorting();
				String modName2 = o2.getModNameForSorting();

				if (modName1 == null) {
					return modName2 == null ? 0 : 1;
				}
				if (modName2 == null) {
					return -1;
				}
				return modName1.compareTo(modName2);
			}
		});
	}
	
	private static void addIdComparator() {
		add("id", new Comparator<ItemStack>() {
			@Override
			public int compare(ItemStack o1, ItemStack o2) {
				int id1 = Item.getIdFromItem(o1.getItem());
				int id2 = Item.getIdFromItem(o2.getItem());
				return Integer.compare(id1, id2);
			}
		});

	}
	
	private static void addDefaultOrderComparator() {
		addListElementComparison("default", new Comparator<IIngredientListElement>() {
			@Override
			public int compare(IIngredientListElement o1, IIngredientListElement o2) {
				final Integer order1 = o1.getOrderIndex();
				final Integer order2 = o2.getOrderIndex();
				return Integer.compare(order1, order2);
			}
		});

	}
	
	private static void addDamageComparator() {
		add("damage", new Comparator<ItemStack>() {
			@Override
			public int compare(ItemStack o1, ItemStack o2) {
				int damage1 = o1.getItemDamage();
				int damage2 = o2.getItemDamage();
				return Integer.compare(damage1, damage2);
			}
		});

	}
	
	private static void addNameComparator() {
		addListElementComparison("name", new Comparator<IIngredientListElement>() {
			@Override
			public int compare(IIngredientListElement o1, IIngredientListElement o2) {
				String name1 = o1.getDisplayName();
				String name2 = o2.getDisplayName();

				int c = name1.compareTo(name2);
				return c > 0 ? 1 : c < 0 ? -1 : 0;
			}
		});

	}

	private static String getToolClass(ItemStack itemStack, Item item)
	{
		if (itemStack == null || item == null) return "";
		Set<String> toolClassSet = item.getToolClasses(itemStack);

		if (toolClassSet.contains("sword")) {
			//Swords are not "tools".
			Set<String> newClassSet = new HashSet<String>();
			for (String toolClass: toolClassSet)
				if (toolClass != "sword")
					newClassSet.add(toolClass);
			toolClassSet = newClassSet;
		}

		//Minecraft hoes and shears don't have tool class names.
		if (toolClassSet.isEmpty()) {
			if (item instanceof ItemHoe) return "hoe";
			if (item instanceof ItemShears) return "shears";
			return "";
		}
		
		//Get the only thing.
		if (toolClassSet.size() == 1)
			return (String) toolClassSet.toArray()[0];
		
		//We have a preferred type to list tools under, primarily the pickaxe for harvest level.
		String[] prefOrder = {"pickaxe", "axe", "shovel", "hoe", "shears", "wrench"};
		for (int i = 0; i < prefOrder.length; i++)
			if (toolClassSet.contains(prefOrder[i])) 
				return prefOrder[i];
		
		//Whatever happens to be the first thing:
		return (String) toolClassSet.toArray()[0];
	}
	
	private static void addToolComparator() {
		addListElementComparison("tool", new Comparator<IIngredientListElement>() {
			@Override
			public int compare(IIngredientListElement o1, IIngredientListElement o2) {
				ItemStack itemStack1 = getItemStack(o1);
				ItemStack itemStack2 = getItemStack(o2);
				if (itemStack1 == null && itemStack2 == null)
					return 0;
				Item item1 = itemStack1 != null ? itemStack1.getItem() : null;
				Item item2 = itemStack2 != null ? itemStack2.getItem() : null;

				String toolClass1 = getToolClass(itemStack1, item1);
				String toolClass2 = getToolClass(itemStack2, item2);
				// These are ints so I can subtract for a comparison value later instead of
				// booleans.
				int isTool1 = toolClass1 != "" ? 1 : 0;
				int isTool2 = toolClass2 != "" ? 1 : 0;
				if (isTool1 == 0 || isTool2 == 0) {
					//This should catch any instances where one of the stacks is null.
					return isTool2 - isTool1;
				} else if (isTool1 == 1 && isTool2 == 1) {
					int toolClassComparison = toolClass1.compareTo(toolClass2);
					if (toolClassComparison != 0) {
						return toolClassComparison;
					}
					// If they were the same type, sort with the better harvest level first.
					int harvestLevel1 = item1.getHarvestLevel(itemStack1, toolClass1, null, null);
					int harvestLevel2 = item2.getHarvestLevel(itemStack2, toolClass2, null, null);
					int toolLevelComparison = harvestLevel2 - harvestLevel1;
					if (toolLevelComparison != 0) {
						return Integer.compare(harvestLevel2 , harvestLevel1);
					}
				}

				// If all else is the same, sort the highest-durability tool first.
				// No durability is treated as basically infinite.
				int durability1 = itemStack1.getMaxDamage() <= 0 ? Integer.MAX_VALUE : itemStack1.getMaxDamage();
				int durability2 = itemStack2.getMaxDamage() <= 0 ? Integer.MAX_VALUE : itemStack2.getMaxDamage();
				return Integer.compare(durability2 , durability1);

			}
		});

	}
	
	private static void addMeleeComparator() {
		// Sort by melee damage and speed (Sort by Tool first if you don't want swords
		// and tools mixed together.
		addListElementComparison("melee", new Comparator<IIngredientListElement>() {
			@Override
			public int compare(IIngredientListElement o1, IIngredientListElement o2) {
				ItemStack itemStack1 = getItemStack(o1);
				ItemStack itemStack2 = getItemStack(o2);
				if (itemStack1 == null && itemStack2 == null)
					return 0;

				Multimap<String, AttributeModifier> multimap1 = itemStack1 != null ? itemStack1.getAttributeModifiers(EntityEquipmentSlot.MAINHAND) : null;
				Multimap<String, AttributeModifier> multimap2 = itemStack2 != null ? itemStack2.getAttributeModifiers(EntityEquipmentSlot.MAINHAND) : null;

				final String attackDamageName = SharedMonsterAttributes.ATTACK_DAMAGE.getName();
				final String attackSpeedName = SharedMonsterAttributes.ATTACK_SPEED.getName();

				boolean hasDamage1 = itemStack1 != null ? multimap1.containsKey(attackDamageName) : false;
				boolean hasDamage2 = itemStack2 != null ? multimap2.containsKey(attackDamageName) : false;
				boolean hasSpeed1 = itemStack1 != null ? multimap1.containsKey(attackSpeedName) : false;
				boolean hasSpeed2 = itemStack2 != null ? multimap2.containsKey(attackSpeedName) : false;

				if (!hasDamage1 || !hasDamage2) {
					return (hasDamage2 ? 1 : 0) - (hasDamage1 ? 1 : 0);
				} else {
					Collection<AttributeModifier> damageMap1 = multimap1.get(attackDamageName);
					Collection<AttributeModifier> damageMap2 = multimap2.get(attackDamageName);
					Double attackDamage1 = ((AttributeModifier) damageMap1.toArray()[0]).getAmount();
					Double attackDamage2 = ((AttributeModifier) damageMap2.toArray()[0]).getAmount();
					// This funny comparison is because Double == Double never seems to work.
					int damageComparison = Double.compare(attackDamage2, attackDamage1);
					if (damageComparison == 0 && hasSpeed1 && hasSpeed2) {
						// Same damage, sort faster weapon first.
						Collection<AttributeModifier> speedMap1 = multimap1.get(attackSpeedName);
						Collection<AttributeModifier> speedMap2 = multimap2.get(attackSpeedName);
						Double speed1 = ((AttributeModifier) speedMap1.toArray()[0]).getAmount();
						Double speed2 = ((AttributeModifier) speedMap2.toArray()[0]).getAmount();
						int speedComparison = Double.compare(speed2, speed1);
						if (speedComparison != 0)
							return speedComparison;
					} else if (damageComparison != 0) {
						// Higher damage first.
						return damageComparison;
					}
					// Most durability if everything else is the same.
					int durability1 = itemStack1.getMaxDamage() <= 0 ? Integer.MAX_VALUE : itemStack1.getMaxDamage();
					int durability2 = itemStack2.getMaxDamage() <= 0 ? Integer.MAX_VALUE : itemStack2.getMaxDamage();
					return Integer.compare(durability2 , durability1);
				}
			}
		});

	}
	
	private static void addArmorComparator() {
		// Armor sorting, High to low AC.
		addListElementComparison("armor", new Comparator<IIngredientListElement>() {
			@Override
			public int compare(IIngredientListElement o1, IIngredientListElement o2) {
				ItemStack itemStack1 = getItemStack(o1);
				ItemStack itemStack2 = getItemStack(o2);
				if (itemStack1 == null && itemStack2 == null)
					return 0;
				Item item1 = itemStack1 != null ? itemStack1.getItem() : null;
				Item item2 = itemStack2 != null ? itemStack2.getItem() : null;
				int isArmor1 = (item1 instanceof ItemArmor) ? 1 : 0;
				int isArmor2 = (item2 instanceof ItemArmor) ? 1 : 0;
				if (isArmor1 == 0 || isArmor2 == 0) { 
					//This should catch any instances where one of the stacks is null.
					return isArmor2 - isArmor1;
				} else {
					ItemArmor a1 = (ItemArmor) item1;
					ItemArmor a2 = (ItemArmor) item2;
					if (a1.armorType != a2.armorType) {
						return a2.armorType.compareTo(a1.armorType);
					} else if (a1.damageReduceAmount != a2.damageReduceAmount) {
						return a2.damageReduceAmount - a1.damageReduceAmount;
					} else if (a1.toughness != a2.toughness) {
						return a2.toughness > a1.toughness ? -1 : 1;
					}
					// Most durability if everything else is the same.
					int durability1 = itemStack1.getMaxDamage() <= 0 ? Integer.MAX_VALUE : itemStack1.getMaxDamage();
					int durability2 = itemStack2.getMaxDamage() <= 0 ? Integer.MAX_VALUE : itemStack2.getMaxDamage();
					return Integer.compare(durability2 , durability1);
				}
			}
		});

	}
	
	private static String getBestDictionaryName(ItemStack itemStack)
	{
		String bestOreName = "";
		int most = 0;
		for (int oreId : OreDictionary.getOreIDs(itemStack)) {
			String oreName = OreDictionary.getOreName(oreId);
			String oreNameLowercase = oreName.toLowerCase(Locale.ENGLISH);
			int cnt = OreDictionary.getOres(oreName).size();
			if (cnt > most) {
				most = cnt;
				bestOreName = oreNameLowercase;
			} else if (cnt == most && oreNameLowercase.compareTo(bestOreName) < 0) {
				bestOreName = oreNameLowercase;
			}
		}
		
		return bestOreName;
	}
	
	private static void addDictionaryComparator() {
		// Ore Dictionary sorting.
		add("dictionary", new Comparator<ItemStack>() {
			@Override
			public int compare(ItemStack o1, ItemStack o2) {
				String oreName1 = getBestDictionaryName(o1);
				String oreName2 = getBestDictionaryName(o2);
				if (oreName1 == oreName2 ) return 0;
				else if (oreName1 == "") return 1;
				else if (oreName2 == "") return -1;
				
				return oreName1.compareTo(oreName2);
			}
		});

	}

	@Nullable
	private static <V> ItemStack getItemStack(IIngredientListElement<V> ingredientListElement) {
		Object ingredient = ingredientListElement.getIngredient();
		if (ingredient instanceof ItemStack) {
			return ((ItemStack) ingredient);
		}
		return null;

	}

	public static String getInclusiveSaveString() {
		return getInclusiveSaveString(Config.getSortOrder());
	}


	/*
	 * This is a non-destructive way to ensure all options are preserved
	 * in the configuration of the sorting options.  Not all of them may
	 * exist at all times as the mods are loaded (or removed/added).
	 */
	public static String getInclusiveSaveString(String savedList) {
		if (savedList == null || savedList.length() == 0) {
			return getSaveString(entries);
		}

		List<String> savedListArray = Arrays.asList(savedList.split(","));
		for (SortEntry aSortEntry : entries) {
			if (!savedListArray.contains(aSortEntry.name)) {
				if (savedList.length() > 0) {
					savedList += (',');
				}
				savedList += (aSortEntry.name);
			}
		}
		return savedList;
	}

	public static String getSaveString(List<SortEntry> list) {
		StringBuilder savedList = new StringBuilder();
		for (SortEntry entry : list) {
			if (savedList.length() > 0) {
				savedList.append(',');
			}
			savedList.append(entry.name);
		}
		return savedList.toString();
	}

	public static ArrayList<SortEntry> fromSaveString(String savedList) {
		if (savedList == null || savedList.length() == 0) {
			return new ArrayList<SortEntry>(entries);
		}

		ArrayList<SortEntry> workingList = new ArrayList<SortEntry>();
		for (String savedListItem : savedList.split(",")) {
			SortEntry aSortEntry = find(savedListItem.trim());
			if (aSortEntry != null) {
				workingList.add(aSortEntry);
			}
		}
		for (SortEntry aSortEntry : entries) {
			if (!workingList.contains(aSortEntry)) {
				workingList.add(aSortEntry);
			}
		}

		return workingList;
	}

	public void loadConfig(String itemSortConfig) {
		list = fromSaveString(itemSortConfig);
	}

	public void clearList() {
		// Next time the comparator function gets called, it will pull the order from the config.
		list = new ArrayList<SortEntry>();
	}
}
