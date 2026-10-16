package mezz.jei.fabric.test;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.common.Internal;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.plugins.vanilla.cooking.AbstractCookingCategory;
import mezz.jei.library.plugins.vanilla.cooking.BlastingCategory;
import mezz.jei.library.plugins.vanilla.cooking.CampfireCookingCategory;
import mezz.jei.library.plugins.vanilla.cooking.CookingCategoryExtension;
import mezz.jei.library.plugins.vanilla.cooking.FurnaceSmeltingCategory;
import mezz.jei.library.plugins.vanilla.cooking.SmokingCategory;
import net.minecraft.client.Minecraft;
import mezz.jei.common.gui.elements.TextWidget;
import net.minecraft.network.chat.FormattedText;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
final class CookingCategoryExtensionClientTest {
	private CookingCategoryExtensionClientTest() {
	}

	static void run(FabricClientTestContext context) {
		context.runOnClient(client -> {
			var guiHelper = Internal.getJeiRuntime().getJeiHelpers().getGuiHelper();
			var extension = new TestExtension(Ingredient.of(Items.LAVA_BUCKET));
			assertCookingInfo(client, new FurnaceSmeltingCategory(guiHelper), RecipeTypes.SMELTING, extension, true);
			assertCookingInfo(client, new BlastingCategory(guiHelper, List.of()), RecipeTypes.BLASTING, extension, true);
			assertCookingInfo(client, new SmokingCategory(guiHelper, List.of()), RecipeTypes.SMOKING, extension, true);
			assertCookingInfo(client, new CampfireCookingCategory(guiHelper), RecipeTypes.CAMPFIRE_COOKING, extension, false);

			var noFuel = new FurnaceSmeltingCategory(guiHelper);
			noFuel.addExtension(AbstractCookingRecipe.class, new TestExtension(null));
			var layout = createLayout(noFuel, firstRecipe(RecipeTypes.SMELTING));
			check(layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.INPUT).size() == 1, "Empty fuel must leave only the cooking input");
			check(layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.RENDER_ONLY).isEmpty(), "Empty fuel must remove the fuel slot");
		});
	}

	private static <R extends AbstractCookingRecipe> void assertCookingInfo(Minecraft client, AbstractCookingCategory<R> category, RecipeType<R> type, TestExtension extension, boolean hasFuel) {
		var recipe = firstRecipe(type);
		float defaultExperience = 0;
		if (hasFuel) {
			defaultExperience = recipe.getExperience();
		}
		assertLabels(client, createLayout(category, recipe), recipe.getCookingTime(), defaultExperience);
		category.addExtension(AbstractCookingRecipe.class, extension);
		var layout = createLayout(category, recipe);
		assertLabels(client, layout, recipe.getCookingTime() + 100, defaultExperience + 0.75f);
		int expectedSlots = 1;
		if (hasFuel) {
			expectedSlots = 2;
			var fuel = layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.INPUT).get(1).getDisplayedItemStack().orElseThrow();
			check(fuel.is(Items.LAVA_BUCKET), "The layout must display extension-selected fuel");
			var remainder = layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.OUTPUT).get(1).getDisplayedItemStack().orElseThrow();
			check(remainder.is(Items.BUCKET), "The layout must display the fuel remainder");
		}
		check(layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.INPUT).size() == expectedSlots, "Cooking input and fuel slot count");
		check(layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.OUTPUT).size() == expectedSlots, "Cooking output and remainder slot count");
	}

	private static void assertLabels(Minecraft client, IRecipeLayoutDrawable<?> layout, int cookingTime, float experience) {
		layout.drawRecipe(new PoseStack(), -1, -1);
		List<String> labels = renderedLabels(layout);
		String time = Component.translatable("gui.jei.category.smelting.time.seconds", cookingTime / 20).getString();
		check(labels.contains(time), "Cooking time label must use the extension value: " + time + " in " + labels);
		if (experience > 0) {
			String experienceText = Component.translatable("gui.jei.category.smelting.experience", experience).getString();
			check(labels.contains(experienceText), "Experience label must use the extension value: " + experienceText + " in " + labels);
		}
	}

	private static <R extends AbstractCookingRecipe> R firstRecipe(RecipeType<R> type) {
		return Internal.getJeiRuntime().getRecipeManager().createRecipeLookup(type).get()
			.filter(recipe -> !recipe.getId().getPath().startsWith("/"))
			.findFirst().orElseThrow();
	}

	private static <R extends AbstractCookingRecipe> IRecipeLayoutDrawable<R> createLayout(AbstractCookingCategory<R> category, R recipe) {
		return Internal.getJeiRuntime().getRecipeManager().createRecipeLayoutDrawable(category, recipe, FocusGroup.EMPTY).orElseThrow();
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static final class TestExtension extends CookingCategoryExtension {
		private final @Nullable Ingredient fuel;

		private TestExtension(@Nullable Ingredient fuel) {
			this.fuel = fuel;
		}

		@Override
		public @Nullable Ingredient getFuel(AbstractCookingRecipe recipe, @Nullable Ingredient fuel) {
			return this.fuel;
		}

		@Override
		public ItemStack getFuelOutput(AbstractCookingRecipe recipe, ItemStack fuelOutput) {
			if (fuel == null) {
				return ItemStack.EMPTY;
			}
			return new ItemStack(Items.BUCKET);
		}

		@Override
		public int getCookingTime(AbstractCookingRecipe recipe, int cookingTime) {
			return cookingTime + 100;
		}

		@Override
		public float getExperience(AbstractCookingRecipe recipe, float experience) {
			return experience + 0.75f;
		}
	}

	private static List<String> renderedLabels(IRecipeLayoutDrawable<?> layout) {
		try {
			var widgetsField = layout.getClass().getDeclaredField("allWidgets");
			widgetsField.setAccessible(true);
			var linesField = TextWidget.class.getDeclaredField("wrappedText");
			linesField.setAccessible(true);
			List<String> labels = new ArrayList<>();
			for (Object widget : (List<?>) widgetsField.get(layout)) {
				if (widget instanceof TextWidget) {
					for (Object line : (List<?>) linesField.get(widget)) {
						labels.add(((FormattedText) line).getString());
					}
				}
			}
			return labels;
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Unable to inspect rendered cooking labels", e);
		}
	}
}
