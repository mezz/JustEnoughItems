package mezz.jei.ingredients;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Unmodifiable;

public interface IIngredientListElementInfo<V> {

	String getName();

	String getModNameForSorting();

	Set<String> getModNameStrings();

	@Unmodifiable
	List<String> getTooltipStrings(IIngredientFilterConfig config, IIngredientManager ingredientManager);

	Collection<String> getTagStrings(IIngredientManager ingredientManager);

	Collection<ResourceLocation> getTagIds(IIngredientManager ingredientManager);

	Collection<String> getCreativeTabsStrings(IIngredientManager ingredientManager);

	Collection<String> getColorStrings(IIngredientManager ingredientManager);

	ResourceLocation getResourceLocation();

	IIngredientListElement<V> getElement();

	ITypedIngredient<V> getTypedIngredient();

	void setSortedIndex(int sortIndex);

	int getSortedIndex();

}
