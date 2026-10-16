package mezz.jei.fabric.test;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.common.Internal;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.plugins.vanilla.cooking.AbstractCookingCategory;
import mezz.jei.library.plugins.vanilla.cooking.BlastingCategory;
import mezz.jei.library.plugins.vanilla.cooking.CampfireCookingCategory;
import mezz.jei.library.plugins.vanilla.cooking.CookingCategoryExtension;
import mezz.jei.library.plugins.vanilla.cooking.FurnaceSmeltingCategory;
import mezz.jei.library.plugins.vanilla.cooking.SmokingCategory;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
final class CookingCategoryExtensionClientTest {
	private CookingCategoryExtensionClientTest() {
	}

	static void run(ClientGameTestContext context) {
		context.runOnClient(client -> {
			var guiHelper = Internal.getJeiRuntime().getJeiHelpers().getGuiHelper();
			var extension = new TestExtension(new SlotDisplay.WithRemainder(new SlotDisplay.ItemSlotDisplay(Items.LAVA_BUCKET), new SlotDisplay.ItemSlotDisplay(Items.BUCKET)));
			assertCookingInfo(client, new FurnaceSmeltingCategory(guiHelper), RecipeTypes.SMELTING, extension, true);
			assertCookingInfo(client, new BlastingCategory(guiHelper), RecipeTypes.BLASTING, extension, true);
			assertCookingInfo(client, new SmokingCategory(guiHelper), RecipeTypes.SMOKING, extension, true);
			assertCookingInfo(client, new CampfireCookingCategory(guiHelper), RecipeTypes.CAMPFIRE_COOKING, extension, false);

			var noFuel = new FurnaceSmeltingCategory(guiHelper);
			noFuel.addExtension(AbstractCookingRecipe.class, new TestExtension(SlotDisplay.Empty.INSTANCE));
			var layout = createLayout(noFuel, firstRecipe(RecipeTypes.SMELTING));
			check(layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.INPUT).size() == 1, "Empty fuel must leave only the cooking input");
			check(layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.RENDER_ONLY).isEmpty(), "Empty fuel must remove the fuel slot");
		});
	}

	private static <R extends AbstractCookingRecipe> void assertCookingInfo(Minecraft client, AbstractCookingCategory<R> category, IRecipeHolderType<R> type, TestExtension extension, boolean hasFuel) {
		var recipe = firstRecipe(type);
		var display = (FurnaceRecipeDisplay) recipe.value().display().getFirst();
		float defaultExperience = 0;
		if (hasFuel) {
			defaultExperience = display.experience();
		}
		assertLabels(client, createLayout(category, recipe), display.duration(), defaultExperience);
		category.addExtension(AbstractCookingRecipe.class, extension);
		var layout = createLayout(category, recipe);
		assertLabels(client, layout, display.duration() + 100, defaultExperience + 0.75f);
		int expectedSlots = 1;
		if (hasFuel) {
			expectedSlots = 2;
			var fuel = layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.INPUT).getLast().getDisplayedItemStack().orElseThrow();
			check(fuel.is(Items.LAVA_BUCKET), "The layout must display extension-selected fuel");
			var remainder = layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.OUTPUT).getLast().getDisplayedItemStack().orElseThrow();
			check(remainder.is(Items.BUCKET), "The layout must display the fuel remainder");
		}
		check(layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.INPUT).size() == expectedSlots, "Cooking input and fuel slot count");
		check(layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.OUTPUT).size() == expectedSlots, "Cooking output and remainder slot count");
	}

	private static void assertLabels(Minecraft client, IRecipeLayoutDrawable<?> layout, int cookingTime, float experience) {
		var graphics = new TextCapturingGraphics(client);
		layout.drawRecipe(graphics, -1, -1);
		String time = Component.translatable("gui.jei.category.smelting.time.seconds", cookingTime / 20).getString();
		check(graphics.text.contains(time), "Cooking time label must use the extension value: " + time + " in " + graphics.text);
		if (experience > 0) {
			String experienceText = Component.translatable("gui.jei.category.smelting.experience", experience).getString();
			check(graphics.text.contains(experienceText), "Experience label must use the extension value: " + experienceText + " in " + graphics.text);
		}
	}

	private static <R extends AbstractCookingRecipe> RecipeHolder<R> firstRecipe(IRecipeHolderType<R> type) {
		return Internal.getJeiRuntime().getRecipeManager().createRecipeLookup(type).get()
			.filter(recipe -> !recipe.id().identifier().getPath().startsWith("/"))
			.findFirst().orElseThrow();
	}

	private static <R extends AbstractCookingRecipe> IRecipeLayoutDrawable<RecipeHolder<R>> createLayout(AbstractCookingCategory<R> category, RecipeHolder<R> recipe) {
		return Internal.getJeiRuntime().getRecipeManager().createRecipeLayoutDrawable(category, recipe, FocusGroup.EMPTY).orElseThrow();
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static final class TestExtension extends CookingCategoryExtension {
		private final SlotDisplay fuel;

		private TestExtension(SlotDisplay fuel) {
			this.fuel = fuel;
		}

		@Override
		public SlotDisplay getFuel(AbstractCookingRecipe recipe, SlotDisplay fuel) {
			return this.fuel;
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

	private static final class TextCapturingGraphics extends GuiGraphics {
		private final List<String> text = new ArrayList<>();

		private TextCapturingGraphics(Minecraft client) {
			super(client, new GuiRenderState(), -1, -1);
		}

		@Override
		public void drawString(Font font, FormattedCharSequence line, int x, int y, int color, boolean shadow) {
			StringBuilder text = new StringBuilder();
			line.accept((index, style, codePoint) -> {
				text.appendCodePoint(codePoint);
				return true;
			});
			this.text.add(text.toString());
			super.drawString(font, line, x, y, color, shadow);
		}
	}
}
