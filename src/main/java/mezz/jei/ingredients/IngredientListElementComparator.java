package mezz.jei.ingredients;

import mezz.jei.config.Constants;
import mezz.jei.gui.ingredients.IIngredientListElement;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import mezz.jei.config.Config;
import mezz.jei.util.Translator;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemHoe;
import scala.Int;
import java.util.*;

import com.google.common.collect.Multimap;

public final class IngredientListElementComparator implements Comparator<IIngredientListElement> {
	public static final IngredientListElementComparator INSTANCE = new IngredientListElementComparator();

	public static ArrayList<SortEntry> entries = new ArrayList<SortEntry>();
	public static ArrayList<SortEntry> list = new ArrayList<SortEntry>();
	private static boolean areDefaultEntriesLoaded = false;

	private IngredientListElementComparator() {

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
		if (list.size() == 0) {
			initConfig(); // emergency initializer, probably happened already.
			// This should be the first time this gets hit after each config (re)load.
			// This should allow enough time for other mods to load their options.
			loadConfig(Config.getSortOrder());
		}

		for (SortEntry entry : list) {
			int comparison = 0;
			if (entry.ingredientComparator != null) {
				comparison = entry.ingredientComparator.compare(o1, o2);
			} else if (entry.itemStackComparator != null) {
				ItemStack itemStack1 = getItemStack(o1);
				ItemStack itemStack2 = getItemStack(o2);
				if (itemStack1 == null && itemStack2 == null) {
					comparison = 0;
				} else if (itemStack1 != null && itemStack2 == null) {
					comparison = -1;
				} else if (itemStack1 == null && itemStack2 != null) {
					comparison = 1;
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

	public static int compareInt(int a, int b) {
		return a == b ? 0 : a < b ? -1 : 1;
	}

	public static void add(String name, Comparator<ItemStack> comparator) {
		SortEntry existingEntry = find(name);
		if (existingEntry == null) {
			SortEntry newEntry = new SortEntry(name, comparator);
			entries.add(newEntry);
			existingEntry = newEntry;
		}
		ArrayList<SortEntry> templist = new ArrayList<SortEntry>(list);
		templist.add(existingEntry);
		list = templist;// concurrency
	}

	private static void addListElementComparison(String name, Comparator<IIngredientListElement> comparator) {
		SortEntry existingEntry = find(name);
		if (existingEntry == null) {
			SortEntry newEntry = new SortEntry(name);
			newEntry.ingredientComparator = comparator;
			entries.add(newEntry);
			existingEntry = newEntry;
		}
		ArrayList<SortEntry> templist = new ArrayList<SortEntry>(list);
		templist.add(existingEntry);
		list = templist;// concurrency
	}

	public static String initConfig(/* ConfigTagParent tag */) {
		// minecraft, mod, id, default, meta, name
		if (areDefaultEntriesLoaded) {
			return getSaveString(entries); // Default value
		}

		// Don't load the entries list twice.
		areDefaultEntriesLoaded = true;

		addListElementComparison("minecraft", new Comparator<IIngredientListElement>() {
			@Override
			public int compare(IIngredientListElement o1, IIngredientListElement o2) {
				boolean isMinecraftool1 = Constants.MINECRAFT_NAME.equals(o1.getModNameForSorting());
				boolean isMinecraftool2 = Constants.MINECRAFT_NAME.equals(o2.getModNameForSorting());
				return isMinecraftool1 == isMinecraftool2 ? 0 : isMinecraftool1 ? -1 : 1;
			}
		});

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

		add("id", new Comparator<ItemStack>() {
			@Override
			public int compare(ItemStack o1, ItemStack o2) {
				int id1 = Item.getIdFromItem(o1.getItem());
				int id2 = Item.getIdFromItem(o2.getItem());
				return compareInt(id1, id2);
			}
		});

		addListElementComparison("default", new Comparator<IIngredientListElement>() {
			@Override
			public int compare(IIngredientListElement o1, IIngredientListElement o2) {
				final Integer order1 = o1.getOrderIndex();
				final Integer order2 = o2.getOrderIndex();
				return compareInt(order1, order2);
			}
		});

		add("damage", new Comparator<ItemStack>() {
			@Override
			public int compare(ItemStack o1, ItemStack o2) {
				int damage1 = o1.getItemDamage();
				int damage2 = o2.getItemDamage();
				return compareInt(damage1, damage2);
			}
		});

		addListElementComparison("name", new Comparator<IIngredientListElement>() {
			@Override
			public int compare(IIngredientListElement o1, IIngredientListElement o2) {
				String name1 = o1.getDisplayName();
				String name2 = o2.getDisplayName();

				int c = name1.compareTo(name2);
				return c > 0 ? 1 : c < 0 ? -1 : 0;
			}
		});

		addListElementComparison("tool", new Comparator<IIngredientListElement>() {
			@Override
			public int compare(IIngredientListElement o1, IIngredientListElement o2) {
				ItemStack itemStack1 = getItemStack(o1);
				ItemStack itemStack2 = getItemStack(o2);
				if (itemStack1 == null || itemStack2 == null)
					return 0;
				Item item1 = itemStack1.getItem();
				Item item2 = itemStack2.getItem();
				// These are ints so I can subtract for a comparison value later instead of
				// booleans.
				int isTool1 = (item1 instanceof ItemTool) ? 1 : 0;
				int isTool2 = (item2 instanceof ItemTool) ? 1 : 0;
				int isHoe1 = (item1 instanceof ItemHoe) ? 1 : 0;
				int isHoe2 = (item2 instanceof ItemHoe) ? 1 : 0;
				boolean isFakeTool1 = isTool1 != 0 || isHoe1 != 0;
				boolean isFakeTool2 = isTool2 != 0 || isHoe2 != 0;
				if (!isFakeTool1 || !isFakeTool2) {
					return (isFakeTool2 ? 1 : 0) - (isFakeTool1 ? 1 : 0);
				} else if (isTool1 == 1 && isTool2 == 1) {
					ItemTool tool1 = (ItemTool) item1;
					ItemTool tool2 = (ItemTool) item2;
					Set<String> toolClassSet1 = tool1.getToolClasses(itemStack1);
					Set<String> toolClassSet2 = tool2.getToolClasses(itemStack2);
					// Grab the first tool class from the list.
					String toolClasses1 = toolClassSet1.isEmpty() ? "" : (String) toolClassSet1.toArray()[0];
					String toolClasses2 = toolClassSet2.isEmpty() ? "" : (String) toolClassSet2.toArray()[0];
					int toolClassComparison = toolClasses1.compareTo(toolClasses2);
					if (toolClassComparison != 0) {
						return toolClassComparison;
					}
					// If they were the same type, sort with the better harvest level first.
					int toolLevelComparison = tool2.getHarvestLevel(itemStack2, toolClasses2, null, null)
							- tool1.getHarvestLevel(itemStack1, toolClasses1, null, null);
					if (toolLevelComparison != 0) {
						return toolLevelComparison;
					}

				} else if (isTool1 == 1 || isTool2 == 1) {
					return isTool2 - isTool1;
				}

				// If all else is the same, sort the highest-durability tool first.
				// No durability is treated as basically infinite.
				int durability1 = itemStack1.getMaxDamage() <= 0 ? Int.MaxValue() : itemStack1.getMaxDamage();
				int durability2 = itemStack2.getMaxDamage() <= 0 ? Int.MaxValue() : itemStack2.getMaxDamage();
				return durability2 - durability1;

			}
		});

		// Sort by melee damage and speed (Sort by Tool first if you don't want swords
		// and tools mixed together.
		addListElementComparison("melee", new Comparator<IIngredientListElement>() {
			@Override
			public int compare(IIngredientListElement o1, IIngredientListElement o2) {
				ItemStack itemStack1 = getItemStack(o1);
				ItemStack itemStack2 = getItemStack(o2);
				if (itemStack1 == null || itemStack2 == null)
					return 0;

				Multimap<String, AttributeModifier> multimap1 = itemStack1
						.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
				Multimap<String, AttributeModifier> multimap2 = itemStack2
						.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
				final String attackDamageName = SharedMonsterAttributes.ATTACK_DAMAGE.getName();
				final String attackSpeedName = SharedMonsterAttributes.ATTACK_SPEED.getName();

				boolean hasDamage1 = multimap1.containsKey(attackDamageName);
				boolean hasDamage2 = multimap2.containsKey(attackDamageName);
				boolean hasSpeed1 = multimap1.containsKey(attackSpeedName);
				boolean hasSpeed2 = multimap2.containsKey(attackSpeedName);

				if (!hasDamage1 || !hasDamage2) {
					return (hasDamage2 ? 1 : 0) - (hasDamage1 ? 1 : 0);
				} else {
					Collection<AttributeModifier> damageMap1 = multimap1.get(attackDamageName);
					Collection<AttributeModifier> damageMap2 = multimap2.get(attackDamageName);
					Double attackDamage1 = ((AttributeModifier) damageMap1.toArray()[0]).getAmount();
					Double attackDamage2 = ((AttributeModifier) damageMap2.toArray()[0]).getAmount();
					// This funny comparison is because Double == Double never seems to work.
					int damageComparison = attackDamage1 > attackDamage2 ? -1 : (attackDamage1 < attackDamage2 ? 1 : 0);
					if (damageComparison == 0 && hasSpeed1 && hasSpeed2) {
						// Same damage, sort faster weapon first.
						Collection<AttributeModifier> speedMap1 = multimap1.get(attackSpeedName);
						Collection<AttributeModifier> speedMap2 = multimap2.get(attackSpeedName);
						Double speed1 = ((AttributeModifier) speedMap1.toArray()[0]).getAmount();
						Double speed2 = ((AttributeModifier) speedMap2.toArray()[0]).getAmount();
						int speedComparison = speed1 > speed2 ? -1 : (speed1 < speed2 ? 1 : 0);
						if (speedComparison != 0)
							return speedComparison;
					} else if (damageComparison != 0) {
						// Higher damage first.
						return damageComparison;
					}
				}
				// Most durability if everything else is the same.
				int durability1 = itemStack1.getMaxDamage() <= 0 ? Int.MaxValue() : itemStack1.getMaxDamage();
				int durability2 = itemStack2.getMaxDamage() <= 0 ? Int.MaxValue() : itemStack2.getMaxDamage();
				return durability2 - durability1;

			}
		});

		// Armor sorting, High to low AC.
		addListElementComparison("armor", new Comparator<IIngredientListElement>() {
			@Override
			public int compare(IIngredientListElement o1, IIngredientListElement o2) {
				ItemStack itemStack1 = getItemStack(o1);
				ItemStack itemStack2 = getItemStack(o2);
				if (itemStack1 == null || itemStack2 == null)
					return 0;
				Item item1 = itemStack1.getItem();
				Item item2 = itemStack2.getItem();
				int isArmor1 = (item1 instanceof ItemArmor) ? 1 : 0;
				int isArmor2 = (item2 instanceof ItemArmor) ? 1 : 0;
				if (isArmor1 == 0 || isArmor2 == 0) {
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
					int durability1 = itemStack1.getMaxDamage() <= 0 ? Int.MaxValue() : itemStack1.getMaxDamage();
					int durability2 = itemStack2.getMaxDamage() <= 0 ? Int.MaxValue() : itemStack2.getMaxDamage();
					return durability2 - durability1;
				}
			}
		});

		return getSaveString(entries); // Default value
	}

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

	// I'm not sure if API items will get added to the config file automatically.
	// If they do, they'll be added before the static ones above.
	// I think there will be a need to have the API update the config value for new
	// options that aren't already in the string list.
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

	public static void loadConfig(String itemSortConfig) {
		list = fromSaveString(itemSortConfig);
	}

	public static void clearList() {
		// Next time the comparator function gets called, it will pull the order from
		// the config.
		list = new ArrayList<SortEntry>();
	}
}
