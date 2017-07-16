package mezz.jei.ingredients;

import java.util.Comparator;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.util.Java6Util;
import mezz.jei.util.Translator;
import net.minecraft.item.ItemStack;
import mezz.jei.startup.IModIdHelper;

import mezz.jei.util.Log;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;


import java.util.*;

import invtweaks.api.InvTweaksAPI;

public final class IngredientListElementComparator implements Comparator<IIngredientListElement> {
	public static final IngredientListElementComparator INSTANCE = new IngredientListElementComparator();

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
	
	
	public static InvTweaksAPI invTweaksAPI;
    public static ArrayList<SortEntry> entries = new ArrayList<SortEntry>();
    public static ArrayList<SortEntry> list = new ArrayList<SortEntry>();
    private static String lastListOrder = "";
    
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
        //API.addSortOption(
		addLE("minecraft", new Comparator<IIngredientListElement>() {
            @Override
            public int compare(IIngredientListElement o1, IIngredientListElement o2) {
                boolean m1 = "minecraft".equals(o1.getModName());
                boolean m2 = "minecraft".equals(o2.getModName());
                return m1 == m2 ? 0 : m1 ? -1 : 1;
            }
        });
        //API.addSortOption(
		addLE("mod", new Comparator<IIngredientListElement>() {
            @Override
            public int compare(IIngredientListElement o1, IIngredientListElement o2) {
                String mod1 = o1.getModName();
                String mod2 = o2.getModName();
                
                if (mod1 == null) {
                    return mod2 == null ? 0 : 1;
                }
                if (mod2 == null) {
                    return -1;
                }
                return mod1.compareTo(mod2);
            }
        });
        //API.addSortOption(
		add("id", new Comparator<ItemStack>() {
            @Override
            public int compare(ItemStack o1, ItemStack o2) {
                int id1 = Item.getIdFromItem(o1.getItem());
                int id2 = Item.getIdFromItem(o2.getItem());
                return compareInt(id1, id2);
            }
        });
        //API.addSortOption(
		addLE("default", new Comparator<IIngredientListElement>() {
            @Override
            public int compare(IIngredientListElement o1, IIngredientListElement o2) {
				final Integer order1 = o1.getOrderIndex();
				final Integer order2 = o2.getOrderIndex();
                if (order1 == null) {
                    return order2 == null ? 0 : 1;
                }
                if (order2 == null) {
                    return -1;
                }
                return compareInt(order1, order2);
            }
        });
        //API.addSortOption(
		add("damage", new Comparator<ItemStack>() {
            @Override
            public int compare(ItemStack o1, ItemStack o2) {
                int id1 = o1.getItemDamage();
                int id2 = o2.getItemDamage();
                return compareInt(id1, id2);
            }
        });
        //API.addSortOption
		addLE("name", new Comparator<IIngredientListElement>() {
            @Override
            public int compare(IIngredientListElement o1, IIngredientListElement o2) {
                String name1 = o1.getDisplayName();
                String name2 = o2.getDisplayName();
                return name1.compareTo(name2);
            }
        });
		
		//Inventory Tweaks needs to be part of the build environment for this bit to work.
		boolean	hasInvTweaks = false;
		try {
			hasInvTweaks = Loader.isModLoaded("inventorytweaks"); 
		} catch (Exception ex) {
			//Nope
			hasInvTweaks = false;
		}
		
		if (hasInvTweaks){
			if (invTweaksAPI == null){
				invTweaksAPI =  (InvTweaksAPI) Loader.instance().getIndexedModList().get("inventorytweaks").getMod();
			}
			if (invTweaksAPI != null){
				add("invtweaks", new Comparator<ItemStack>() {
		            @Override
		            public int compare(ItemStack o1, ItemStack o2) {
		            	return invTweaksAPI.compareItems(o1, o2);
		            }
		        });
			}
		}
		
		return getSaveString(list); //Default value        
    }

    
	private static <V> String getWildcardUid(IIngredientListElement<V> ingredientListElement) {
		V ingredient = ingredientListElement.getIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientListElement.getIngredientHelper();
		return ingredientHelper.getWildcardId(ingredient);
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
