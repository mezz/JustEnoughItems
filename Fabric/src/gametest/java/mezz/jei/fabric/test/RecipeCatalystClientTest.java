package mezz.jei.fabric.test;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.Internal;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.SimpleIngredientAcceptor;
import mezz.jei.library.load.registration.RecipeCatalystRegistration;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Objects;

/** Checks invalid crafting stations and grouped displays against the running ingredient manager. */
@SuppressWarnings({"UnstableApiUsage", "removal"})
final class RecipeCatalystClientTest {
	private RecipeCatalystClientTest() {
	}

	static void run(ClientGameTestContext context) {
		context.runOnClient(client -> {
			var runtime = Internal.getJeiRuntime();
			var ingredientManager = (IIngredientManagerInternal) runtime.getIngredientManager();
			var level = Objects.requireNonNull(client.level);
			var contextMap = SlotDisplayContext.fromLevel(level);
			var registration = new RecipeCatalystRegistration(ingredientManager, runtime.getJeiHelpers(), contextMap);
			var recipeType = RecipeTypes.CRAFTING;
			var planks = new SlotDisplay.TagSlotDisplay(level.registryAccess().lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.PLANKS));

			// Empty stations must not interrupt registration of later valid stations.
			registration.addCraftingStations(recipeType, VanillaTypes.ITEM_STACK, List.of(ItemStack.EMPTY, new ItemStack(Items.CRAFTING_TABLE)));
			registration.addCraftingStation(recipeType, Blocks.AIR, Blocks.STONE);
			registration.addCraftingStation(recipeType, VanillaTypes.ITEM_STACK, ItemStack.EMPTY);
			registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, ItemStack.EMPTY, recipeType);
			registration.addCraftingStation(recipeType, SlotDisplay.Empty.INSTANCE);
			registration.addCraftingStation(recipeType, planks);
			var stations = registration.getCraftingStations().get(recipeType);
			var resolved = new SimpleIngredientAcceptor(ingredientManager, contextMap, RecipeIngredientRole.CRAFTING_STATION);
			stations.forEach(station -> station.accept(resolved));
			var ingredients = resolved.getAllSlotIngredients();

			check(stations.size() == 3, "Invalid stations must be skipped without losing valid registrations or splitting a tag display");
			check(ingredients.get(0).typedIngredient().getItemStack().orElseThrow().is(Items.CRAFTING_TABLE), "The valid station after an empty stack must remain registered");
			check(ingredients.get(1).typedIngredient().getItemStack().orElseThrow().is(Items.STONE), "The valid station after an air block must remain registered");
			check(ingredients.size() > 3, "The grouped plank station must resolve multiple ingredients");
			for (var ingredient : ingredients.subList(2, ingredients.size())) {
				var displayData = Objects.requireNonNull(ingredient.slotDisplayData());
				check(displayData.info().tagKey().orElseThrow().equals(ItemTags.PLANKS), "Crafting station resolution must preserve the display's tag identity");
			}
		});
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
