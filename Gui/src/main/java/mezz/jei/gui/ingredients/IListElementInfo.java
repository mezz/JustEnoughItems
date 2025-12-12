package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IIngredientFilterConfig;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface IListElementInfo<V> {

	List<String> getNames();

	String getModNameForSorting();

	List<String> getModNames();

	List<String> getModIds();

	@Unmodifiable
	Set<String> getTooltipStrings(IIngredientFilterConfig config, IIngredientManager ingredientManager);

	Collection<String> getTagStrings(IIngredientManager ingredientManager);

	Stream<Identifier> getTagIds(IIngredientManager ingredientManager);

	Iterable<Integer> getColors(IIngredientManager ingredientManager);

	@Unmodifiable
	Collection<String> getCreativeTabsStrings(IIngredientManager ingredientManager);

	Identifier getIdentifier();

	IListElement<V> getElement();

	ITypedIngredient<V> getTypedIngredient();

	int getCreatedIndex();
}
