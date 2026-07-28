package mezz.jei.test;

import mezz.jei.library.plugins.vanilla.crafting.CraftingCategoryExtension;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CraftingCategoryExtensionTest {
	@BeforeAll
	public static void bootstrap() {
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		Bootstrap.bootStrap();
	}

	@Test
	public void customCraftingRecipeUsesShapedDisplayDimensions() {
		List<SlotDisplay> ingredients = List.of(
			new SlotDisplay.ItemSlotDisplay(Items.STICK),
			SlotDisplay.Empty.INSTANCE,
			new SlotDisplay.ItemSlotDisplay(Items.OAK_PLANKS),
			new SlotDisplay.ItemSlotDisplay(Items.OAK_PLANKS),
			SlotDisplay.Empty.INSTANCE,
			new SlotDisplay.ItemSlotDisplay(Items.STICK)
		);
		RecipeDisplay display = new ShapedCraftingRecipeDisplay(
			2,
			3,
			ingredients,
			new SlotDisplay.ItemSlotDisplay(Items.OAK_FENCE),
			createCraftingStationDisplay()
		);
		RecipeHolder<CraftingRecipe> recipeHolder = createRecipeHolder(display);

		CraftingCategoryExtension extension = new CraftingCategoryExtension();

		assertTrue(extension.isHandled(recipeHolder));
		assertEquals(2, extension.getWidth(recipeHolder));
		assertEquals(3, extension.getHeight(recipeHolder));
		assertEquals(ingredients, extension.getIngredients(recipeHolder));
	}

	@Test
	public void shapelessDisplayUsesShapelessDimensions() {
		List<SlotDisplay> ingredients = List.of(
			new SlotDisplay.ItemSlotDisplay(Items.STICK),
			new SlotDisplay.ItemSlotDisplay(Items.OAK_PLANKS)
		);
		RecipeDisplay display = new ShapelessCraftingRecipeDisplay(
			ingredients,
			new SlotDisplay.ItemSlotDisplay(Items.OAK_FENCE),
			createCraftingStationDisplay()
		);
		RecipeHolder<CraftingRecipe> recipeHolder = createRecipeHolder(display);

		CraftingCategoryExtension extension = new CraftingCategoryExtension();

		assertTrue(extension.isHandled(recipeHolder));
		assertEquals(0, extension.getWidth(recipeHolder));
		assertEquals(0, extension.getHeight(recipeHolder));
		assertEquals(ingredients, extension.getIngredients(recipeHolder));
	}

	private static SlotDisplay createCraftingStationDisplay() {
		return new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE);
	}

	private static RecipeHolder<CraftingRecipe> createRecipeHolder(RecipeDisplay display) {
		ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(
			Registries.RECIPE,
			Identifier.fromNamespaceAndPath("jei", "display_only_crafting_test")
		);
		return new RecipeHolder<>(recipeKey, new DisplayOnlyCraftingRecipe(display));
	}

	private record DisplayOnlyCraftingRecipe(RecipeDisplay recipeDisplay) implements CraftingRecipe {
		@Override
		public RecipeSerializer<? extends CraftingRecipe> getSerializer() {
			throw new UnsupportedOperationException();
		}

		@Override
		public CraftingBookCategory category() {
			return CraftingBookCategory.MISC;
		}

		@Override
		public boolean matches(CraftingInput input, Level level) {
			return false;
		}

		@Override
		public ItemStack assemble(CraftingInput input) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean showNotification() {
			return false;
		}

		@Override
		public String group() {
			return "";
		}

		@Override
		public PlacementInfo placementInfo() {
			return PlacementInfo.NOT_PLACEABLE;
		}

		@Override
		public List<RecipeDisplay> display() {
			return List.of(recipeDisplay);
		}
	}
}
