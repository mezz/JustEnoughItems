package mezz.jei.fabric.test;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.IngredientGridTooltipComponent;
import mezz.jei.common.gui.JeiTooltip;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@SuppressWarnings({"UnstableApiUsage", "removal"})
final class RecipeSlotTooltipClientTest {
	private RecipeSlotTooltipClientTest() {
	}

	static void run() {
		ClientTestUtil.runOnClient(client -> {
			var runtime = Internal.getJeiRuntime();
			var helpers = runtime.getJeiHelpers();
			var visibility = helpers.getIngredientVisibility();
			var showGrid = Internal.getClientConfigs().getClientConfig().tagContentTooltipEnabled();
			boolean originalShowGrid = showGrid.get();
			List<ItemStack> candidates = IntStream.rangeClosed(1, 10_000)
				.mapToObj(index -> {
					ItemStack stack = new ItemStack(Items.STONE, 64);
					if (index == 10_000) {
						stack = new ItemStack(Items.COAL, 64);
					}
					stack.set(DataComponents.CUSTOM_NAME, Component.literal("Tooltip candidate " + index));
					return stack;
				})
				.toList();
			ItemStack coal = candidates.getLast();
			IRecipeSlotDrawable slot = helpers.getGuiHelper().createRecipeSlotDrawable(
				RecipeIngredientRole.INPUT,
				acceptor -> acceptor.addItemStacks(candidates),
				Set.of(),
				0
			);
			try {
				showGrid.set(true);
				IngredientGridTooltipComponent<?> initialGrid = getGrid(slot);
				assertCandidateCount(slot, 10_000);
				if (getGrid(slot) != initialGrid) {
					throw new AssertionError("Repeated tooltip draws must reuse prepared candidates and their grid.");
				}
				if (!slot.getDisplayedIngredients().toList().getLast().getItemStack().orElseThrow().is(Items.COAL)) {
					throw new AssertionError("The tooltip must include coal at position 10,000.");
				}

				visibility.hideIngredients(VanillaTypes.ITEM_STACK, List.of(coal), Set.of(UidContext.Recipe));
				assertCandidateCount(slot, 10_000);
				if (getGrid(slot) != initialGrid) {
					throw new AssertionError("An open slot must retain its prepared tooltip candidates.");
				}
				IRecipeSlotDrawable reopenedSlot = helpers.getGuiHelper().createRecipeSlotDrawable(
					RecipeIngredientRole.INPUT,
					acceptor -> acceptor.addItemStacks(candidates),
					Set.of(),
					0
				);
				getGrid(reopenedSlot);
				assertCandidateCount(reopenedSlot, 9_999);
				visibility.unhideIngredients(VanillaTypes.ITEM_STACK, List.of(coal), Set.of(UidContext.Recipe));

				var overrides = slot.createDisplayOverrides();
				overrides.addItemStack(new ItemStack(Items.DIAMOND));
				assertCandidateCount(slot, 1);
				overrides.addItemStack(new ItemStack(Items.EMERALD));
				IngredientGridTooltipComponent<?> overriddenGrid = getGrid(slot);
				assertCandidateCount(slot, 2);
				if (overriddenGrid == initialGrid) {
					throw new AssertionError("Display overrides must not reuse the original candidates.");
				}
				slot.clearDisplayOverrides();
				getGrid(slot);
				assertCandidateCount(slot, 10_000);
			} finally {
				visibility.unhideIngredients(VanillaTypes.ITEM_STACK, List.of(coal), Set.of(UidContext.Recipe));
				showGrid.set(originalShowGrid);
			}
		});
	}

	private static IngredientGridTooltipComponent<?> getGrid(IRecipeSlotDrawable slot) {
		JeiTooltip tooltip = new JeiTooltip();
		slot.getTooltip(tooltip);
		return tooltip.getLines().stream()
			.flatMap(line -> line.right().stream())
			.filter(IngredientGridTooltipComponent.class::isInstance)
			.map(component -> (IngredientGridTooltipComponent<?>) component)
			.findFirst()
			.orElseThrow(() -> new AssertionError("Expected a candidate grid in the recipe slot tooltip."));
	}

	private static void assertCandidateCount(IRecipeSlotDrawable slot, long expected) {
		long actual = slot.getDisplayedIngredients().count();
		if (actual != expected) {
			throw new AssertionError("Expected " + expected + " tooltip candidates, got " + actual);
		}
	}
}
