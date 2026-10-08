package mezz.jei.fabric.test;

import mezz.jei.common.Internal;
import mezz.jei.common.gui.InventoryEffectRenderer;
import mezz.jei.gui.overlay.IngredientListOverlay;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.List;
import java.util.Objects;

/** Checks status effect layout against the real inventory screen and ingredient overlay. */
@SuppressWarnings("UnstableApiUsage")
final class InventoryEffectsClientTest {
	private InventoryEffectsClientTest() {}

	static void run() {
		var context = new FabricClientTestContext();
		var originalScreen = context.computeOnClient(client -> client.screen);
		var runtime = Internal.getJeiRuntime();
		var overlay = (IngredientListOverlay) runtime.getIngredientListOverlay();
		var toggles = Internal.getClientToggleState();
		var compact = Internal.getClientConfigs().getClientConfig().compactInventoryEffects();
		boolean originalCompact = compact.get();
		boolean originalOverlay = toggles.isOverlayEnabled();
		String originalFilter = runtime.getIngredientFilter().getFilterText();
		List<MobEffectInstance> originalEffects = context.computeOnClient(client -> Objects.requireNonNull(client.player).getActiveEffects().stream().map(MobEffectInstance::new).toList());
		try {
			context.runOnClient(client -> {
				var player = Objects.requireNonNull(client.player);
				List.copyOf(player.getActiveEffects()).forEach(effect -> player.removeEffect(effect.getEffect()));
				player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2400, 1));
				compact.set(true);
				toggles.setOverlayEnabled(true);
				runtime.getIngredientFilter().setFilterText("");
				client.setScreen(new InventoryScreen(player));
			});
			context.waitTicks(3);
			context.runOnClient(client -> {
				var screen = (InventoryScreen) client.screen;
				var expanded = InventoryEffectRenderer.getEffectAreas(screen, false);
				var collapsed = InventoryEffectRenderer.getEffectAreas(screen, true);
				check(expanded.size() == 1 && expanded.getFirst().width() > InventoryEffectRenderer.COMPACT_WIDTH,
					"The inventory must have room for a wide status effect bar");
				check(collapsed.size() == 1 && collapsed.getFirst().width() == InventoryEffectRenderer.COMPACT_WIDTH,
					"Compact exclusions must match the vanilla icon bar");
				check(overlay.isListDisplayed() && overlay.shouldRenderCompactInventoryEffects(screen),
					"An overlapping ingredient list must compact the effect bar");
			});
			context.takeScreenshot("jei-inventory-effects-compact");

			// Empty results leave the search field available without squeezing effects beside the inventory.
			context.runOnClient(client -> runtime.getIngredientFilter().setFilterText("jei_effect_test_no_matching_ingredient"));
			context.waitTicks(3);
			context.runOnClient(client -> {
				check(!overlay.isListDisplayed(), "The test filter must hide the ingredient grid");
				check(!overlay.shouldRenderCompactInventoryEffects((InventoryScreen) client.screen),
					"Effects must stay wide when no JEI surface overlaps them");
			});
			context.takeScreenshot("jei-inventory-effects-expanded");

			context.runOnClient(client -> {
				runtime.getIngredientFilter().setFilterText("");
				compact.set(false);
			});
			context.waitTicks(3);
			context.runOnClient(client -> check(!overlay.shouldRenderCompactInventoryEffects((InventoryScreen) client.screen),
				"Disabling effect compacting must retain wide bars beside the ingredient list"));
			context.takeScreenshot("jei-inventory-effects-disabled");
		} finally {
			context.runOnClient(client -> {
				var player = Objects.requireNonNull(client.player);
				List.copyOf(player.getActiveEffects()).forEach(effect -> player.removeEffect(effect.getEffect()));
				originalEffects.forEach(player::addEffect);
				compact.set(originalCompact);
				toggles.setOverlayEnabled(originalOverlay);
				runtime.getIngredientFilter().setFilterText(originalFilter);
				client.setScreen(originalScreen);
			});
		}
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
