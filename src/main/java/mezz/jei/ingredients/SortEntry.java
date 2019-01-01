package mezz.jei.ingredients;

import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.ingredients.ISortableIngredient;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Comparator;

public class SortEntry<T> {
	private final ResourceLocation name;
	private final Comparator<ISortableIngredient<T>> comparator;
	@Nullable
	private final IIngredientType<T> ingredientType;

	public SortEntry(ResourceLocation name, Comparator<ISortableIngredient<T>> comparator) {
		this.name = name;
		this.comparator = comparator;
		this.ingredientType = null;
	}

	public SortEntry(ResourceLocation name, Comparator<T> comparator, IIngredientType<T> ingredientType) {
		this.name = name;
		this.comparator = (o1, o2) -> comparator.compare(o1.getIngredient(), o2.getIngredient());
		this.ingredientType = ingredientType;
	}

	@Nullable
	public IIngredientType<T> getIngredientType() {
		return ingredientType;
	}

	public ResourceLocation getName() {
		return name;
	}

	public Comparator<ISortableIngredient<T>> getComparator() {
		return comparator;
	}
}
