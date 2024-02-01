package mezz.jei.ingredients;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.gui.ingredients.IIngredientListElement;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraftforge.common.ToolType;

public class IngredientUtils {
    private static Boolean nullToolClassWarned = false;

	public static String getToolClass(ItemStack itemStack)
	{
		//I think I should find a way to cache this.
		if (itemStack == null || itemStack == ItemStack.EMPTY) {
			return "";
		}
		Item item = itemStack.getItem();
		Set<ToolType> toolTypeSet = item.getToolTypes(itemStack);
		
		Set<String> toolClassSet = new HashSet<String>();

		for (ToolType toolClass: toolTypeSet) {
			if (toolClass == null) {
				//What kind of monster puts a null ToolClass instance into the toolTypes list?
				if (!nullToolClassWarned) {
					nullToolClassWarned = true;
					LogManager.getLogger().warn("Item '" + item.getRegistryName() + "' has a null tool class entry.");
				}
			} else if (toolClass.getName() != "sword") {
				//Swords are not "tools".
				toolClassSet.add(toolClass.getName());
			}
		}

		//Minecraft hoes, shears, and fishing rods don't have tool class names.
		if (toolClassSet.isEmpty()) {
			if (item instanceof HoeItem) return "hoe";
			if (item instanceof ShearsItem) return "shears";
			if (item instanceof FishingRodItem) return "fishingrod";
			return "";
		}
		
		//Get the only thing.
		if (toolClassSet.size() == 1) {
			return (String) toolClassSet.toArray()[0];
		}
		
		//We have a preferred type to list tools under, primarily the pickaxe for harvest level.
		String[] prefOrder = {"pickaxe", "axe", "shovel", "hoe", "shears", "wrench"};
		for (int i = 0; i < prefOrder.length; i++) {
			if (toolClassSet.contains(prefOrder[i])) {
				return prefOrder[i];
			}
		}
		
		//Whatever happens to be the first thing:
		return (String) toolClassSet.toArray()[0];
	}
    
    public static <V> ItemStack getItemStack(IIngredientListElementInfo<V> ingredientInfo) {
		IIngredientListElement<V> element = ingredientInfo.getElement();
		V ingredient = element.getIngredient();
		if (ingredient instanceof ItemStack) {
			return (ItemStack) ingredient;
		}
		return ItemStack.EMPTY;
	}

    public static <V> String getUniqueId(IIngredientListElementInfo<V> ingredientInfo) {
		IIngredientListElement<V> element = ingredientInfo.getElement();
        IngredientManager ingredientManager = Internal.getIngredientManager();
		V ingredient = element.getIngredient();
		return ingredientManager.getIngredientHelper(ingredient).getUniqueId(ingredient, UidContext.Ingredient);
    }

}
