package mezz.jei.library.plugins.jei.tags;

import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.tags.TagKey;

import java.util.List;

public interface ITagInfoRecipe {
	TagKey<?> getTag();

	List<ITypedIngredient<?>> getTypedIngredients();
}
