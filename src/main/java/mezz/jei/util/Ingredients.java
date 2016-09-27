package mezz.jei.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mezz.jei.IngredientRegistry;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredients;

public class Ingredients implements IIngredients {
	private final Map<Class, List<List>> inputs = new HashMap<Class, List<List>>();
	private final Map<Class, List> outputs = new HashMap<Class, List>();
	private boolean used = false; // check that the addon used this at all. legacy addons will not

	@Override
	public <T> void setInput(Class<T> ingredientClass, T input) {
		setInputs(ingredientClass, Collections.singletonList(input));
	}

	@Override
	public <T> void setInputLists(Class<T> ingredientClass, List<List<T>> inputs) {
		this.used = true;

		IngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientClass);
		List<List<T>> expandedInputs = new ArrayList<List<T>>();
		for (List<T> input : inputs) {
			List<T> itemStacks = ingredientHelper.expandSubtypes(input);
			expandedInputs.add(itemStacks);
		}

		//noinspection unchecked
		this.inputs.put(ingredientClass, (List<List>) (Object) expandedInputs);
	}

	@Override
	public <T> void setInputs(Class<T> ingredientClass, List<T> input) {
		this.used = true;

		IngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientClass);
		List<List<T>> expandedInputs = new ArrayList<List<T>>();
		for (T input1 : input) {
			List<T> itemStacks = ingredientHelper.expandSubtypes(Collections.singletonList(input1));
			expandedInputs.add(itemStacks);
		}

		//noinspection unchecked
		this.inputs.put(ingredientClass, (List<List>) (Object) expandedInputs);
	}

	@Override
	public <T> void setOutput(Class<T> ingredientClass, T output) {
		this.used = true;
		IngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientClass);
		List<T> expandedOutputs = ingredientHelper.expandSubtypes(Collections.singletonList(output));
		this.outputs.put(ingredientClass, expandedOutputs);
	}

	@Override
	public <T> void setOutputs(Class<T> ingredientClass, List<T> outputs) {
		this.used = true;
		IngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientClass);
		List<T> expandedOutputs = ingredientHelper.expandSubtypes(outputs);
		this.outputs.put(ingredientClass, expandedOutputs);
	}

	@Override
	public <T> List<List<T>> getInputs(Class<T> ingredientClass) {
		//noinspection unchecked
		List<List<T>> inputs = (List<List<T>>) (Object) this.inputs.get(ingredientClass);
		if (inputs == null) {
			return Collections.emptyList();
		}
		return inputs;
	}

	@Override
	public <T> List<T> getOutputs(Class<T> ingredientClass) {
		//noinspection unchecked
		List<T> outputs = this.outputs.get(ingredientClass);
		if (outputs == null) {
			return Collections.emptyList();
		}
		return outputs;
	}

	public Map<Class, List> getInputIngredients() {
		Map<Class, List> inputIngredients = new HashMap<Class, List>();
		for (Map.Entry<Class, List<List>> entry : inputs.entrySet()) {
			List<Object> flatIngredients = new ArrayList<Object>();
			for (List ingredients : entry.getValue()) {
				flatIngredients.addAll(ingredients);
			}
			inputIngredients.put(entry.getKey(), flatIngredients);
		}
		return inputIngredients;
	}

	public Map<Class, List> getOutputIngredients() {
		return outputs;
	}

	public boolean isUsed() {
		return used;
	}
}
