package mezz.jei.ingredients;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;

public interface IIngredientListElementInfo<V> {

	String getName();

	String getModNameForSorting();

	Set<String> getModNameStrings();

	List<String> getTooltipStrings(IIngredientFilterConfig config, IIngredientManager ingredientManager);

	Collection<String> getTagStrings(IIngredientManager ingredientManager);

	Collection<String> getCreativeTabsStrings(IIngredientManager ingredientManager);

	Collection<String> getColorStrings(IIngredientManager ingredientManager);

	String getResourceId();

	IIngredientListElement<V> getElement();

	int getOrderIndex();
	
	int getDamage();
	
	int getMaxDamage();
	
	boolean isTool();
	
	String getToolClass();
	
	int getHarvestLevel();
	
	int getToolDurability();
	
	boolean isWeapon();
	
	Double getAttackDamage();
	
	Double getAttackSpeed();
	
	int getWeaponDurability();
	
	boolean isArmor();
	
	int getArmorSlotIndex();
	
	int getArmorDamageReduce();
	
	float getArmorToughness();
	
	int getArmorDurability();
	
	String getTagForSorting();
	
	boolean hasTag();

}
