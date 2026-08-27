package mezz.jei.test;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.recipes.BrewingExtensionHelper;
import mezz.jei.forge.platform.BrewingHelper;
import mezz.jei.forge.platform.BrewingRecipeCategoryExtension;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.library.plugins.vanilla.VanillaRecipeFactory;
import mezz.jei.test.lib.ForgeTestBootstrap;
import mezz.jei.test.lib.TestColorHelper;

import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgeBrewingExtensionTest {
	private static final IIngredientHelper<ItemStack> ITEM_STACK_HELPER = new TestItemStackHelper();

	@BeforeAll
	public static void setup() {
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		ForgeTestBootstrap.bootStrap();
	}

	@Test
	public void registeredExtensionConvertsCustomBrewingRecipe() {
		IBrewingRecipe brewingRecipe = new CustomBrewingRecipe();
		BrewingExtensionHelper extensionHelper = new BrewingExtensionHelper();
		extensionHelper.addExtension(
			CustomBrewingRecipe.class,
			(recipe, vanillaRecipeFactory) -> List.of(
				vanillaRecipeFactory.createBrewingRecipe(
					List.of(new ItemStack(Items.NETHER_WART)),
					new ItemStack(Items.POTION),
					new ItemStack(Items.DIAMOND),
					new ResourceLocation("test", "custom_regular")
				),
				vanillaRecipeFactory.createBrewingRecipe(
					List.of(new ItemStack(Items.NETHER_WART)),
					new ItemStack(Items.SPLASH_POTION),
					new ItemStack(Items.EMERALD),
					new ResourceLocation("test", "custom_splash")
				)
			)
		);

		List<IJeiBrewingRecipe> recipes = extensionHelper.getBrewingRecipes(
			List.of(brewingRecipe),
			createRecipeFactory()
		);

		assertEquals(2, recipes.size());
		Set<ResourceLocation> uids = recipes.stream()
			.map(IJeiBrewingRecipe::getUid)
			.collect(Collectors.toSet());
		assertEquals(
			Set.of(
				new ResourceLocation("test", "custom_regular"),
				new ResourceLocation("test", "custom_splash")
			),
			uids
		);
	}

	@Test
	public void failingExtensionDoesNotStopOtherRecipeProcessing() {
		IBrewingRecipe brokenRecipe = new CustomBrewingRecipe();
		BrewingRecipe validRecipe = new BrewingRecipe(
			Ingredient.of(Items.POTION),
			Ingredient.of(Items.NETHER_WART),
			new ItemStack(Items.DIAMOND)
		);
		BrewingExtensionHelper extensionHelper = createExtensionHelper();
		extensionHelper.addExtension(CustomBrewingRecipe.class, (recipe, vanillaRecipeFactory) -> {
			throw new IllegalStateException("test failure");
		});

		List<IJeiBrewingRecipe> recipes = extensionHelper.getBrewingRecipes(
			List.of(brokenRecipe, validRecipe),
			createRecipeFactory()
		);

		assertEquals(1, recipes.size());
		assertSame(Items.DIAMOND, recipes.get(0).getPotionOutput().getItem());
	}

	@Test
	public void invalidStandardRecipesAreSkipped() {
		BrewingRecipe emptyInput = new BrewingRecipe(
			Ingredient.EMPTY,
			Ingredient.of(Items.NETHER_WART),
			new ItemStack(Items.DIAMOND)
		);
		BrewingRecipe emptyIngredient = new BrewingRecipe(
			Ingredient.of(Items.POTION),
			Ingredient.EMPTY,
			new ItemStack(Items.DIAMOND)
		);
		BrewingRecipe emptyOutput = new BrewingRecipe(
			Ingredient.of(Items.POTION),
			Ingredient.of(Items.NETHER_WART),
			ItemStack.EMPTY
		);
		BrewingRecipe validRecipe = new BrewingRecipe(
			Ingredient.of(Items.POTION),
			Ingredient.of(Items.NETHER_WART),
			new ItemStack(Items.DIAMOND)
		);

		List<IJeiBrewingRecipe> recipes = createExtensionHelper().getBrewingRecipes(
			List.of(emptyInput, emptyIngredient, emptyOutput, validRecipe),
			createRecipeFactory()
		);

		assertEquals(1, recipes.size());
		assertSame(Items.DIAMOND, recipes.get(0).getPotionOutput().getItem());
	}

	@Test
	public void vanillaBrewingRecipeUsesRegisteredExtension() {
		IIngredientManager ingredientManager = createIngredientManager();
		BrewingExtensionHelper extensionHelper = new BrewingExtensionHelper();
		BrewingHelper brewingHelper = new BrewingHelper();
		brewingHelper.registerCategoryExtensions(extensionHelper, ingredientManager);

		List<IJeiBrewingRecipe> recipes = brewingHelper.getBrewingRecipes(
			ingredientManager,
			createRecipeFactory(),
			extensionHelper
		);

		assertTrue(
			recipes.stream()
				.anyMatch(recipe ->
					recipe.getIngredients().stream().anyMatch(ingredient -> ingredient.is(Items.NETHER_WART)) &&
						PotionUtils.getPotion(recipe.getPotionOutput()) == Potions.AWKWARD
				)
		);
	}

	@Test
	public void standardRecipesWithTheSameOutputHaveUniqueIds() {
		BrewingRecipe firstRecipe = new BrewingRecipe(
			Ingredient.of(Items.POTION),
			Ingredient.of(Items.NETHER_WART),
			new ItemStack(Items.DIAMOND)
		);
		BrewingRecipe secondRecipe = new BrewingRecipe(
			Ingredient.of(Items.POTION),
			Ingredient.of(Items.REDSTONE),
			new ItemStack(Items.DIAMOND)
		);

		List<IJeiBrewingRecipe> recipes = createExtensionHelper().getBrewingRecipes(
			List.of(firstRecipe, secondRecipe),
			createRecipeFactory()
		);

		assertEquals(2, recipes.size());
		assertEquals(2, recipes.stream().map(IJeiBrewingRecipe::getUid).distinct().count());
		assertTrue(recipes.stream().allMatch(recipe -> recipe.getUid().getPath().length() == 72));
	}

	private static BrewingExtensionHelper createExtensionHelper() {
		BrewingExtensionHelper extensionHelper = new BrewingExtensionHelper();
		extensionHelper.addExtension(BrewingRecipe.class, new BrewingRecipeCategoryExtension(ITEM_STACK_HELPER));
		return extensionHelper;
	}

	private static IIngredientManager createIngredientManager() {
		SubtypeManager subtypeManager = new SubtypeManager(new SubtypeInterpreters());
		IColorHelper colorHelper = new TestColorHelper();
		IngredientManagerBuilder builder = new IngredientManagerBuilder(subtypeManager, colorHelper);
		builder.register(
			VanillaTypes.ITEM_STACK,
			List.of(new ItemStack(Items.NETHER_WART)),
			ITEM_STACK_HELPER,
			new NoOpItemStackRenderer()
		);
		return builder.build();
	}

	private static VanillaRecipeFactory createRecipeFactory() {
		return new VanillaRecipeFactory(ITEM_STACK_HELPER);
	}

	private static class CustomBrewingRecipe implements IBrewingRecipe {
		@Override
		public boolean isInput(ItemStack input) {
			return true;
		}

		@Override
		public boolean isIngredient(ItemStack ingredient) {
			return true;
		}

		@Override
		public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
			return new ItemStack(Items.DIAMOND);
		}
	}

	private static class TestItemStackHelper implements IIngredientHelper<ItemStack> {
		@Override
		public IIngredientType<ItemStack> getIngredientType() {
			return VanillaTypes.ITEM_STACK;
		}

		@Override
		public String getDisplayName(ItemStack ingredient) {
			return ingredient.getHoverName().getString();
		}

		@Override
		public String getUniqueId(ItemStack ingredient, UidContext context) {
			return ingredient.save(new CompoundTag()).toString();
		}

		@Override
		public ResourceLocation getResourceLocation(ItemStack ingredient) {
			return BuiltInRegistries.ITEM.getKey(ingredient.getItem());
		}

		@Override
		public ItemStack copyIngredient(ItemStack ingredient) {
			return ingredient.copy();
		}

		@Override
		public String getErrorInfo(@Nullable ItemStack ingredient) {
			return String.valueOf(ingredient);
		}
	}

	private static class NoOpItemStackRenderer implements IIngredientRenderer<ItemStack> {
		@Override
		public void render(GuiGraphics guiGraphics, ItemStack ingredient) {
		}

		@SuppressWarnings("removal")
		@Override
		public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
			return List.of();
		}
	}
}
