package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import java.util.Collection;

import mezz.jei.gui.Focus;

public interface IIngredientHelper<T> {
	Collection<T> expandSubtypes(Collection<T> contained);

	T getMatch(Iterable<T> contained, @Nonnull Focus toMatch);
}
