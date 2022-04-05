package mezz.jei.common.deprecated.gui.ingredients.adapters;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.gui.ingredients.RecipeSlot;
import mezz.jei.common.gui.ingredients.RecipeSlots;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.ingredients.TypedIngredient;
import mezz.jei.common.focus.FocusGroup;
import mezz.jei.common.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

@SuppressWarnings({"removal", "DeprecatedIsStillUsed"})
@Deprecated
public class RecipeSlotsGuiIngredientGroupAdapter<T> implements IGuiIngredientGroup<T> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final RecipeSlots recipeSlots;
	private final RegisteredIngredients registeredIngredients;
	private final IIngredientType<T> ingredientType;
	private final Map<Integer, RecipeSlotGuiIngredientAdapter<T>> guiIngredientsCache = new Int2ObjectArrayMap<>();
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
	private IFocusGroup focuses = FocusGroup.EMPTY;
	private boolean slotsCreatedWithLegacyInit = false;
	private IIngredientVisibility ingredientVisibility;

	public RecipeSlotsGuiIngredientGroupAdapter(
		RecipeSlots recipeSlots,
		RegisteredIngredients registeredIngredients,
		IIngredientType<T> ingredientType,
		IIngredientVisibility ingredientVisibility,
		int cycleOffset
	) {
		this.recipeSlots = recipeSlots;
		this.registeredIngredients = registeredIngredients;
		this.ingredientType = ingredientType;
		this.ingredientVisibility = ingredientVisibility;
		this.cycleOffset = cycleOffset;
	}

	private Optional<RecipeSlot> getSlot(int guiIngredientIndex) {
		Map<Integer, RecipeSlotGuiIngredientAdapter<T>> guiIngredients = getGuiIngredients();
		RecipeSlotGuiIngredientAdapter<T> adapter = guiIngredients.get(guiIngredientIndex);
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
	public Map<Integer, RecipeSlotGuiIngredientAdapter<T>> getGuiIngredients() {
		List<RecipeSlot> slots = this.recipeSlots.getSlots();
		if (!this.slotsCreatedWithLegacyInit) {
			// Support for the case where we are reading from this legacy adapter,
			// but the recipe slots were set by the modern RecipeSlots methods.
			for (RecipeSlot recipeSlot : slots) {
				int index = recipeSlot.getLegacyIngredientIndex();
				if (!guiIngredientsCache.containsKey(index)) {
					RecipeSlotGuiIngredientAdapter<T> adapter = new RecipeSlotGuiIngredientAdapter<>(recipeSlot, this.ingredientType);
					guiIngredientsCache.put(index, adapter);
				}
			}
		}
		return Collections.unmodifiableMap(guiIngredientsCache);
	}

	@Override
	public void init(int ingredientIndex, boolean input, int xPosition, int yPosition) {
		RecipeIngredientRole role = input ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
		IIngredientRenderer<T> ingredientRenderer = this.registeredIngredients.getIngredientRenderer(this.ingredientType);
		addSlot(ingredientIndex, role, ingredientRenderer, xPosition, yPosition, 16, 16, 0, 0);
	}

	@Override
	public void init(int ingredientIndex, boolean input, IIngredientRenderer<T> ingredientRenderer, int xPosition, int yPosition, int width, int height, int xInset, int yInset) {
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");

		RecipeIngredientRole role = input ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
		addSlot(ingredientIndex, role, ingredientRenderer, xPosition, yPosition, width, height, xInset, yInset);
	}

	private void addSlot(int legacyIngredientIndex, RecipeIngredientRole role, IIngredientRenderer<T> ingredientRenderer, int xPosition, int yPosition, int width, int height, int xInset, int yInset) {
		IIngredientRenderer<T> legacyAdaptedIngredientRenderer = LegacyAdaptedIngredientRenderer.create(ingredientRenderer, width, height, xInset, yInset);
		RecipeSlot recipeSlot = new RecipeSlot(this.registeredIngredients, role, xPosition, yPosition, this.cycleOffset, legacyIngredientIndex);
		recipeSlot.addRenderOverride(this.ingredientType, legacyAdaptedIngredientRenderer);

		this.recipeSlots.addSlot(recipeSlot);

		RecipeSlotGuiIngredientAdapter<T> adapter = new RecipeSlotGuiIngredientAdapter<>(recipeSlot, this.ingredientType);
		this.guiIngredientsCache.put(legacyIngredientIndex, adapter);
		this.slotsCreatedWithLegacyInit = true;
	}

	@Override
	public void setOverrideDisplayFocus(@Nullable IFocus<T> focus) {
		this.focuses = FocusGroup.createFromNullable(focus, registeredIngredients);
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

		Map<Integer, ? extends IGuiIngredient<T>> guiIngredients = getGuiIngredients();
		List<Integer> slotIndexes = new ArrayList<>(guiIngredients.keySet());
		Collections.sort(slotIndexes);
		for (int slotIndex : slotIndexes) {
			IGuiIngredient<?> guiIngredient = guiIngredients.get(slotIndex);
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
				IntSet focusMatches = getMatches(focuses, recipeSlot.getRole(), typedIngredients);
				recipeSlot.set(typedIngredients, focusMatches, ingredientVisibility);
				this.legacyTooltipCallbacks.forEach(recipeSlot::addTooltipCallback);
			});
	}

	private List<Optional<ITypedIngredient<?>>> getTypedIngredients(@Nullable List<@Nullable T> ingredients) {
		if (ingredients == null) {
			return List.of();
		}
		return ingredients.stream()
			.map(i -> TypedIngredient.create(registeredIngredients, ingredientType, i))
			.toList();
	}

	private IntSet getMatches(IFocusGroup focuses, RecipeIngredientRole role, List<Optional<ITypedIngredient<?>>> ingredients) {
		int[] matches = focuses.getFocuses(ingredientType, role)
			.map(focus -> getMatch(focus, ingredients))
			.flatMapToInt(OptionalInt::stream)
			.distinct()
			.toArray();
		return new IntArraySet(matches);
	}

	private OptionalInt getMatch(IFocus<T> focus, List<Optional<ITypedIngredient<?>>> ingredients) {
		if (ingredients.isEmpty()) {
			return OptionalInt.empty();
		}
		ITypedIngredient<T> focusValue = focus.getTypedValue();
		T focusIngredient = focusValue.getIngredient();

		IIngredientHelper<T> ingredientHelper = this.registeredIngredients.getIngredientHelper(ingredientType);
		String focusUid = ingredientHelper.getUniqueId(focusIngredient, UidContext.Ingredient);

		return IntStream.range(0, ingredients.size())
			.filter(i ->
				ingredients.get(i)
					.flatMap(typedIngredient -> typedIngredient.getIngredient(ingredientType))
					.map(ingredient -> {
						String uniqueId = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
						return focusUid.equals(uniqueId);
					})
					.orElse(false)
			)
			.findFirst();
	}

}
