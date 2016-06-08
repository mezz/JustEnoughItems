package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import java.util.Collection;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.gui.Focus;

public interface IIngredientHelper<T> {
	Collection<T> expandSubtypes(Collection<T> contained);

	T getMatch(Iterable<T> ingredients, @Nonnull IFocus<T> toMatch);

	@Nonnull
	Focus<T> createFocus(@Nonnull T ingredient);
}
