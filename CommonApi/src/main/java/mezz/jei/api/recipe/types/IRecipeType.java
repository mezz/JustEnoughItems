package mezz.jei.api.recipe.types;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Identifies a type of recipe, (i.e. Crafting Table Recipe, Furnace Recipe).
 * Each {@link IRecipeCategory} can be uniquely identified by its {@link IRecipeType}.
 *
 * Unfortunately, the vanilla {@link RecipeType} only works for recipes that extend the vanilla {@link Recipe} class,
 * so this more general version is needed for modded recipes in JEI.
 *
 * @see RecipeTypes for all the built-in recipe types that are added by JEI.
 * @see IRecipeHolderType for a convenient {@link IRecipeType} created from a vanilla {@link RecipeType}
 *
 * @since 20.0.0
 *
 * @apiNote Replaces RecipeType in 20.0.0 to avoid naming collision with the vanilla {@link RecipeType}
 */
public interface IRecipeType<T> {
	/**
	 * The unique id of this recipe type.
	 *
	 * @since 20.0.0
	 */
	Identifier getUid();

	/**
	 * The class of recipes represented by this recipe type.
	 *
	 * @since 20.0.0
	 */
	Class<? extends T> getRecipeClass();

	/**
	 * Create a JEI RecipeType from a Vanilla RecipeType.
	 * Returns a RecipeType that uses {@link RecipeHolder} to hold recipes.
	 * @since 20.0.0
	 */
	static <R extends Recipe<?>> IRecipeHolderType<R> create(RecipeType<R> vanillaRecipeType) {
		return IRecipeHolderType.create(vanillaRecipeType);
	}

	/**
	 * Create a JEI RecipeType from a given uid.
	 * Returns a RecipeType that uses the given recipe class to hold recipes.
	 * @since 20.0.0
	 */
	static <T> IRecipeType<T> create(Identifier uid, Class<? extends T> recipeClass) {
		return new JeiRecipeType<>(uid, recipeClass);
	}

	/**
	 * Convenience function create a JEI RecipeType from a given nameSpace and path.
	 * Returns a RecipeType that uses the given recipe class to hold recipes.
	 * @since 20.0.0
	 */
	static <T> IRecipeType<T> create(String nameSpace, String path, Class<? extends T> recipeClass) {
		Identifier uid = Identifier.fromNamespaceAndPath(nameSpace, path);
		return create(uid, recipeClass);
	}

	record JeiRecipeType<T>(Identifier uid, Class<? extends T> recipeClass) implements IRecipeType<T> {
		@Override
		public Identifier getUid() {
			return uid;
		}

		@Override
		public Class<? extends T> getRecipeClass() {
			return recipeClass;
		}
	}
}
