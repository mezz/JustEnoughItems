package mezz.jei.library.gui.helpers;

import com.mojang.datafixers.util.Pair;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CraftingGridHelper implements ICraftingGridHelper {
	public static final CraftingGridHelper INSTANCE = new CraftingGridHelper();

	private CraftingGridHelper() {}

	@Override
	public List<IRecipeSlotBuilder> createAndSetNamedIngredients(IRecipeLayoutBuilder builder, List<Pair<String, Ingredient>> namedIngredients, int width, int height) {
		List<IRecipeSlotBuilder> inputSlots = createInputSlots(builder, width, height);
		setNamedIngredients(inputSlots, namedIngredients, width, height);
		return inputSlots;
	}

	@Override
	public <T> List<IRecipeSlotBuilder> createAndSetNamedInputs(IRecipeLayoutBuilder builder, IIngredientType<T> ingredientType, List<@Nullable Pair<String, List<@Nullable T>>> namedInputs, int width, int height) {
		List<IRecipeSlotBuilder> inputSlots = createInputSlots(builder, width, height);
		setNamedInputs(inputSlots, ingredientType, namedInputs, width, height);
		return inputSlots;
	}

	@Override
	public void createAndSetIngredients(IRecipeLayoutBuilder builder, List<Ingredient> ingredients, int width, int height) {
		List<IRecipeSlotBuilder> inputSlots = createInputSlots(builder, width, height);
		setIngredients(inputSlots, ingredients, width, height);
	}

	@Override
	public <T> List<IRecipeSlotBuilder> createAndSetInputs(IRecipeLayoutBuilder builder, IIngredientType<T> ingredientType, List<@Nullable List<@Nullable T>> inputs, int width, int height) {
		List<IRecipeSlotBuilder> inputSlots = createInputSlots(builder, width, height);
		setInputs(inputSlots, ingredientType, inputs, width, height);
		return inputSlots;
	}

	public void setIngredients(List<IRecipeSlotBuilder> slotBuilders, List<Ingredient> ingredients, int width, int height) {
		if (width <= 0 || height <= 0) {
			width = height = getShapelessSize(ingredients.size());
		}
		if (slotBuilders.size() < width * height) {
			throw new IllegalArgumentException(String.format("There are not enough slots (%s) to hold a recipe of this size. (%sx%s)", slotBuilders.size(), width, height));
		}

		for (int i = 0; i < ingredients.size(); i++) {
			int index = getCraftingIndex(i, width, height);
			IRecipeSlotBuilder slot = slotBuilders.get(index);

			Ingredient ingredient = ingredients.get(i);
			if (ingredient != null) {
				slot.addIngredients(ingredient);
			}
		}
	}

	@Override
	public <T> void setInputs(List<IRecipeSlotBuilder> slotBuilders, IIngredientType<T> ingredientType, List<@Nullable List<@Nullable T>> inputs, int width, int height) {
		if (width <= 0 || height <= 0) {
			width = height = getShapelessSize(inputs.size());
		}
		if (slotBuilders.size() < width * height) {
			throw new IllegalArgumentException(String.format("There are not enough slots (%s) to hold a recipe of this size. (%sx%s)", slotBuilders.size(), width, height));
		}

		for (int i = 0; i < inputs.size(); i++) {
			int index = getCraftingIndex(i, width, height);
			IRecipeSlotBuilder slot = slotBuilders.get(index);

			@Nullable List<@Nullable T> ingredients = inputs.get(i);
			if (ingredients != null) {
				slot.addIngredients(ingredientType, ingredients);
			}
		}
	}

	@Override
	public <T> IRecipeSlotBuilder createAndSetOutputs(IRecipeLayoutBuilder builder, IIngredientType<T> ingredientType, @Nullable List<@Nullable T> outputs) {
		IRecipeSlotBuilder outputSlot = builder.addOutputSlot(95, 19)
			.setOutputSlotBackground();
		if (outputs != null) {
			outputSlot.addIngredients(ingredientType, outputs);
		}
		return outputSlot;
	}

	private static List<IRecipeSlotBuilder> createInputSlots(IRecipeLayoutBuilder builder, int width, int height) {
		if (width <= 0 || height <= 0) {
			builder.setShapeless();
		}

		List<IRecipeSlotBuilder> inputSlots = new ArrayList<>();
		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 3; ++x) {
				IRecipeSlotBuilder slot = builder.addInputSlot(x * 18 + 1, y * 18 + 1)
					.setStandardSlotBackground();
				inputSlots.add(slot);
			}
		}
		return inputSlots;
	}

	private static void setNamedIngredients(List<IRecipeSlotBuilder> slotBuilders, List<Pair<String, Ingredient>> namedIngredients, int width, int height) {
		if (width <= 0 || height <= 0) {
			width = height = getShapelessSize(namedIngredients.size());
		}
		if (slotBuilders.size() < width * height) {
			throw new IllegalArgumentException(String.format("There are not enough slots (%s) to hold a recipe of this size. (%sx%s)", slotBuilders.size(), width, height));
		}

		for (int i = 0; i < namedIngredients.size(); i++) {
			int index = getCraftingIndex(i, width, height);
			IRecipeSlotBuilder slot = slotBuilders.get(index);

			Pair<String, Ingredient> value = namedIngredients.get(i);
			if (value != null) {
				slot.setSlotName(value.getFirst())
					.addIngredients(value.getSecond());
			}
		}
	}

	private <T> void setNamedInputs(List<IRecipeSlotBuilder> slotBuilders, IIngredientType<T> ingredientType, List<@Nullable Pair<String, List<@Nullable T>>> namedInputs, int width, int height) {
		if (width <= 0 || height <= 0) {
			width = height = getShapelessSize(namedInputs.size());
		}
		if (slotBuilders.size() < width * height) {
			throw new IllegalArgumentException(String.format("There are not enough slots (%s) to hold a recipe of this size. (%sx%s)", slotBuilders.size(), width, height));
		}

		for (int i = 0; i < namedInputs.size(); i++) {
			int index = getCraftingIndex(i, width, height);
			IRecipeSlotBuilder slot = slotBuilders.get(index);

			Pair<String, List<@Nullable T>> value = namedInputs.get(i);
			if (value != null) {
				slot.setSlotName(value.getFirst())
					.addIngredients(ingredientType, value.getSecond());
			}
		}
	}

	public static Map<Integer, Ingredient> getGuiSlotToIngredientMap(RecipeHolder<CraftingRecipe> recipeHolder, int width, int height) {
		CraftingRecipe recipe = recipeHolder.value();
		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		if (width <= 0 || height <= 0) {
			width = height = getShapelessSize(ingredients.size());
		}

		Map<Integer, Ingredient> result = new LinkedHashMap<>(ingredients.size());
		for (int i = 0; i < ingredients.size(); i++) {
			Ingredient ingredient = ingredients.get(i);
			if (!ingredient.isEmpty()) {
				int craftingIndex = CraftingGridHelper.getCraftingIndex(i, width, height);
				result.put(craftingIndex, ingredient);
			}
		}
		return result;
	}

	private static int getShapelessSize(int total) {
		if (total > 4) {
			return 3;
		} else if (total > 1) {
			return 2;
		} else {
			return 1;
		}
	}

	private static int getCraftingIndex(int i, int width, int height) {
		int index;
		if (width == 1) {
			if (height == 3) {
				index = (i * 3) + 1;
			} else if (height == 2) {
				index = (i * 3) + 1;
			} else {
				index = 4;
			}
		} else if (height == 1) {
			index = i + 3;
		} else if (width == 2) {
			index = i;
			if (i > 1) {
				index++;
				if (i > 3) {
					index++;
				}
			}
		} else if (height == 2) {
			index = i + 3;
		} else {
			index = i;
		}
		return index;
	}

}
