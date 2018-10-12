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

	private IngredientListElementComparator() {

	}

	public static class SortEntry {
        public String name;
        //External interfaces only have access to ItemStacks, sorry fluids.
        public Comparator<ItemStack> comparator;
        //This is the internal comparator, fluids supported here.
        public Comparator<IIngredientListElement> lEcomparator;
        public SortEntry(String name, Comparator<ItemStack> comparator) {
            this.name = name;
            this.comparator = comparator;
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
			initConfig();
			list = fromSaveString(Config.getSortOrder());
		}

        for (SortEntry e : list) {
        	int c = 0;
        	if (e.lEcomparator != null) {
            	c = e.lEcomparator.compare(o1, o2);
        	}
        	else if (e.comparator!= null) {
        		ItemStack is1 = getItemStack(o1);
        		ItemStack is2 = getItemStack(o2);
        		if (is1 == null && is2 == null) {
        			c = 0;
        		}
        		else if (is1 != null && is2 == null) {
        			c = -1;
        		}
        		else if (is1 == null && is2 != null) {
        			c = 1;
        		}
        		else {
        			c = e.comparator.compare(is1, is2);
        		}
        	}
            if (c != 0) {
                return c;
            }
        }
        return 0;
    }

    public static SortEntry find(String name) {
        for (SortEntry e : entries) {
            if (e.name.equals(name)) {
                return e;
            }
        }
        return null;
    }

    public static int compareInt(int a, int b) {
        return a == b ? 0 : a < b ? -1 : 1;
    }

    public static void add(String name, Comparator<ItemStack> comparator) {
        SortEntry e = new SortEntry(name, comparator);
        entries.add(e);
        ArrayList<SortEntry> nlist = new ArrayList<SortEntry>(list);
        nlist.add(e);
        list = nlist;//concurrency
    }

    private static void addLE(String name, Comparator<IIngredientListElement> comparator) {
        SortEntry e = new SortEntry(name);
        e.lEcomparator = comparator;
        entries.add(e);
        ArrayList<SortEntry> nlist = new ArrayList<SortEntry>(list);
        nlist.add(e);
        list = nlist;//concurrency
    }

    
    public static String initConfig(/*ConfigTagParent tag*/) {
        //minecraft, mod, id, default, meta, name

		addLE("minecraft", new Comparator<IIngredientListElement>() {
            @Override
            public int compare(IIngredientListElement o1, IIngredientListElement o2) {
                boolean m1 = Constants.MINECRAFT_NAME.equals(o1.getModNameForSorting());
                boolean m2 = Constants.MINECRAFT_NAME.equals(o2.getModNameForSorting());
                return m1 == m2 ? 0 : m1 ? -1 : 1;
            }
        });

		addLE("mod", new Comparator<IIngredientListElement>() {
            @Override
            public int compare(IIngredientListElement o1, IIngredientListElement o2) {
                String mod1 = o1.getModNameForSorting();
                String mod2 = o2.getModNameForSorting();
                
                if (mod1 == null) {
                    return mod2 == null ? 0 : 1;
                }
                if (mod2 == null) {
                    return -1;
                }
                return mod1.compareTo(mod2);
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

		addLE("default", new Comparator<IIngredientListElement>() {
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
                int id1 = o1.getItemDamage();
                int id2 = o2.getItemDamage();
                return compareInt(id1, id2);
            }
        });

		addLE("name", new Comparator<IIngredientListElement>() {
            @Override
            public int compare(IIngredientListElement o1, IIngredientListElement o2) {
                String name1 = o1.getDisplayName();
                String name2 = o2.getDisplayName();

                int c = name1.compareTo(name2);
                return c > 0 ? 1 : c < 0 ? -1 : 0;
            }
        });

		addLE("tool", new Comparator<IIngredientListElement>() {
            @Override
            public int compare(IIngredientListElement o1, IIngredientListElement o2) {
            	ItemStack i1 = getItemStack(o1);
            	ItemStack i2 = getItemStack(o2);
            	if (i1 == null || i2 == null) return 0;
            	Item item1 = i1.getItem();
            	Item item2 = i2.getItem();
            	int isTool1 = (item1 instanceof ItemTool) ? 1 : 0;
            	int isTool2 = (item2 instanceof ItemTool) ? 1 : 0;
            	int isHoe1 = (item1 instanceof ItemHoe) ? 1 : 0;
            	int isHoe2 = (item2 instanceof ItemHoe) ? 1 : 0;
            	boolean isFakeTool1 = isTool1 != 0 || isHoe1 != 0;
            	boolean isFakeTool2 = isTool2 != 0 || isHoe2 != 0;
            	if (!isFakeTool1 || !isFakeTool2 ) {
            		return (isFakeTool2 ? 1 : 0) - (isFakeTool1 ? 1 : 0);
            	}
            	else if (isTool1 == 1 && isTool2 == 1){
            		ItemTool t1 = (ItemTool)item1;
            		ItemTool t2 = (ItemTool)item2;
                	Set<String> c1 = t1.getToolClasses(i1);
                	Set<String> c2 = t2.getToolClasses(i2);
                	//Grab the first tool class from the list.
                	String cs1 = c1.isEmpty() ? "" : (String)c1.toArray()[0];
                	String cs2 = c2.isEmpty() ? "" : (String)c2.toArray()[0];
                	int csComp = cs1.compareTo(cs2);
                	if (csComp != 0)
                	{
                		return csComp;
                	}
                	//If they were the same type, sort with the better harvest level first.
                	int lvlComp = t2.getHarvestLevel(i2, cs2, null, null) - t1.getHarvestLevel(i1, cs1, null, null) ;
            		if (lvlComp != 0)
            		{
            			return lvlComp;
            		}
            		                		
            	}
            	else if (isTool1 == 1 || isTool2 == 1) 
            	{
            			return isTool2 - isTool1;
            	}
            	
            	//If all else is the same, sort the highest-durability tool first.
        		int m1 = i1.getMaxDamage() <= 0 ? Int.MaxValue() : i1.getMaxDamage();
        		int m2 = i2.getMaxDamage() <= 0 ? Int.MaxValue() : i2.getMaxDamage();
        		return m2 - m1;
            	
            }
        });

		//Sort by melee damage and speed  (Sort by Tool first if you don't want swords and tools mixed together.
		addLE("melee", new Comparator<IIngredientListElement>() {
            @Override
            public int compare(IIngredientListElement o1, IIngredientListElement o2) {
            	ItemStack i1 = getItemStack(o1);
            	ItemStack i2 = getItemStack(o2);
            	if (i1 == null || i2 == null) return 0;

            	Multimap<String, AttributeModifier> multimap1 = i1.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
            	Multimap<String, AttributeModifier> multimap2 = i2.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
            	final String adName = SharedMonsterAttributes.ATTACK_DAMAGE.getName();
            	final String asName = SharedMonsterAttributes.ATTACK_SPEED.getName();
            	
            	boolean hasDamage1 = multimap1.containsKey(adName);
            	boolean hasDamage2 = multimap2.containsKey(adName);
            	boolean hasSpeed1 = multimap1.containsKey(asName);
            	boolean hasSpeed2 = multimap2.containsKey(asName);

            	if (!hasDamage1 || !hasDamage2) {
            		return (hasDamage2 ? 1 : 0) - (hasDamage1 ? 1 : 0);
            	}
            	else {
            		Collection<AttributeModifier> adMap1 = multimap1.get(adName);
            		Collection<AttributeModifier> adMap2 = multimap2.get(adName);
            		Double dmg1 = ((AttributeModifier)adMap1.toArray()[0]).getAmount();
            		Double dmg2 = ((AttributeModifier)adMap2.toArray()[0]).getAmount();
            		//This funny comparison is because Double == Double never seems to work.
            		int dmgComp = dmg1 > dmg2 ? -1 : (dmg1 < dmg2 ? 1 : 0);            		
            		if (dmgComp == 0 && hasSpeed1 && hasSpeed2)
            		{
            			//Same damage, sort faster weapon first.
                		adMap1 = multimap1.get(asName);
                		adMap2 = multimap2.get(asName);
                		Double spd1 = ((AttributeModifier)adMap1.toArray()[0]).getAmount();
                		Double spd2 = ((AttributeModifier)adMap2.toArray()[0]).getAmount();
                		int spdComp = spd1 > spd2 ? -1 : (spd1 < spd2 ? 1 : 0);
                		if (spdComp != 0)
                			return spdComp;
            		}
            		else if (dmgComp != 0)
            		{
            			//Higher damage first.
            			return dmgComp;
            		}
            	}
            	//Most durability if everything else is the same.
        		int m1 = i1.getMaxDamage() <= 0 ? Int.MaxValue() : i1.getMaxDamage();
        		int m2 = i2.getMaxDamage() <= 0 ? Int.MaxValue() : i2.getMaxDamage();
        		return m2 - m1;
            	
            }
        });
		
		//Armor sorting, High to low AC.
		addLE("armor", new Comparator<IIngredientListElement>() {
            @Override
            public int compare(IIngredientListElement o1, IIngredientListElement o2) {
            	ItemStack i1 = getItemStack(o1);
            	ItemStack i2 = getItemStack(o2);
            	if (i1 == null || i2 == null) return 0;
            	Item item1 = i1.getItem();
            	Item item2 = i2.getItem();
            	int isArmor1 = (item1 instanceof ItemArmor) ? 1 : 0;
            	int isArmor2 = (item2 instanceof ItemArmor) ? 1 : 0;
            	if (isArmor1 == 0 || isArmor2 == 0) {
            		return isArmor2 - isArmor1;
            	}
            	else 
            	{
            		ItemArmor a1 = (ItemArmor) item1;
            		ItemArmor a2 = (ItemArmor) item2;
            		if (a1.armorType != a2.armorType)
            		{
            			return a2.armorType.compareTo(a1.armorType);
            		}
            		else if (a1.damageReduceAmount != a2.damageReduceAmount)
            		{
            			return a2.damageReduceAmount - a1.damageReduceAmount;
            		}
            		else if (a1.toughness != a2.toughness)
            		{
            			return a2.toughness > a1.toughness ? -1: 1;
            		}
            		int m1 = i1.getMaxDamage() <= 0 ? Int.MaxValue() : i1.getMaxDamage();
            		int m2 = i2.getMaxDamage() <= 0 ? Int.MaxValue() : i2.getMaxDamage();
            		return m2 - m1;
            	}
            }
        });
			
		return getSaveString(list); //Default value
    }
    
    private static <V> ItemStack getItemStack(IIngredientListElement<V> ingredientListElement){
		Object ingredient = ingredientListElement.getIngredient();
		if (ingredient instanceof ItemStack) {
			return ((ItemStack) ingredient);
		}
		return null;

	}


    public static String getSaveString(List<SortEntry> list) {
        StringBuilder sb = new StringBuilder();
        for (SortEntry e : list) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(e.name);
        }
        return sb.toString();
    }

    public static ArrayList<SortEntry> fromSaveString(String s) {
        if (s == null) {
            return new ArrayList<SortEntry>(entries);
        }

        ArrayList<SortEntry> list = new ArrayList<SortEntry>();
        for (String s2 : s.split(",")) {
            SortEntry e = find(s2.trim());
            if (e != null) {
                list.add(e);
            }
        }
        for (SortEntry e : entries) {
            if (!list.contains(e)) {
                list.add(e);
            }
        }

        return list;
    }

    public static void loadConfig(String itemSortConfig) {
        list = fromSaveString(itemSortConfig);
    }
}
