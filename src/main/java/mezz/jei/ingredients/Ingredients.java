package mezz.jei.ingredients;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Ingredients implements IIngredients {
	private final List<IngredientsForType<?>> inputs = new ArrayList<>();
	private final List<IngredientsForType<?>> outputs = new ArrayList<>();

	@Override
	public <T> void setInput(IIngredientType<T> ingredientType, T input) {
		setInputs(ingredientType, Collections.singletonList(input));
	}

	@Override
	public <T> void setInputLists(IIngredientType<T> ingredientType, List<List<T>> inputs) {
		List<List<T>> expandedInputs = new ArrayList<>(inputs);
		setIngredients(ingredientType, this.inputs, expandedInputs);
	}

	@Override
	public void setInputIngredients(List<Ingredient> inputs) {
		List<List<ItemStack>> inputLists = new ArrayList<>();
		for (Ingredient input : inputs) {
			ItemStack[] stacks = input.getItems();
			List<ItemStack> expandedInput = Arrays.asList(stacks);
			inputLists.add(expandedInput);
		}
		setIngredients(VanillaTypes.ITEM, this.inputs, inputLists);
	}

	@Override
	public <T> void setInputs(IIngredientType<T> ingredientType, List<T> inputs) {
		List<List<T>> expandedInputs = new ArrayList<>();
		for (T input : inputs) {
			List<T> expandedInput = Collections.singletonList(input);
			expandedInputs.add(expandedInput);
		}
		setIngredients(ingredientType, this.inputs, expandedInputs);
	}

	@Override
	public <T> void setOutput(IIngredientType<T> ingredientType, T output) {
		setOutputs(ingredientType, Collections.singletonList(output));
	}

	@Override
	public <T> void setOutputs(IIngredientType<T> ingredientType, List<T> outputs) {
		List<List<T>> expandedOutputs = new ArrayList<>();
		for (T output : outputs) {
			List<T> expandedOutput = Collections.singletonList(output);
			expandedOutputs.add(expandedOutput);
		}
		setIngredients(ingredientType, this.outputs, expandedOutputs);
	}

	@Override
	public <T> void setOutputLists(IIngredientType<T> ingredientType, List<List<T>> outputs) {
		List<List<T>> expandedOutputs = new ArrayList<>(outputs);
		setIngredients(ingredientType, this.outputs, expandedOutputs);
	}

	@Override
	public <T> List<List<T>> getInputs(IIngredientType<T> ingredientType) {
		return getIngredients(ingredientType, this.inputs);
	}

	@Override
	public <T> List<List<T>> getOutputs(IIngredientType<T> ingredientType) {
		return getIngredients(ingredientType, this.outputs);
	}

	public List<IngredientsForType<?>> getInputIngredients() {
		return inputs;
	}

	public List<IngredientsForType<?>> getOutputIngredients() {
		return outputs;
	}

	private static <T> void setIngredients(IIngredientType<T> ingredientType, List<IngredientsForType<?>> ingredientsForTypes, List<List<T>> ingredients) {
		IngredientsForType<T> recipeIngredients = getIngredientsForType(ingredientType, ingredientsForTypes);
		if (recipeIngredients == null) {
			ingredientsForTypes.add(new IngredientsForType<>(ingredientType, ingredients));
		} else {
			recipeIngredients.setIngredients(ingredients);
		}
	}

	private static <T> List<List<T>> getIngredients(IIngredientType<T> ingredientType, List<IngredientsForType<?>> ingredientsForTypes) {
		IngredientsForType<T> recipeIngredients = getIngredientsForType(ingredientType, ingredientsForTypes);
		if (recipeIngredients == null) {
			return Collections.emptyList();
		}
		return recipeIngredients.getIngredients();
	}

	@Nullable
	private static <T> IngredientsForType<T> getIngredientsForType(IIngredientType<T> ingredientType, List<IngredientsForType<?>> ingredientsForTypes) {
		for (IngredientsForType<?> i : ingredientsForTypes) {
			if (i.getIngredientType() == ingredientType) {
				@SuppressWarnings("unchecked")
				IngredientsForType<T> ingredientsForType = (IngredientsForType<T>) i;
				return ingredientsForType;
			}
		}
		return null;
	}
}
