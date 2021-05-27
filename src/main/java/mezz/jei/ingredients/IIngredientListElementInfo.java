package mezz.jei.ingredients;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;
import net.minecraft.util.ResourceLocation;

public interface IIngredientListElementInfo<V> {

	String getName();

	String getModNameForSorting();

	Set<String> getModNameStrings();

	List<String> getTooltipStrings(IIngredientFilterConfig config, IIngredientManager ingredientManager);

	Collection<String> getTagStrings(IIngredientManager ingredientManager);

	Collection<ResourceLocation> getTagIds(IIngredientManager ingredientManager);

	Collection<String> getCreativeTabsStrings(IIngredientManager ingredientManager);

	Collection<String> getColorStrings(IIngredientManager ingredientManager);

	String getResourceId();

	IIngredientListElement<V> getElement();
}
