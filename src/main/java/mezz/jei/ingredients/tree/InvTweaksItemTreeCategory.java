package mezz.jei.ingredients.tree;


import mezz.jei.api.ingredients.tree.IItemTreeCategory;
import mezz.jei.api.ingredients.tree.IItemTreeItem;
//import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Representation of a category in the item tree, i.e. a group of items.
 *
 * @author Jimeo Wan
 */
public class InvTweaksItemTreeCategory implements IItemTreeCategory {
    private final Map<String, List<IItemTreeItem>> items = new HashMap<>();
    private final List<String> matchingItems = new ArrayList<>();
    private final List<IItemTreeCategory> subCategories = new ArrayList<>();
    private String name;
    private int order = -1;

    public InvTweaksItemTreeCategory(String name_) {
        name = name_;
    }

    @Override
    public boolean contains( IItemTreeItem item) {
        List<IItemTreeItem> storedItems = items.get(item.getId());
        if(storedItems != null) {
            for( IItemTreeItem storedItem : storedItems) {
                if(storedItem.equals(item)) {
                    return true;
                }
            }
        }
        for( IItemTreeCategory category : subCategories) {
            if(category.contains(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addCategory(IItemTreeCategory category) {
        subCategories.add(category);
    }

    @Override
    public void addItem( IItemTreeItem item) {

        // Add item to category
        if(items.get(item.getId()) == null) {
             List<IItemTreeItem> itemList = new ArrayList<>();
            itemList.add(item);
            items.put(item.getId(), itemList);
        } else {
            items.get(item.getId()).add(item);
        }
        matchingItems.add(item.getName());

        // Categorie's order is defined by its lowest item order
        if(order == -1 || order > item.getOrder()) {
            order = item.getOrder();
        }
    }

    @Override
    public int getCategoryOrder() {
        if(this.order != -1) {
            return this.order;
        } else {
            int catOrder;
            for( IItemTreeCategory category : subCategories) {
                catOrder = category.getCategoryOrder();
                if(catOrder != -1) {
                    return catOrder;
                }
            }
            return -1;
        }
    }

    @Override
    public int findCategoryOrder( String keyword) {
        if(keyword.equals(name)) {
            return getCategoryOrder();
        } else {
            int result;
            for( IItemTreeCategory category : subCategories) {
                result = category.findCategoryOrder(keyword);
                if(result != -1) {
                    return result;
                }
            }
            return -1;
        }
    }

    @Override
    public int findKeywordDepth(String keyword) {
        if(name.equals(keyword)) {
            return 0;
        } else if(matchingItems.contains(keyword)) {
            return 1;
        } else {
            int result;
            for( IItemTreeCategory category : subCategories) {
                result = category.findKeywordDepth(keyword);
                if(result != -1) {
                    return result + 1;
                }
            }
            return -1;
        }
    }
    
    @Override
    public String findKeywordPath(String keyword) {
        if(name.equals(keyword)) {
            return keyword;
        } else if(matchingItems.contains(keyword)) {
            return name + "\\" + keyword;
        } else {
            String result;
            for( IItemTreeCategory category : subCategories) {
                result = category.findKeywordPath(keyword);
                if(result.length() > 0) {
                    return name + "\\" + result;
                }
            }
            return "";
        }
    }

    /**
     * @return all categories contained in this one.
     */
    
    @Override
    public Collection<IItemTreeCategory> getSubCategories() {
        return subCategories;
    }

    
    @Override
    public Collection<List<IItemTreeItem>> getItems() {
        return items.values();
    }

    @Override
    public String getName() {
        return name;
    }

    
    public String toString() {
        return name + " (" + subCategories.size() +
                " cats, " + items.size() + " items)";
    }

}
