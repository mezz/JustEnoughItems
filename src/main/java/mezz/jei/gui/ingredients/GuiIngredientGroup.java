package mezz.jei.gui.ingredients;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mezz.jei.Internal;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.gui.Focus;
import mezz.jei.util.Log;
import net.minecraft.client.Minecraft;

public class GuiIngredientGroup<T> implements IGuiIngredientGroup<T> {
	private final int itemCycleOffset = (int) (Math.random() * 1000);
	private final Map<Integer, GuiIngredient<T>> guiIngredients = new HashMap<Integer, GuiIngredient<T>>();
	private final Set<Integer> inputSlots = new HashSet<Integer>();
	private final IIngredientHelper<T> ingredientHelper;
	private final Class<T> ingredientClass;
	/**
	 * If focus is set and any of the guiIngredients contains focus
	 * they will only display focus instead of rotating through all their values.
	 */
	private IFocus<T> inputFocus;
	private IFocus<T> outputFocus;
	@Nullable
	private ITooltipCallback<T> tooltipCallback;

	public GuiIngredientGroup(Class<T> ingredientClass, IFocus<T> focus) {
		this.ingredientClass = ingredientClass;
		if (focus.getMode() == IFocus.Mode.INPUT) {
			this.inputFocus = focus;
			this.outputFocus = new Focus<T>(null);
		} else if (focus.getMode() == IFocus.Mode.OUTPUT) {
			this.inputFocus = new Focus<T>(null);
			this.outputFocus = focus;
		} else {
			this.inputFocus = new Focus<T>(null);
			this.outputFocus = new Focus<T>(null);
		}
		this.ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(ingredientClass);
	}

	@Override
	public void init(int slotIndex, boolean input, IIngredientRenderer<T> ingredientRenderer, int xPosition, int yPosition, int width, int height, int xPadding, int yPadding) {
		GuiIngredient<T> guiIngredient = new GuiIngredient<T>(slotIndex, input, ingredientRenderer, ingredientHelper, xPosition, yPosition, width, height, xPadding, yPadding, itemCycleOffset);
		guiIngredients.put(slotIndex, guiIngredient);
		if (input) {
			inputSlots.add(slotIndex);
		}
	}

	@Override
	public IFocus<T> getFocus() {
		return inputFocus.getMode() != IFocus.Mode.NONE ? inputFocus : outputFocus;
	}

	@Override
	public void set(IIngredients ingredients) {
		List<List<T>> inputs = ingredients.getInputs(ingredientClass);
		List<T> outputs = ingredients.getOutputs(ingredientClass);
		int inputIndex = 0;
		int outputIndex = 0;

		List<Integer> slots = new ArrayList<Integer>(guiIngredients.keySet());
		Collections.sort(slots);
		for (Integer slot : slots) {
			if (inputSlots.contains(slot)) {
				if (inputIndex < inputs.size()) {
					List<T> input = inputs.get(inputIndex);
					inputIndex++;
					set(slot, input);
				}
			} else {
				if (outputIndex < outputs.size()) {
					T output = outputs.get(outputIndex);
					outputIndex++;
					set(slot, output);
				}
			}
		}
	}

	@Override
	@Deprecated
	public void set(int slotIndex, Collection<T> values) {
		set(slotIndex, new ArrayList<T>(values));
	}

	@Override
	public void set(int slotIndex, @Nullable List<T> ingredients) {
		// Sanitize API input
		if (ingredients != null) {
			for (T ingredient : ingredients) {
				if (!ingredientClass.isInstance(ingredient) && ingredient != null) {
					Log.error("Received wrong type of ingredient. Expected {}, got {}", ingredientClass, ingredient.getClass(), new IllegalArgumentException());
					return;
				}
			}
		}
		GuiIngredient<T> guiIngredient = guiIngredients.get(slotIndex);
		if (guiIngredient.isInput()) {
			guiIngredient.set(ingredients, inputFocus);
		} else {
			guiIngredient.set(ingredients, outputFocus);
		}
	}

	@Override
	public void set(int slotIndex, @Nullable T value) {
		set(slotIndex, Collections.singletonList(value));
	}

	@Override
	public void addTooltipCallback(ITooltipCallback<T> tooltipCallback) {
		this.tooltipCallback = tooltipCallback;
	}

	@Override
	public Map<Integer, GuiIngredient<T>> getGuiIngredients() {
		return guiIngredients;
	}

	@Nullable
	public T getIngredientUnderMouse(int xOffset, int yOffset, int mouseX, int mouseY) {
		for (GuiIngredient<T> guiIngredient : guiIngredients.values()) {
			if (guiIngredient != null && guiIngredient.isMouseOver(xOffset, yOffset, mouseX, mouseY)) {
				T displayedIngredient = guiIngredient.getDisplayedIngredient();
				if (displayedIngredient != null) {
					return displayedIngredient;
				}
			}
		}
		return null;
	}

	@Nullable
	public GuiIngredient<T> draw(Minecraft minecraft, int xOffset, int yOffset, int mouseX, int mouseY) {
		GuiIngredient<T> hovered = null;
		for (GuiIngredient<T> ingredient : guiIngredients.values()) {
			if (hovered == null && ingredient.isMouseOver(xOffset, yOffset, mouseX, mouseY)) {
				hovered = ingredient;
				hovered.setTooltipCallback(tooltipCallback);
			} else {
				ingredient.draw(minecraft, xOffset, yOffset);
			}
		}
		return hovered;
	}

	@Override
	public void setOverrideDisplayFocus(@Nullable IFocus<T> focus) {
		if (focus == null) {
			Log.error("Null focus", new NullPointerException());
			return;
		}

		if (focus.getMode() == IFocus.Mode.INPUT) {
			this.inputFocus = focus;
		} else if (focus.getMode() == IFocus.Mode.OUTPUT) {
			this.outputFocus = focus;
		}
	}
}
