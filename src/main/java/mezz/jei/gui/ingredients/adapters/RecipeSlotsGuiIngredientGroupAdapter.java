package mezz.jei.gui.ingredients.adapters;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.Focus;
import mezz.jei.gui.ingredients.LegacyTooltipAdapter;
import mezz.jei.gui.ingredients.RecipeSlot;
import mezz.jei.gui.ingredients.RecipeSlots;
import mezz.jei.util.ErrorUtil;
import net.minecraft.client.renderer.Rect2i;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeSlotsGuiIngredientGroupAdapter<T> implements IGuiIngredientGroup<T> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final RecipeSlots recipeSlots;
	private final IIngredientManager ingredientManager;
	private final IIngredientType<T> ingredientType;
	private final Map<Integer, RecipeSlotGuiIngredientAdapter<T>> guiIngredients = new HashMap<>();
	private final IntSet inputSlots = new IntOpenHashSet();
	private final int cycleOffset;
	/**
	 * If focus is set and any of the guiIngredients contains focus
	 * they will only display focus instead of rotating through all their values.
	 */
	@Nullable
	private Focus<T> focus;

	public RecipeSlotsGuiIngredientGroupAdapter(
		RecipeSlots recipeSlots,
		IIngredientManager ingredientManager,
		IIngredientType<T> ingredientType,
		int cycleOffset
	) {
		this.recipeSlots = recipeSlots;
		this.ingredientManager = ingredientManager;
		this.ingredientType = ingredientType;
		this.cycleOffset = cycleOffset;
	}

	@Override
	public void setBackground(int slotIndex, IDrawable background) {
		ErrorUtil.checkNotNull(background, "background");

		RecipeSlotGuiIngredientAdapter<T> guiIngredientAdapter = this.guiIngredients.get(slotIndex);
		RecipeSlot recipeSlot = guiIngredientAdapter.getRecipeSlot();
		recipeSlot.setBackground(background);
	}

	@Override
	public void addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback) {
		ErrorUtil.checkNotNull(tooltipCallback, "tooltipCallback");

		this.recipeSlots.addTooltipCallback(tooltipCallback);
	}

	@Override
	public Map<Integer, ? extends IGuiIngredient<T>> getGuiIngredients() {
		return this.guiIngredients;
	}

	@Override
	public void init(int slotIndex, boolean input, int xPosition, int yPosition) {
		RecipeIngredientRole role = input ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
		IIngredientRenderer<T> ingredientRenderer = this.ingredientManager.getIngredientRenderer(this.ingredientType);
		init(slotIndex, role, ingredientRenderer, xPosition, yPosition, 16, 16, 0, 0);
	}

	@Override
	public void init(int slotIndex, boolean input, IIngredientRenderer<T> ingredientRenderer, int xPosition, int yPosition, int width, int height, int xInset, int yInset) {
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");

		RecipeIngredientRole role = input ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
		init(slotIndex, role, ingredientRenderer, xPosition, yPosition, width, height, xInset, yInset);
	}

	@Override
	public void init(int slotIndex, RecipeIngredientRole role, IIngredientRenderer<T> ingredientRenderer, int xPosition, int yPosition, int width, int height, int xInset, int yInset) {
		ErrorUtil.checkNotNull(role, "role");
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");

		Rect2i rect = new Rect2i(xPosition, yPosition, width, height);
		RecipeSlot recipeSlot = new RecipeSlot(this.ingredientManager, role, slotIndex, rect, xInset, yInset, cycleOffset);
		this.recipeSlots.addSlot(recipeSlot);

		RecipeSlotGuiIngredientAdapter<T> guiIngredient = new RecipeSlotGuiIngredientAdapter<>(
			recipeSlot,
			this.ingredientType
		);
		this.guiIngredients.put(slotIndex, guiIngredient);
		if (guiIngredient.isInput()) {
			this.inputSlots.add(slotIndex);
		}
	}

	@Override
	public void setOverrideDisplayFocus(@Nullable IFocus<T> focus) {
		if (focus == null) {
			this.focus = null;
		} else {
			this.focus = Focus.checkOne(focus);
		}
	}

	@Override
	public void addTooltipCallback(ITooltipCallback<T> tooltipCallback) {
		ErrorUtil.checkNotNull(tooltipCallback, "tooltipCallback");

		addTooltipCallback(new LegacyTooltipAdapter<>(this.ingredientType, tooltipCallback));
	}

	@Override
	public void set(IIngredients ingredients) {
		ErrorUtil.checkNotNull(ingredients, "ingredients");

		List<List<T>> inputs = ingredients.getInputs(ingredientType);
		List<List<T>> outputs = ingredients.getOutputs(ingredientType);
		int inputIndex = 0;
		int outputIndex = 0;

		List<Integer> slots = new ArrayList<>(guiIngredients.keySet());
		Collections.sort(slots);
		for (int slot : slots) {
			if (inputSlots.contains(slot)) {
				if (inputIndex < inputs.size()) {
					List<T> input = inputs.get(inputIndex);
					inputIndex++;
					set(slot, input);
				}
			} else {
				if (outputIndex < outputs.size()) {
					List<T> output = outputs.get(outputIndex);
					outputIndex++;
					set(slot, output);
				}
			}
		}
	}

	@Override
	public void set(int slotIndex, @Nullable List<T> ingredients) {
		// Sanitize API input
		if (ingredients != null) {
			for (T ingredient : ingredients) {
				Class<? extends T> ingredientClass = ingredientType.getIngredientClass();
				if (!ingredientClass.isInstance(ingredient) && ingredient != null) {
					LOGGER.error(
						"Received wrong type of ingredient. " +
							"Expected {}, got {}", ingredientClass, ingredient.getClass(),
						new IllegalArgumentException());
					return;
				}
			}
		}
		RecipeSlotGuiIngredientAdapter<T> guiIngredient = guiIngredients.get(slotIndex);
		RecipeSlot recipeSlot = guiIngredient.getRecipeSlot();

		if (focus == null || focus.getRole() == guiIngredient.getRole()) {
			recipeSlot.set(ingredients, focus);
		} else {
			recipeSlot.set(ingredients, null);
		}
	}

	@Override
	public void set(int slotIndex, @Nullable T value) {
		set(slotIndex, Collections.singletonList(value));
	}

}
