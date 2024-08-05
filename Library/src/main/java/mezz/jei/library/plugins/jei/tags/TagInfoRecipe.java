package mezz.jei.library.plugins.jei.tags;

import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.tags.TagKey;

import java.util.Collections;
import java.util.List;

public class TagInfoRecipe<B, I> implements ITagInfoRecipe {
	private final TagKey<B> tag;
	private final List<ITypedIngredient<I>> ingredients;

	public TagInfoRecipe(TagKey<B> tag, List<ITypedIngredient<I>> ingredients) {
		this.tag = tag;
		this.ingredients = ingredients;
	}

	@Override
	public TagKey<B> getTag() {
		return tag;
	}

	@Override
	public List<ITypedIngredient<?>> getTypedIngredients() {
		return Collections.unmodifiableList(ingredients);
	}
}
