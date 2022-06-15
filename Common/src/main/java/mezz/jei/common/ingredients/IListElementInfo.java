package mezz.jei.common.ingredients;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.gui.ingredients.IListElement;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Unmodifiable;

public interface IListElementInfo<V> {

	String getName();

	String getModNameForSorting();

	Set<String> getModNameStrings();

	@Unmodifiable
	List<String> getTooltipStrings(IIngredientFilterConfig config, RegisteredIngredients registeredIngredients);

	Collection<String> getTagStrings(RegisteredIngredients registeredIngredients);

	Collection<ResourceLocation> getTagIds(RegisteredIngredients registeredIngredients);

	Collection<String> getCreativeTabsStrings(RegisteredIngredients registeredIngredients);

	Collection<String> getColorStrings(RegisteredIngredients registeredIngredients);

	ResourceLocation getResourceLocation();

	IListElement<V> getElement();

	ITypedIngredient<V> getTypedIngredient();

	void setSortedIndex(int sortIndex);

	int getSortedIndex();

}
