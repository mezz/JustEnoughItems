package mezz.jei.library.plugins.vanilla.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.ISlotDisplayInterpreterRegistration;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.platform.IPlatformRecipeHelper.FireworkRocketRecipeData;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.crafting.FireworkRocketRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class FireworkRocketRecipeCategoryExtension implements ICraftingCategoryExtension<FireworkRocketRecipe> {
	private static final int CRAFTING_GRID_SLOT_COUNT = 9;

	private final IPlatformRecipeHelper recipeHelper;

	public FireworkRocketRecipeCategoryExtension(IPlatformRecipeHelper recipeHelper) {
		this.recipeHelper = recipeHelper;
	}

	@SuppressWarnings("removal")
	public static void registerSlotDisplayInterpreter(ISlotDisplayInterpreterRegistration registration) {
		registration.register(
			AllSubtypesSlotDisplay.TYPE,
			(slotDisplay, ignoredContext, interpretationBuilder) -> interpretationBuilder
				.addChildDisplay(slotDisplay.source())
				.setMatchesAllSubtypes(true)
		);
	}

	@Override
	public List<SlotDisplay> getIngredients(RecipeHolder<FireworkRocketRecipe> recipeHolder) {
		FireworkRocketRecipeData recipeData = recipeHelper.getFireworkRocketRecipeData(recipeHolder.value());
		Fireworks fireworks = getDefaultFireworks(recipeData);
		return createIngredientDisplays(recipeData, fireworks);
	}

	@Override
	public void setRecipe(
		RecipeHolder<FireworkRocketRecipe> recipeHolder,
		IRecipeLayoutBuilder builder,
		ICraftingGridHelper craftingGridHelper,
		IFocusGroup focuses
	) {
		FireworkRocketRecipeData recipeData = recipeHelper.getFireworkRocketRecipeData(recipeHolder.value());
		Fireworks fireworks = getFocusedFireworks(focuses, recipeData)
			.orElseGet(() -> getDefaultFireworks(recipeData));

		ItemStack output = createOutput(recipeData.result(), fireworks);
		SlotDisplay outputDisplay = new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(output));
		craftingGridHelper.createAndSetOutputs(builder, outputDisplay);

		List<SlotDisplay> ingredients = createIngredientDisplays(recipeData, fireworks);
		craftingGridHelper.createAndSetIngredientsFromDisplays(builder, ingredients, 0, 0);

		builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
			.add(new AllSubtypesSlotDisplay(new SlotDisplay.ItemStackSlotDisplay(recipeData.result())));
		builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
			.add(new AllSubtypesSlotDisplay(recipeData.star().display()));
	}

	private static Optional<Fireworks> getFocusedFireworks(IFocusGroup focuses, FireworkRocketRecipeData recipeData) {
		ItemStack defaultOutput = recipeData.result().create();
		Optional<Fireworks> focusedRocket = focuses.getItemStackFocuses(RecipeIngredientRole.OUTPUT)
			.map(focus -> focus.getTypedValue().getIngredient())
			.filter(stack -> ItemStack.isSameItem(stack, defaultOutput))
			.map(stack -> Optional.ofNullable(stack.get(DataComponents.FIREWORKS)))
			.flatMap(Optional::stream)
			.filter(FireworkRocketRecipeCategoryExtension::canFitCraftingGrid)
			.findFirst();
		if (focusedRocket.isPresent()) {
			return focusedRocket;
		}

		return focuses.getItemStackFocuses(RecipeIngredientRole.INPUT)
			.map(focus -> focus.getTypedValue().getIngredient())
			.filter(recipeData.star())
			.map(stack -> Optional.ofNullable(stack.get(DataComponents.FIREWORK_EXPLOSION)))
			.flatMap(Optional::stream)
			.findFirst()
			.map(explosion -> {
				Fireworks defaultFireworks = getDefaultFireworks(recipeData);
				return new Fireworks(defaultFireworks.flightDuration(), List.of(explosion));
			});
	}

	private static Fireworks getDefaultFireworks(FireworkRocketRecipeData recipeData) {
		ItemStack defaultOutput = recipeData.result().create();
		return Optional.ofNullable(defaultOutput.get(DataComponents.FIREWORKS))
			.orElseGet(() -> {
				if (FireworkRocketItem.CRAFTABLE_DURATIONS.length == 0) {
					throw new IllegalStateException("Minecraft defines no craftable firework rocket durations");
				}
				return new Fireworks(FireworkRocketItem.CRAFTABLE_DURATIONS[0], List.of());
			});
	}

	private static boolean canFitCraftingGrid(Fireworks fireworks) {
		boolean craftableDuration = false;
		for (byte duration : FireworkRocketItem.CRAFTABLE_DURATIONS) {
			if (duration == fireworks.flightDuration()) {
				craftableDuration = true;
				break;
			}
		}
		return craftableDuration &&
			1 + fireworks.flightDuration() + fireworks.explosions().size() <= CRAFTING_GRID_SLOT_COUNT;
	}

	private static List<SlotDisplay> createIngredientDisplays(FireworkRocketRecipeData recipeData, Fireworks fireworks) {
		List<SlotDisplay> ingredients = new ArrayList<>(1 + fireworks.flightDuration() + fireworks.explosions().size());
		ingredients.add(recipeData.shell().display());
		for (int i = 0; i < fireworks.flightDuration(); i++) {
			ingredients.add(recipeData.fuel().display());
		}
		for (FireworkExplosion explosion : fireworks.explosions()) {
			ingredients.add(new FireworkStarSlotDisplay(recipeData.star().display(), explosion));
		}
		return ingredients;
	}

	private static ItemStack createOutput(ItemStackTemplate result, Fireworks fireworks) {
		DataComponentPatch components = DataComponentPatch.builder()
			.set(DataComponents.FIREWORKS, fireworks)
			.build();
		return result.apply(components);
	}

	private record AllSubtypesSlotDisplay(SlotDisplay source) implements SlotDisplay {
		private static final MapCodec<AllSubtypesSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				SlotDisplay.CODEC.fieldOf("source").forGetter(AllSubtypesSlotDisplay::source)
			)
			.apply(instance, AllSubtypesSlotDisplay::new));
		private static final StreamCodec<RegistryFriendlyByteBuf, AllSubtypesSlotDisplay> STREAM_CODEC = StreamCodec.composite(
			SlotDisplay.STREAM_CODEC,
			AllSubtypesSlotDisplay::source,
			AllSubtypesSlotDisplay::new
		);
		private static final Type<AllSubtypesSlotDisplay> TYPE = new Type<>(MAP_CODEC, STREAM_CODEC);

		@Override
		public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
			return source.resolve(context, factory);
		}

		@Override
		public Type<AllSubtypesSlotDisplay> type() {
			return TYPE;
		}

		@Override
		public boolean isEnabled(FeatureFlagSet enabledFeatures) {
			return source.isEnabled(enabledFeatures);
		}
	}

	private record FireworkStarSlotDisplay(SlotDisplay source, FireworkExplosion explosion) implements SlotDisplay {
		private static final MapCodec<FireworkStarSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				SlotDisplay.CODEC.fieldOf("source").forGetter(FireworkStarSlotDisplay::source),
				FireworkExplosion.CODEC.fieldOf("explosion").forGetter(FireworkStarSlotDisplay::explosion)
			)
			.apply(instance, FireworkStarSlotDisplay::new));
		private static final StreamCodec<RegistryFriendlyByteBuf, FireworkStarSlotDisplay> STREAM_CODEC = StreamCodec.composite(
			SlotDisplay.STREAM_CODEC,
			FireworkStarSlotDisplay::source,
			FireworkExplosion.STREAM_CODEC,
			FireworkStarSlotDisplay::explosion,
			FireworkStarSlotDisplay::new
		);
		private static final Type<FireworkStarSlotDisplay> TYPE = new Type<>(MAP_CODEC, STREAM_CODEC);

		@Override
		public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
			if (factory instanceof DisplayContentsFactory.ForStacks<T> stacks) {
				return source.resolveForStacks(context)
					.stream()
					.map(stack -> {
						ItemStack copy = stack.copy();
						copy.set(DataComponents.FIREWORK_EXPLOSION, explosion);
						return copy;
					})
					.map(stacks::forStack);
			}
			return Stream.empty();
		}

		@Override
		public Type<FireworkStarSlotDisplay> type() {
			return TYPE;
		}

		@Override
		public boolean isEnabled(FeatureFlagSet enabledFeatures) {
			return source.isEnabled(enabledFeatures);
		}
	}
}
