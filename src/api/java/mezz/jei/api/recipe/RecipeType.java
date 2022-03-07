package mezz.jei.api.recipe;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceLocation;

/**
 * Identifies a type of recipe, (i.e. Crafting Table Recipe, Furnace Recipe).
 * Each {@link IRecipeCategory} can be uniquely identified by its {@link RecipeType}.
 *
 * This class replaces the {@link ResourceLocation} used in {@link IRecipeCategory#getUid()}
 *
 * @see RecipeTypes for all the built-in recipe types that are added by JEI.
 *
 * @since 9.5.0
 */
@SuppressWarnings("removal")
public final class RecipeType<T> {
	public static <T> RecipeType<T> create(String nameSpace, String path, Class<? extends T> recipeClass) {
		ResourceLocation uid = new ResourceLocation(nameSpace, path);
		return new RecipeType<>(uid, recipeClass);
	}

	private final ResourceLocation uid;
	private final Class<? extends T> recipeClass;

	public RecipeType(ResourceLocation uid, Class<? extends T> recipeClass) {
		this.uid = uid;
		this.recipeClass = recipeClass;
	}

	/**
	 * The unique id of this recipe type.
	 */
	public ResourceLocation getUid() {
		return uid;
	}

	/**
	 * The class of recipes represented by this recipe type.
	 */
	public Class<? extends T> getRecipeClass() {
		return recipeClass;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof RecipeType<?> other)) {
			return false;
		}
		return this.recipeClass == other.recipeClass &&
			this.uid.equals(other.uid);
	}

	@Override
	public int hashCode() {
		return 31 * uid.hashCode() + recipeClass.hashCode();
	}

	@Override
	public String toString() {
		return "RecipeType[" +
			"uid=" + uid + ", " +
			"recipeClass=" + recipeClass + ']';
	}

}
