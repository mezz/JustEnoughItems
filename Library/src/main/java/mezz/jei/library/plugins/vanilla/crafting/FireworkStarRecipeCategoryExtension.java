package mezz.jei.library.plugins.vanilla.crafting;

import it.unimi.dsi.fastutil.ints.IntList;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.plugins.vanilla.ingredients.FireworkStarIngredientFactory;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.FireworkStarFadeRecipe;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Displays the creation and fading steps separately, preserving the focused star's decoration.
 */
public final class FireworkStarRecipeCategoryExtension implements ICraftingCategoryExtension<CraftingRecipe> {
	private static final int CRAFTING_GRID_SLOT_COUNT = 9;
	private final IPlatformRecipeHelper recipeHelper;
	private final Map<Ingredient, List<ItemStack>> ingredientStacks = new HashMap<>();
	private final Map<ResourceKey<Recipe<?>>, SampleLayout> sampleLayouts = new HashMap<>();

	public FireworkStarRecipeCategoryExtension(IPlatformRecipeHelper recipeHelper) {
		this.recipeHelper = recipeHelper;
	}

	@Override
	public boolean isHandled(RecipeHolder<CraftingRecipe> recipeHolder) {
		return getRecipe(recipeHolder, FocusGroup.EMPTY).isPresent();
	}

	@Override
	public List<SlotDisplay> getIngredients(RecipeHolder<CraftingRecipe> recipeHolder) {
		if (sampleLayouts.containsKey(recipeHolder.id())) {
			return getRepresentativeRecipes(recipeHolder, FocusGroup.EMPTY).stream()
				.findFirst().map(StarRecipe::ingredientDisplays).orElse(List.of());
		}
		return getRecipe(recipeHolder, FocusGroup.EMPTY)
			.map(StarRecipe::ingredientDisplays)
			.orElse(List.of());
	}

	@Override
	public void setRecipe(RecipeHolder<CraftingRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		ItemStack focusedOutput = getFocus(focuses, RecipeIngredientRole.OUTPUT);
		if (focusedOutput.isEmpty()) {
			List<StarRecipe> samples = getRepresentativeRecipes(recipeHolder, focuses);
			if (!samples.isEmpty()) {
				ItemStack focusedInput = getFocus(focuses, RecipeIngredientRole.INPUT);
				setRotatingRecipe(recipeHolder.value(), builder, craftingGridHelper, samples, focusedInput);
			}
			return;
		}
		getRecipe(recipeHolder, focuses).ifPresent(recipe -> {
			craftingGridHelper.createAndSetOutputs(builder, display(recipe.output()));
			craftingGridHelper.createAndSetIngredientsFromDisplays(builder, recipe.ingredientDisplays(), 0, 0);
		});
	}

	@Override
	public void onDisplayedIngredientsUpdate(
		RecipeHolder<CraftingRecipe> recipeHolder,
		List<IRecipeSlotDrawable> recipeSlots,
		IFocusGroup focuses
	) {
		List<ItemStack> inputs = recipeSlots.stream()
			.filter(slot -> slot.getRole() == RecipeIngredientRole.INPUT)
			.map(slot -> slot.getDisplayedItemStack().orElse(ItemStack.EMPTY))
			.toList();
		if (inputs.size() != CRAFTING_GRID_SLOT_COUNT) {
			return;
		}

		ItemStack output = recipeHolder.value().assemble(CraftingInput.of(3, 3, inputs));
		if (output.isEmpty()) {
			return;
		}
		recipeSlots.stream()
			.filter(slot -> slot.getRole() == RecipeIngredientRole.OUTPUT)
			.findFirst()
			.ifPresent(slot -> slot.createDisplayOverrides().add(output));
	}

	List<RecipeHolder<CraftingRecipe>> createSampleRecipes(RecipeHolder<CraftingRecipe> source) {
		List<SampleLayout> layouts = createRepresentativeRecipes(source, FocusGroup.EMPTY).stream()
			.map(sample -> SampleLayout.of(source.value(), sample)).distinct().toList();
		List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();
		for (SampleLayout layout : layouts) {
			RecipeHolder<CraftingRecipe> holder = source;
			if (!recipes.isEmpty()) {
				var id = source.id().identifier().withSuffix("/jei_samples/" + layout.id());
				ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id);
				holder = new RecipeHolder<>(key, source.value());
			}
			sampleLayouts.put(holder.id(), layout);
			recipes.add(holder);
		}
		return List.copyOf(recipes);
	}

	List<StarRecipe> getRepresentativeRecipes(RecipeHolder<CraftingRecipe> recipeHolder, IFocusGroup focuses) {
		SampleLayout layout = sampleLayouts.get(recipeHolder.id());
		if (layout == null) {
			layout = getRecipe(recipeHolder, FocusGroup.EMPTY)
				.map(sample -> SampleLayout.of(recipeHolder.value(), sample)).orElse(null);
		}
		SampleLayout selectedLayout = layout;
		return createRepresentativeRecipes(recipeHolder, focuses).stream()
			.filter(sample -> SampleLayout.of(recipeHolder.value(), sample).equals(selectedLayout))
			.toList();
	}

	private List<StarRecipe> createRepresentativeRecipes(RecipeHolder<CraftingRecipe> recipeHolder, IFocusGroup focuses) {
		Optional<StarRecipe> defaultRecipe = getRecipe(recipeHolder, focuses);
		if (defaultRecipe.isEmpty()) {
			return List.of();
		}
		ItemStack focusedInput = getFocus(focuses, RecipeIngredientRole.INPUT);
		Map<FireworkExplosion, StarRecipe> samples = new LinkedHashMap<>();
		StarRecipe first = defaultRecipe.get();
		samples.put(first.explosion(), first);
		for (FireworkExplosion explosion : FireworkStarIngredientFactory.createExplosions()) {
			Optional<StarRecipe> example = switch (recipeHolder.value()) {
				case FireworkStarRecipe recipe -> {
					var data = recipeHelper.getFireworkStarRecipeData(recipe);
					List<ItemStack> dyes = getDyes(data.dye(), focusedInput);
					FireworkExplosion adapted = new FireworkExplosion(
						explosion.shape(),
						sampleColors(explosion.colors(), dyes, focusedInput), IntList.of(),
						explosion.hasTrail(), explosion.hasTwinkle());
					ItemStack output = data.result().create();
					output.set(DataComponents.FIREWORK_EXPLOSION, adapted);
					yield createStar(recipe, output, focusedInput);
				}
				case FireworkStarFadeRecipe recipe -> {
					var data = recipeHelper.getFireworkStarFadeRecipeData(recipe);
					List<ItemStack> dyes = getDyes(data.dye(), focusedInput);
					ItemStack target = first.inputs().getFirst().getFirst().copy();
					if (!data.target().test(focusedInput)) {
						target.set(DataComponents.FIREWORK_EXPLOSION, explosion.withFadeColors(IntList.of()));
					}
					IntList colors = explosion.fadeColors();
					if (colors.isEmpty()) {
						colors = explosion.colors();
					}
					FireworkExplosion faded = target.getOrDefault(DataComponents.FIREWORK_EXPLOSION, FireworkExplosion.DEFAULT)
						.withFadeColors(sampleColors(colors, dyes, focusedInput));
					yield assembleFade(recipe, target, dyes, faded);
				}
				default -> Optional.empty();
			};
			example.filter(value -> usesInput(value, focusedInput))
				.ifPresent(value -> samples.putIfAbsent(value.explosion(), value));
		}
		return List.copyOf(samples.values());
	}

	private static IntList sampleColors(IntList colors, List<ItemStack> dyes, ItemStack focusedInput) {
		int[] selected = colors.toIntArray();
		for (int i = 0; i < selected.length; i++) {
			int color = selected[i];
			if (dyes.stream().noneMatch(dye -> getColor(dye) == color)) {
				selected[i] = getColor(dyes.getFirst());
			}
		}
		if (focusedInput.has(DataComponents.DYE) && dyes.stream().anyMatch(dye -> ItemStack.isSameItemSameComponents(dye, focusedInput))) {
			selected[0] = getColor(focusedInput);
		}
		return IntList.of(selected);
	}

	private void setRotatingRecipe(
		CraftingRecipe recipe,
		IRecipeLayoutBuilder builder,
		ICraftingGridHelper craftingGridHelper,
		List<StarRecipe> samples,
		ItemStack focusedInput
	) {
		int dyeSlots = SampleLayout.of(recipe, samples.getFirst()).colors();
		List<ItemStack> dyes = getDyes(recipe, focusedInput);
		List<List<ItemStack>> inputs = new ArrayList<>();
		for (int slot = 0; slot < samples.getFirst().inputs().size(); slot++) {
			if (slot > 0 && slot <= dyeSlots) {
				inputs.add(rotate(dyes, slot - 1));
			} else {
				inputs.add(getDistinctInputs(samples, slot));
			}
		}
		craftingGridHelper.createAndSetInputs(builder, inputs, 0, 0);
		craftingGridHelper.createAndSetOutputs(builder, display(samples.getFirst().output()));
	}

	private List<ItemStack> getDyes(CraftingRecipe recipe, ItemStack focusedInput) {
		return switch (recipe) {
			case FireworkStarRecipe starRecipe -> getDyes(recipeHelper.getFireworkStarRecipeData(starRecipe).dye(), focusedInput);
			case FireworkStarFadeRecipe fadeRecipe -> getDyes(recipeHelper.getFireworkStarFadeRecipeData(fadeRecipe).dye(), focusedInput);
			default -> List.of();
		};
	}

	private static List<ItemStack> rotate(List<ItemStack> stacks, int offset) {
		if (stacks.size() < 2 || offset == 0) {
			return stacks;
		}
		List<ItemStack> rotated = new ArrayList<>(stacks.size());
		for (int i = 0; i < stacks.size(); i++) {
			rotated.add(stacks.get((i + offset) % stacks.size()));
		}
		return List.copyOf(rotated);
	}

	private static List<ItemStack> getDistinctInputs(List<StarRecipe> samples, int slot) {
		List<ItemStack> inputs = new ArrayList<>();
		for (StarRecipe sample : samples) {
			for (ItemStack stack : sample.inputs().get(slot)) {
				if (inputs.stream().noneMatch(input -> ItemStack.matches(input, stack))) {
					inputs.add(stack);
				}
			}
		}
		return List.copyOf(inputs);
	}

	private record SampleLayout(int colors, boolean shaped, boolean trail, boolean twinkle) {
		private static SampleLayout of(CraftingRecipe recipe, StarRecipe sample) {
			FireworkExplosion explosion = sample.explosion();
			if (recipe instanceof FireworkStarFadeRecipe) {
				return new SampleLayout(explosion.fadeColors().size(), false, false, false);
			}
			return new SampleLayout(explosion.colors().size(), explosion.shape() != FireworkExplosion.Shape.SMALL_BALL,
				explosion.hasTrail(), explosion.hasTwinkle());
		}

		private String id() {
			return colors + "_" + shaped + "_" + trail + "_" + twinkle;
		}
	}

	Optional<StarRecipe> getRecipe(RecipeHolder<CraftingRecipe> recipeHolder, IFocusGroup focuses) {
		ItemStack output = getFocus(focuses, RecipeIngredientRole.OUTPUT);
		ItemStack input = getFocus(focuses, RecipeIngredientRole.INPUT);
		Optional<StarRecipe> result = switch (recipeHolder.value()) {
			case FireworkStarRecipe recipe -> createStar(recipe, output, input);
			case FireworkStarFadeRecipe recipe -> fadeStar(recipe, output, input);
			default -> Optional.empty();
		};
		return result.filter(recipe -> usesInput(recipe, input));
	}

	private static boolean usesInput(StarRecipe recipe, ItemStack input) {
		return input.isEmpty() || recipe.inputs().stream()
			.flatMap(List::stream)
			.anyMatch(stack -> ItemStack.isSameItemSameComponents(stack, input));
	}

	private Optional<StarRecipe> createStar(FireworkStarRecipe recipe, ItemStack focusedOutput, ItemStack focusedInput) {
		var data = recipeHelper.getFireworkStarRecipeData(recipe);
		if (!focusedOutput.isEmpty() && !ItemStack.isSameItem(focusedOutput, data.result().create())) {
			return Optional.empty();
		}
		List<ItemStack> dyes = getDyes(data.dye(), focusedInput);
		if (dyes.isEmpty()) {
			return Optional.empty();
		}
		FireworkExplosion explosion = focusedOutput.getOrDefault(DataComponents.FIREWORK_EXPLOSION, FireworkExplosion.DEFAULT);
		if (explosion.equals(FireworkExplosion.DEFAULT)) {
			FireworkExplosion.Shape shape = data.shapes().entrySet().stream()
				.filter(entry -> entry.getValue().test(focusedInput))
				.map(entry -> entry.getKey())
				.findFirst().orElse(FireworkExplosion.Shape.SMALL_BALL);
			explosion = new FireworkExplosion(shape, IntList.of(getColor(dyes.getFirst())), IntList.of(),
				data.trail().test(focusedInput), data.twinkle().test(focusedInput));
		}
		if (explosion.colors().isEmpty() || explosion.colors().size() >= CRAFTING_GRID_SLOT_COUNT || !explosion.fadeColors().isEmpty()) {
			return Optional.empty();
		}

		List<List<ItemStack>> inputs = new ArrayList<>();
		inputs.add(getStacks(data.fuel(), focusedInput));
		addDyes(inputs, dyes, explosion.colors());
		if (explosion.shape() != FireworkExplosion.Shape.SMALL_BALL) {
			Ingredient shape = data.shapes().get(explosion.shape());
			if (shape == null) {
				return Optional.empty();
			}
			inputs.add(getStacks(shape, focusedInput));
		}
		if (explosion.hasTrail()) {
			inputs.add(getStacks(data.trail(), focusedInput));
		}
		if (explosion.hasTwinkle()) {
			inputs.add(getStacks(data.twinkle(), focusedInput));
		}
		return assemble(recipe, inputs, explosion);
	}

	private Optional<StarRecipe> fadeStar(FireworkStarFadeRecipe recipe, ItemStack focusedOutput, ItemStack focusedInput) {
		var data = recipeHelper.getFireworkStarFadeRecipeData(recipe);
		if (!focusedOutput.isEmpty() && !ItemStack.isSameItem(focusedOutput, data.result().create())) {
			return Optional.empty();
		}
		List<ItemStack> dyes = getDyes(data.dye(), focusedInput);
		List<ItemStack> targets = getStacks(data.target(), focusedInput);
		if (dyes.isEmpty() || targets.isEmpty()) {
			return Optional.empty();
		}
		ItemStack target = targets.getFirst().copy();
		FireworkExplosion explosion;
		if (!focusedOutput.isEmpty()) {
			explosion = focusedOutput.getOrDefault(DataComponents.FIREWORK_EXPLOSION, FireworkExplosion.DEFAULT);
			if (explosion.fadeColors().isEmpty() || explosion.fadeColors().size() >= CRAFTING_GRID_SLOT_COUNT) {
				return Optional.empty();
			}
			// Fade colors are a second crafting step. R on this input finds the creation recipe.
			target.set(DataComponents.FIREWORK_EXPLOSION, explosion.withFadeColors(IntList.of()));
		} else {
			FireworkExplosion base = target.getOrDefault(DataComponents.FIREWORK_EXPLOSION, FireworkExplosion.DEFAULT);
			if (!data.target().test(focusedInput)) {
				base = new FireworkExplosion(FireworkExplosion.Shape.SMALL_BALL, IntList.of(getColor(dyes.getFirst())), IntList.of(), false, false);
				target.set(DataComponents.FIREWORK_EXPLOSION, base);
			}
			explosion = base.withFadeColors(IntList.of(getColor(dyes.getFirst())));
		}
		return assembleFade(recipe, target, dyes, explosion);
	}

	private static Optional<StarRecipe> assembleFade(FireworkStarFadeRecipe recipe, ItemStack target, List<ItemStack> dyes, FireworkExplosion explosion) {
		List<List<ItemStack>> inputs = new ArrayList<>();
		inputs.add(List.of(target));
		addDyes(inputs, dyes, explosion.fadeColors());
		return assemble(recipe, inputs, explosion);
	}

	private static void addDyes(List<List<ItemStack>> inputs, List<ItemStack> dyes, IntList colors) {
		for (int color : colors) {
			inputs.add(dyes.stream().filter(stack -> getColor(stack) == color).toList());
		}
	}

	private static int getColor(ItemStack dye) {
		return dye.getOrDefault(DataComponents.DYE, DyeColor.WHITE).getFireworkColor();
	}

	private List<ItemStack> getDyes(Ingredient ingredient, ItemStack focus) {
		List<ItemStack> dyes = new ArrayList<>(getStacks(ingredient, ItemStack.EMPTY).stream()
			.filter(stack -> stack.has(DataComponents.DYE))
			.toList());
		if (!focus.isEmpty() && ingredient.test(focus) && focus.has(DataComponents.DYE)) {
			dyes.removeIf(stack -> ItemStack.isSameItemSameComponents(stack, focus));
			dyes.addFirst(focus.copyWithCount(1));
		}
		return dyes;
	}

	private List<ItemStack> getStacks(Ingredient ingredient, ItemStack focus) {
		if (!focus.isEmpty() && ingredient.test(focus)) {
			return List.of(focus.copyWithCount(1));
		}
		// Client fallback ingredients can refer to tags whose own contents have not been bound.
		return ingredientStacks.computeIfAbsent(ingredient, key -> RegistryUtil.getRegistry(Registries.ITEM)
			.listElements()
			.filter(Holder::isBound)
			.filter(key::acceptsItem)
			.map(ItemStack::new)
			.toList());
	}

	private static Optional<StarRecipe> assemble(CraftingRecipe recipe, List<List<ItemStack>> inputs, FireworkExplosion explosion) {
		if (inputs.size() < 2 || inputs.size() > CRAFTING_GRID_SLOT_COUNT || inputs.stream().anyMatch(List::isEmpty)) {
			return Optional.empty();
		}
		List<ItemStack> stacks = new ArrayList<>(Collections.nCopies(CRAFTING_GRID_SLOT_COUNT, ItemStack.EMPTY));
		for (int i = 0; i < inputs.size(); i++) {
			stacks.set(i, inputs.get(i).getFirst());
		}
		ItemStack output = recipe.assemble(CraftingInput.of(3, 3, stacks));
		if (output.isEmpty() || !output.has(DataComponents.FIREWORK_EXPLOSION)) {
			return Optional.empty();
		}
		FireworkExplosion outputExplosion = output.getOrDefault(DataComponents.FIREWORK_EXPLOSION, FireworkExplosion.DEFAULT);
		if (!explosion.equals(outputExplosion)) {
			return Optional.empty();
		}
		return Optional.of(new StarRecipe(inputs, output, explosion));
	}

	private static ItemStack getFocus(IFocusGroup focuses, RecipeIngredientRole role) {
		return focuses.getItemStackFocuses(role)
			.map(focus -> focus.getTypedValue().getIngredient())
			.findFirst().orElse(ItemStack.EMPTY);
	}

	private static SlotDisplay display(ItemStack stack) {
		return new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack));
	}

	record StarRecipe(List<List<ItemStack>> inputs, ItemStack output, FireworkExplosion explosion) {
		List<SlotDisplay> ingredientDisplays() {
			return inputs.stream()
				.<SlotDisplay>map(stacks -> new SlotDisplay.Composite(stacks.stream().map(FireworkStarRecipeCategoryExtension::display).toList()))
				.toList();
		}
	}
}
