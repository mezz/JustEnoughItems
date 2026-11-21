package mezz.jei.fabric.test;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.Internal;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Checks independent badge settings against recipe slot rendering in a loaded world. */
@SuppressWarnings("UnstableApiUsage")
public final class IngredientBadgeClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReport("fabric-client-rendering", "ingredientBadges", () -> {
			try (TestSingleplayerContext ignored = context.worldBuilder().create()) {
				JeiFabricClientGameTestAssertions.assertJeiStartedWithSyncedRecipes(context);
				context.runOnClient(IngredientBadgeClientGameTest::checkBadges);
			}
		});
	}

	private static void checkBadges(Minecraft client) {
		var helper = Internal.getJeiRuntime().getJeiHelpers().getGuiHelper();
		var planks = Objects.requireNonNull(client.level).registryAccess().lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.PLANKS);
		var tagSlot = helper.createRecipeSlotDrawable(RecipeIngredientRole.INPUT,
			acceptor -> acceptor.add(new SlotDisplay.TagSlotDisplay(planks)), Set.of(), 0);
		var listSlot = helper.createRecipeSlotDrawable(RecipeIngredientRole.INPUT,
			acceptor -> acceptor.add(Items.STICK).add(Items.DIAMOND), Set.of(), 0);
		var singleSlot = helper.createRecipeSlotDrawable(RecipeIngredientRole.INPUT,
			acceptor -> acceptor.add(Items.STICK), Set.of(), 0);
		var emptySlot = helper.createRecipeSlotDrawable(RecipeIngredientRole.INPUT,
			acceptor -> {}, Set.of(), 0);
		check(tagSlot.getTagKey().orElseThrow().equals(ItemTags.PLANKS), "The tag slot must retain its plank tag");
		check(listSlot.getTagKey().isEmpty(), "The alternative list must not match a tag");
		int tagIngredients = tagSlot.getAllIngredientsList().size();
		check(tagIngredients > 1, "The plank tag must resolve multiple ingredients");

		var config = Internal.getClientConfigs().getClientConfig();
		boolean originalTagBadges = config.showTagBadges().get();
		boolean originalListBadges = config.showListBadges().get();
		try {
			for (boolean showTags : List.of(false, true)) {
				for (boolean showLists : List.of(false, true)) {
					config.showTagBadges().set(showTags);
					config.showListBadges().set(showLists);
					assertBadgeCount(client, tagSlot, showTags);
					assertBadgeCount(client, listSlot, showLists);
					assertBadgeCount(client, singleSlot, false);
					assertBadgeCount(client, emptySlot, false);
					check(tagSlot.getAllIngredientsList().size() == tagIngredients && listSlot.getAllIngredientsList().size() == 2,
						"Hiding badges must preserve the alternative ingredients");
					check(tagSlot.getTagKey().orElseThrow().equals(ItemTags.PLANKS), "Hiding badges must preserve tag identity");
				}
			}
		} finally {
			config.showTagBadges().set(originalTagBadges);
			config.showListBadges().set(originalListBadges);
		}
	}

	private static void assertBadgeCount(Minecraft client, IRecipeSlotDrawable slot, boolean expected) {
		var state = new GuiRenderState();
		var graphics = new GuiGraphicsExtractor(client, state, -1, -1);
		slot.setPosition(20, 20);
		slot.draw(graphics, false);
		List<BlitRenderState> badges = new ArrayList<>();
		state.forEachElement(element -> {
			if (element instanceof BlitRenderState blit && blit.x1() - blit.x0() == 9 && blit.y1() - blit.y0() == 9) {
				badges.add(blit);
			}
		}, GuiRenderState.TraverseRange.ALL);
		int expectedCount = 0;
		if (expected) {
			expectedCount = 1;
		}
		check(badges.size() == expectedCount, "Expected " + expectedCount + " ingredient badges, got " + badges.size());
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
