package mezz.jei.gui.ingredients;

import javax.annotation.Nullable;
import java.util.Collection;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.gui.Focus;

public interface IIngredientHelper<T> {
	Collection<T> expandSubtypes(Collection<T> contained);

	@Nullable
	T getMatch(Iterable<T> ingredients, IFocus<T> toMatch);

	Focus<T> createFocus(T ingredient);
}
