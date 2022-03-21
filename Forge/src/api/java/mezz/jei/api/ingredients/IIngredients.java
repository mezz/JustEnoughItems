package mezz.jei.api.ingredients;

import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.world.item.crafting.Ingredient;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import org.jetbrains.annotations.Nullable;

/**
 * This holds a recipe's inputs and outputs in a standardized format.
 *
 * It is filled out by {@link IRecipeCategory#setIngredients(Object, IIngredients)}, and then used by JEI to figure out
 * what items are in the recipe, for lookups.
 * It is also passed back to {@link IRecipeCategory#setRecipe(IRecipeLayout, Object, IIngredients)}
 * where it can be used to set ingredient groups in the recipe layout with {@link IGuiIngredientGroup#set(IIngredients)}
 *
 * @deprecated Use the new {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)},
 * which does not use this class.
 */
@Deprecated(forRemoval = true, since = "9.3.0")
public interface IIngredients {
	/**
	 * Sets the recipe's inputs. Each input ingredient represents one slot.
	 *
	 * @deprecated Use the new {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)},
	 * which does not use this class.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	void setInputIngredients(List<Ingredient> inputs);

	/**
	 * Sets a single recipe input. For recipes with only one input slot.
	 *
	 * @param ingredientType The type of ingredient: {@link VanillaTypes#ITEM}, {@link VanillaTypes#FLUID}, etc
	 * @param input          The list of ingredients representing each input slot.
	 *
	 * @deprecated Use the new {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)},
	 * which does not use this class.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	<T> void setInput(IIngredientType<T> ingredientType, T input);

	/**
	 * Sets the recipe's inputs. Each list element represents one slot.
	 *
	 * @param ingredientType The type of ingredient: {@link VanillaTypes#ITEM}, {@link VanillaTypes#FLUID}, etc
	 * @param input          The list of ingredients representing each input slot.
	 *
	 * @deprecated Use the new {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)},
	 * which does not use this class.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	<T> void setInputs(IIngredientType<T> ingredientType, List<@Nullable T> input);

	/**
	 * Sets the recipe's inputs. Each input list represents one slot.
	 * Accepts multiple ingredients per slot.
	 *
	 * @param ingredientType The type of ingredient: {@link VanillaTypes#ITEM}, {@link VanillaTypes#FLUID}, etc
	 * @param inputs         The outer list represents the slot, the inner list is a rotating list of ingredients in that slot.
	 *
	 * @deprecated Use the new {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)},
	 * which does not use this class.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	<T> void setInputLists(IIngredientType<T> ingredientType, List<@Nullable List<@Nullable T>> inputs);

	/**
	 * Sets a single recipe output.
	 *
	 * @param ingredientType The type of ingredient: {@link VanillaTypes#ITEM}, {@link VanillaTypes#FLUID}, etc
	 * @param output         The single ingredient representing the recipe output.
	 *
	 * @deprecated Use the new {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)},
	 * which does not use this class.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	<T> void setOutput(IIngredientType<T> ingredientType, T output);

	/**
	 * Sets multiple recipe outputs. Each list element represents one slot.
	 *
	 * @param ingredientType The type of ingredient: {@link VanillaTypes#ITEM}, {@link VanillaTypes#FLUID}, etc
	 * @param outputs        The list of ingredients representing each output slot.
	 *
	 * @deprecated Use the new {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)},
	 * which does not use this class.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	<T> void setOutputs(IIngredientType<T> ingredientType, List<@Nullable T> outputs);

	/**
	 * Sets the recipe's outputs. Each output list represents one slot.
	 * Accepts multiple ingredients per slot.
	 *
	 * @param ingredientType The type of ingredient: {@link VanillaTypes#ITEM}, {@link VanillaTypes#FLUID}, etc
	 * @param outputs        The outer list represents the slot, the inner list is a rotating list of ingredients in that slot.
	 *
	 * @deprecated Use the new {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)},
	 * which does not use this class.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	<T> void setOutputLists(IIngredientType<T> ingredientType, List<@Nullable List<@Nullable T>> outputs);

	/**
	 * Get all the inputs that have been set for the ingredientClass.
	 * Each list element represents one slot. The inner list represents the ingredient(s) in the slot.
	 *
	 * @deprecated Use the new {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)},
	 * which does not use this class.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	<T> List<@Nullable List<@Nullable T>> getInputs(IIngredientType<T> ingredientType);

	/**
	 * Get all the outputs that have been set for the ingredientClass.
	 * Each list element represents one slot.
	 *
	 * @deprecated Use the new {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)},
	 * which does not use this class.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	<T> List<@Nullable List<@Nullable T>> getOutputs(IIngredientType<T> ingredientType);
}
