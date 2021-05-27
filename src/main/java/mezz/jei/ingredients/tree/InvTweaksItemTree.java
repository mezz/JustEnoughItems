package mezz.jei.ingredients.tree;


import mezz.jei.ingredients.IngredientUtils;
import mezz.jei.api.ingredients.tree.IItemTree;
import mezz.jei.api.ingredients.tree.IItemTreeCategory;
import mezz.jei.api.ingredients.tree.IItemTreeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.Map.Entry;

/**
 * Contains the whole hierarchy of categories and items, as defined in the XML item tree. Is used to recognize keywords
 * and store item orders.
 *
 * @author Jimeo Wan
 */
public class InvTweaksItemTree implements IItemTree {
    public static final String UNKNOWN_ITEM = "unknown";

    private static final Logger log = LogManager.getLogger();
    
    private static List<IItemTreeItem> defaultItems = null;
    /**
     * All categories, stored by name
     */
    
    private Map<String, IItemTreeCategory> categories = new HashMap<>();
    /**
     * Items stored by ID. A same ID can hold several names.
     */
    
    private Map<String, List<IItemTreeItem>> itemsById = new HashMap<>(500);
    /**
     * Items stored by name. A same name can match several IDs.
     */
    
    private Map<String, List<IItemTreeItem>> itemsByName = new HashMap<>(500);

    private String rootCategory;
    
    private List<OreDictInfo> oresRegistered = new ArrayList<>();
    
    
    private List<ItemStack> allGameItems = new ArrayList<ItemStack>();

    private int highestOrder = 0;
    
    private int lastTreeOrder = 0;

    public InvTweaksItemTree() {
        reset();
    }

    public void reset() {

        if(defaultItems == null) {
            defaultItems = new ArrayList<>();
            
            IItemTreeCategory rootCat = getRootCategory(); 
            String rootName = "stuff";
            if (rootCat != null) {
            	rootName = rootCat.getName();
            }
            
            defaultItems.add(new InvTweaksItemTreeItem(UNKNOWN_ITEM, null, InvTweaksConst.DAMAGE_WILDCARD, null,
                    Integer.MAX_VALUE, rootName));
        }

        // Reset tree
        categories.clear();
        itemsByName.clear();
        itemsById.clear();

    }

    /**
     * Checks if given item ID matches a given keyword (either the item's name is the keyword, or it is in the keyword
     * category)
     */
    @Override
    public boolean matches( List<IItemTreeItem> items,  String keyword) {

        if(items == null) {
            return false;
        }

        // The keyword is an item
        for( IItemTreeItem item : items) {
            if(item.getName() != null && item.getName().equals(keyword)) {
                return true;
            }
        }

        // The keyword is a category
        IItemTreeCategory category = getCategory(keyword);
        if(category != null) {
            for(IItemTreeItem item : items) {
                if(category.contains(item)) {
                    return true;
                }
            }
        }

        // Everything is stuff
        return keyword.equals(rootCategory);

    }

    @Override
    public int getKeywordDepth(String keyword) {
        try {
            return getRootCategory().findKeywordDepth(keyword);
        } catch(NullPointerException e) {
            log.error("The root category is missing: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int getKeywordOrder(String keyword) {
        List<IItemTreeItem> items = getItems(keyword);
        if(items != null && items.size() != 0) {
            return items.get(0).getOrder();
        } else {
            try {
                return getRootCategory().findCategoryOrder(keyword);
            } catch(NullPointerException e) {
                log.error("The root category is missing: " + e.getMessage());
                return -1;
            }
        }
    }

    /**
     * Checks if the given keyword is valid (i.e. represents either a registered item or a registered category)
     */
    @Override
    public boolean isKeywordValid(String keyword) {

        // Is the keyword an item?
        if(containsItem(keyword)) {
            return true;
        }

        // Or maybe a category ?
        else {
            IItemTreeCategory category = getCategory(keyword);
            return category != null;
        }
    }

    /**
     * Returns a reference to all categories.
     */
    
    @Override
    public Collection<IItemTreeCategory> getAllCategories() {
        return categories.values();
    }

    @Override
    public IItemTreeCategory getRootCategory() {
        return categories.get(rootCategory);
    }

    @Override
    public void setRootCategory( IItemTreeCategory category) {
        rootCategory = category.getName();
        categories.put(rootCategory, category);
    }

    @Override
    public IItemTreeCategory getCategory(String keyword) {
        return categories.get(keyword);
    }

    @Override
    public boolean isItemUnknown(String id, int damage) {
        return itemsById.get(id) == null;
    }

    
    @Override
    public List<IItemTreeItem> getItems( String id, int damage,  CompoundNBT extra) {
        if(id == null) {
            return new ArrayList<>();
        }

        List<IItemTreeItem> items = itemsById.get(id);
               
        List<IItemTreeItem> filteredItems = new ArrayList<>();
        if(items != null) {
            filteredItems.addAll(items);
        }

        // Filter items of same ID, but different damage value
        if(items != null && !items.isEmpty()) {
            items.stream().filter(item -> item.getDamage() != InvTweaksConst.DAMAGE_WILDCARD && item.getDamage() != damage).forEach(filteredItems::remove);
        }

        items = filteredItems;
        filteredItems = new ArrayList<>(items);

        // Filter items that don't match extra data
        if(extra != null && !items.isEmpty()) {
            items.stream().filter(item -> !NBTUtil.areNBTEquals(item.getExtraData(), extra, true)).forEach(filteredItems::remove);
        }

        // If there's no matching item, create new ones
        if(filteredItems.isEmpty()) {
        	//Make them all sort in the same sort pool at the end.
            int newItemOrder = Integer.MAX_VALUE;
        	//int newItemOrder = highestOrder + 1;
             IItemTreeItem newItemId = new InvTweaksItemTreeItem(String.format("%s-%d", id, damage), id, damage, null,
                    newItemOrder, getRootCategory().getName() + "\\_uncategorized\\" + String.format("%s-%d", id, damage));
             IItemTreeItem newItemDamage = new InvTweaksItemTreeItem(id, id,
                    InvTweaksConst.DAMAGE_WILDCARD, null, newItemOrder, getRootCategory().getName() + "\\_uncategorized\\" + id);
            addItem(getRootCategory().getName(), newItemId);
            addItem(getRootCategory().getName(), newItemDamage);
            filteredItems.add(newItemId);
            filteredItems.add(newItemDamage);
        }

        filteredItems.removeIf(Objects::isNull);

        return filteredItems;

    }

    
    @Override
    public List<IItemTreeItem> getItems(String id, int damage) {
        return getItems(id, damage, null);
    }

    @Override
    public List<IItemTreeItem> getItems(String name) {
        return itemsByName.get(name);
    }

    public int getItemOrder(ItemStack itemStack) {
        List<IItemTreeItem> items = getItems(itemStack.getItem().getRegistryName().toString(),
            itemStack.getDamage(), itemStack.getTag());
        return (items.size() > 0) ? items.get(0).getOrder() : Integer.MAX_VALUE;
    }

    
    @Override
    public IItemTreeItem getRandomItem( Random r) {
        return (IItemTreeItem) itemsByName.values().toArray()[r.nextInt(itemsByName.size())];
    }

    @Override
    public boolean containsItem(String name) {
        return itemsByName.containsKey(name);
    }

    @Override
    public boolean containsCategory(String name) {
        return categories.containsKey(name);
    }

    
    @Override
    public IItemTreeCategory addCategory(String parentCategory, String newCategory) throws NullPointerException {
         IItemTreeCategory addedCategory = new InvTweaksItemTreeCategory(newCategory);
        addCategory(parentCategory, addedCategory);
        return addedCategory;
    }

    
    @Override
    public IItemTreeItem addItem(String parentCategory, String name, String id, int damage, int order)
            throws NullPointerException {
        return addItem(parentCategory, name, id, damage, null, order);
    }

    
    @Override
    public IItemTreeItem addItem(String parentCategory, String name, String id, int damage, CompoundNBT extra, int order)
            throws NullPointerException {
         InvTweaksItemTreeItem addedItem = new InvTweaksItemTreeItem(name, id, damage, extra, order, getRootCategory().findKeywordPath(parentCategory) + "\\" + name);
        addItem(parentCategory, addedItem);
        return addedItem;
    }

    @Override
    public void addCategory(String parentCategory,  IItemTreeCategory newCategory) throws NullPointerException {
        // Build tree
        categories.get(parentCategory).addCategory(newCategory);

        // Register category
        categories.put(newCategory.getName(), newCategory);
    }

    @Override
    public void addItem(String parentCategory,  IItemTreeItem newItem) throws NullPointerException {
        highestOrder = Math.max(highestOrder, newItem.getOrder());

        // Build tree
        categories.get(parentCategory).addItem(newItem);

        // Register item
        if(itemsByName.containsKey(newItem.getName())) {
            itemsByName.get(newItem.getName()).add(newItem);
        } else {
             List<IItemTreeItem> list = new ArrayList<>();
            list.add(newItem);
            itemsByName.put(newItem.getName(), list);
        }
        
        if(itemsById.containsKey(newItem.getId())) {
            itemsById.get(newItem.getId()).add(newItem);
        } else {
             List<IItemTreeItem> list = new ArrayList<>();
            list.add(newItem);
            itemsById.put(newItem.getId(), list);
        }
    }

    public int getHighestOrder() {
        return highestOrder;
    }

    public int getLastTreeOrder() {
        return lastTreeOrder;
    }

    @Override
    public void registerOre(String category, String name, String oreName, int order, String path) {
    	try {
	    	ResourceLocation tagId = new ResourceLocation(oreName.toLowerCase());
	    	
	        for( Item i : ItemTags.getCollection().getTagByID(tagId).getAllElements()) {
	            if(i != null) {
	                addItem(category,
	                        new InvTweaksItemTreeItem(name, i.getRegistryName().toString(), InvTweaksConst.DAMAGE_WILDCARD, null, order, path));
	                log.info(String.format("An OreDictionary entry for %s is named %s", oreName, i.getRegistryName().toString()));
	            } else {
	                log.warn(String.format("An OreDictionary entry for %s is null", oreName));
	            }
	        }
	        oresRegistered.add(new OreDictInfo(category, name, oreName, order, path));
    	} catch (Exception ex) {
    		log.warn(String.format("An OreDictionary name '%s' contains invalid characters.", oreName));
    	}
    	
    }

    //ATB: I hope I don't need this any more...
    /*@SubscribeEvent
    public void tagRegistered( ItemTags ev) {
        // TODO: It looks like Mojang changed the internal name type to ResourceLocation. Evaluate how much of a pain that will be.
        oresRegistered.stream().filter(ore -> ore.oreName.equals(ev.getName())).forEach(ore -> {
             ItemStack evOre = ev.getOre();
            if(!evOre.isEmpty()) {
                // TODO: It looks like Mojang changed the internal name type to ResourceLocation. Evaluate how much of a pain that will be.
                addItem(ore.category, new InvTweaksItemTreeItem(ore.name, evOre.getItem().getRegistryName().toString(),
                        evOre.getItemDamage(), null, ore.order, ore.orePath));
            } else {
                log.warn(String.format("An OreDictionary entry for %s is null", ev.getName()));
            }
        });
    }
    */
    
    public void registerClass(String category, String name, String className, CompoundNBT extraData, int order, String path)
    {
        if (allGameItems.size() == 0)
        {
            populateGameItems();
        }
        
        for(ItemStack stack : allGameItems)
        {
            Item item = stack.getItem();
            boolean isClass = InstanceOfClassNameKind(item, className);
            if (isClass) {
                boolean doIt = true;
                if (extraData != null) {
                    if (doIt && extraData.contains("toolclass")) 
                    {
                        String tclass = extraData.getString("toolclass");
                        //We don't want the set, we want the one we will use during comparisons.
                        //An empty toolclass will match non-tools.                        
                        doIt = tclass.equals(IngredientUtils.getToolClass(stack));                        
                    }
                    if (doIt && extraData.contains("armortype") && item instanceof ArmorItem) 
                    {
                        ArmorItem armor = (ArmorItem) item;
                        String keyArmorType = extraData.getString("armortype");
                        String itemArmorType = armor.getEquipmentSlot().getName().toLowerCase();
                        doIt = (keyArmorType.equals(itemArmorType));
                        armor = null;
                    }
                    if (doIt && extraData.contains("isshield")) 
                    {                        
                        doIt = item.isShield(stack, null);
                    }
                }
                //Checks out, add it to the tree:
                if (doIt) {
                    int dmg = item.isDamageable() ? InvTweaksConst.DAMAGE_WILDCARD : stack.getDamage();
                    addItem(category,
                            new InvTweaksItemTreeItem(name, item.getRegistryName().toString(), dmg, null, order, path));
                }
            }
        }
    }

    private static class OreDictInfo {
        String category;
        String name;
        String oreName;
        int order;
        String orePath;

        OreDictInfo(String category_, String name_, String oreName_, int order_, String orePath_) {
            category = category_;
            name = name_;
            oreName = oreName_;
            order = order_;
            orePath = orePath_;
        }
    }
    
    private void populateGameItems()
    {
        for (Entry<RegistryKey<Item>, Item> entry : ForgeRegistries.ITEMS.getEntries())
        {
            //getDataForItemSubtypes(itemDump, entry.getValue(), entry.getKey(), includeToolClass, dumpNBT);
            Item item = entry.getValue();
            
            allGameItems.add(item.getDefaultInstance());
            //addData(itemDump, item, rl, false, includeToolClass, dumpNBT, new ItemStack(item, 1, 0));
        }
    }
    
    private boolean InstanceOfClassNameKind(Object o, String className)
    {
        Class testClass = o.getClass();
        while (testClass != null)
        {
            if (testClass.getName().toLowerCase().endsWith(className))
                return true;
            //The secret sauce:
            testClass = testClass.getSuperclass();
        }
        return false;
    }
    
    public void endFileRead()
    {
        //We are done with this, let's release the memory.
        allGameItems.clear();
        
        //Remember where the last entry was placed in the tree for the API to leave these unsorted.
        lastTreeOrder = highestOrder;
    }
}
