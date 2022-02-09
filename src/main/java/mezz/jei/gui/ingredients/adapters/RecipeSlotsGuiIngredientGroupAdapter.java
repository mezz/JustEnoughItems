package mezz.jei.gui.ingredients.adapters;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.Focus;
import mezz.jei.gui.ingredients.LegacyTooltipAdapter;
import mezz.jei.gui.ingredients.RecipeSlot;
import mezz.jei.gui.ingredients.RecipeSlots;
import mezz.jei.gui.ingredients.RendererOverrides;
import mezz.jei.ingredients.TypedIngredient;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"removal"})
public class RecipeSlotsGuiIngredientGroupAdapter<T> implements IGuiIngredientGroup<T> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final RecipeSlots recipeSlots;
	private final IIngredientManager ingredientManager;
	private final IIngredientType<T> ingredientType;
	private final Map<Integer, RecipeSlotGuiIngredientAdapter<T>> guiIngredients = new Int2ObjectArrayMap<>();
	/**
	 * For backward compatibility with {@link IGuiIngredientGroup},
	 * we keep "per-recipe" tooltip callbacks here.
	 */
	private final List<IRecipeSlotTooltipCallback> legacyTooltipCallbacks = new ArrayList<>();

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

	private Optional<RecipeSlot> getSlot(int guiIngredientIndex) {
		RecipeSlotGuiIngredientAdapter<T> adapter = this.guiIngredients.get(guiIngredientIndex);
		return Optional.ofNullable(adapter)
			.map(RecipeSlotGuiIngredientAdapter::getRecipeSlot);
	}

	@Override
	public void setBackground(int slotIndex, IDrawable background) {
		ErrorUtil.checkNotNull(background, "background");

		getSlot(slotIndex)
			.ifPresent(recipeSlot -> recipeSlot.setBackground(background));
	}

	@Override
	public Map<Integer, ? extends IGuiIngredient<T>> getGuiIngredients() {
		return guiIngredients;
	}

	@Override
	public void init(int ingredientIndex, boolean input, int xPosition, int yPosition) {
		RecipeIngredientRole role = input ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
		IIngredientRenderer<T> ingredientRenderer = this.ingredientManager.getIngredientRenderer(this.ingredientType);
		addSlot(ingredientIndex, role, ingredientRenderer, xPosition, yPosition, 16, 16, 0, 0);
	}

	@Override
	public void init(int ingredientIndex, boolean input, IIngredientRenderer<T> ingredientRenderer, int xPosition, int yPosition, int width, int height, int xInset, int yInset) {
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");

		RecipeIngredientRole role = input ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
		addSlot(ingredientIndex, role, ingredientRenderer, xPosition, yPosition, width, height, xInset, yInset);
	}

	private void addSlot(int ingredientIndex, RecipeIngredientRole role, IIngredientRenderer<T> ingredientRenderer, int xPosition, int yPosition, int width, int height, int xInset, int yInset) {
		LegacyAdaptedIngredientRenderer<T> legacyAdaptedIngredientRenderer = new LegacyAdaptedIngredientRenderer<>(ingredientRenderer, width, height, xInset, yInset);
		RecipeSlot recipeSlot = new RecipeSlot(this.ingredientManager, role, xPosition, yPosition, this.cycleOffset);
		recipeSlot.setLegacyIngredientIndex(ingredientIndex);

		RendererOverrides rendererOverrides = new RendererOverrides();
		rendererOverrides.addOverride(this.ingredientType, legacyAdaptedIngredientRenderer);
		recipeSlot.setRendererOverrides(rendererOverrides);

		this.recipeSlots.addSlot(recipeSlot);
		this.guiIngredients.put(ingredientIndex, new RecipeSlotGuiIngredientAdapter<>(recipeSlot, this.ingredientType));
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

		LegacyTooltipAdapter<T> legacyTooltipAdapter = new LegacyTooltipAdapter<>(this.ingredientType, tooltipCallback);
		this.legacyTooltipCallbacks.add(legacyTooltipAdapter);

		this.recipeSlots.getSlots()
			.forEach(slot -> slot.addTooltipCallback(legacyTooltipAdapter));
	}

	@Override
	public void set(IIngredients ingredients) {
		ErrorUtil.checkNotNull(ingredients, "ingredients");

		List<@Nullable List<@Nullable T>> inputs = ingredients.getInputs(ingredientType);
		List<@Nullable List<@Nullable T>> outputs = ingredients.getOutputs(ingredientType);
		int inputIndex = 0;
		int outputIndex = 0;

		List<Integer> slotIndexes = new ArrayList<>(guiIngredients.keySet());
		Collections.sort(slotIndexes);
		for (int slotIndex : slotIndexes) {
			IGuiIngredient<?> guiIngredient = this.guiIngredients.get(slotIndex);
			if (guiIngredient.isInput()) {
				if (inputIndex < inputs.size()) {
					@Nullable List<@Nullable T> input = inputs.get(inputIndex);
					inputIndex++;
					set(slotIndex, input);
				}
			} else {
				if (outputIndex < outputs.size()) {
					@Nullable List<@Nullable T> output = outputs.get(outputIndex);
					outputIndex++;
					set(slotIndex, output);
				}
			}
		}
	}

	@Override
	public void set(int ingredientIndex, @Nullable T value) {
		set(ingredientIndex, Collections.singletonList(value));
	}

	@Override
	public void set(int slotIndex, @Nullable List<@Nullable T> ingredients) {
		// Sanitize API input
		if (ingredients != null) {
			Class<? extends T> ingredientClass = ingredientType.getIngredientClass();
			for (@Nullable T ingredient : ingredients) {
				if (!ingredientClass.isInstance(ingredient) && ingredient != null) {
					LOGGER.error(
						"Received wrong type of ingredient. " +
							"Expected {}, got {}", ingredientClass, ingredient.getClass(),
						new IllegalArgumentException());
					return;
				}
			}
		}

		getSlot(slotIndex)
			.ifPresent(recipeSlot -> {
				List<Optional<ITypedIngredient<?>>> typedIngredients = getTypedIngredients(ingredients);
				if (focus != null) {
					recipeSlot.set(typedIngredients, List.of(focus));
				} else {
					recipeSlot.set(typedIngredients, List.of());
				}

				this.legacyTooltipCallbacks.forEach(recipeSlot::addTooltipCallback);
			});
	}

	private List<Optional<ITypedIngredient<?>>> getTypedIngredients(@Nullable List<@Nullable T> ingredients) {
		if (ingredients == null) {
			return List.of();
		}
		return ingredients.stream()
			.map(i -> TypedIngredient.create(ingredientManager, ingredientType, i))
			.toList();
	}

}
