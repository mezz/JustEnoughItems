package mezz.jei.gui.ingredients;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.config.IIngredientFilterConfig;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Unmodifiable;

public interface IListElementInfo<V> {

	String getName();

	String getModNameForSorting();

	Set<String> getModNameStrings();

	@Unmodifiable
	List<String> getTooltipStrings(IIngredientFilterConfig config, IIngredientManager ingredientManager);

	Collection<String> getTagStrings(IIngredientManager ingredientManager);

	Collection<ResourceLocation> getTagIds(IIngredientManager ingredientManager);

	Collection<String> getCreativeTabsStrings(IIngredientManager ingredientManager);

	Iterable<Integer> getColors(IIngredientManager ingredientManager);

	ResourceLocation getResourceLocation();

	IListElement<V> getElement();

	ITypedIngredient<V> getTypedIngredient();

	void setSortedIndex(int sortIndex);

	int getSortedIndex();

}
