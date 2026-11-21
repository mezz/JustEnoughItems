package mezz.jei.test.client;

import com.mojang.logging.LogUtils;
import mezz.jei.common.Internal;
import mezz.jei.common.platform.Services;
import mezz.jei.library.plugins.vanilla.anvil.AnvilHelper;
import mezz.jei.library.plugins.vanilla.grindstone.GrindstoneRecipeMaker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Tests real player replacement while JEI stays running. Run in a disposable creative world. */
public final class FakeMenuClientTest {
	private FakeMenuClientTest() {
	}

	public static void run(Consumer<Consumer<Minecraft>> runOnClient, Consumer<Predicate<Minecraft>> waitFor) {
		// The server ignores damage, including /kill, until the client reports that it has loaded.
		waitFor.accept(client -> client.player != null && client.getConnection() != null && client.getConnection().hasClientLoaded());
		AtomicReference<Player> oldPlayer = new AtomicReference<>();
		AtomicReference<Object> runtime = new AtomicReference<>();
		AtomicReference<Sample> expected = new AtomicReference<>();
		runOnClient.accept(client -> {
			var server = Objects.requireNonNull(client.getSingleplayerServer());
			server.execute(() -> LogUtils.getLogger().info("Fake-menu regression: doImmediateRespawn={}", server.getGameRules().get(GameRules.IMMEDIATE_RESPAWN)));
			runtime.set(Internal.getJeiRuntime());
			expected.set(sample(client));
			assertReusedAndEquivalent(expected.get(), sample(client));
			oldPlayer.set(client.player);
			Objects.requireNonNull(client.getConnection()).sendCommand("kill @s");
		});
		waitFor.accept(client -> client.player != null && (client.player != oldPlayer.get() || client.player.isDeadOrDying()));
		runOnClient.accept(client -> {
			if (Objects.requireNonNull(client.player).isDeadOrDying()) {
				client.player.respawn();
				client.gui.setScreen(null);
			}
		});
		waitFor.accept(client -> client.player != null && client.player != oldPlayer.get() && !client.player.isDeadOrDying());
		runOnClient.accept(client -> {
			assertCleared();
			assertEquivalent(expected.get(), sample(client));
			assertReusedAndEquivalent(sample(client), sample(client));
			oldPlayer.set(client.player);
			Objects.requireNonNull(client.getConnection()).sendCommand("execute in minecraft:the_nether run tp @s 0 90 0");
		});
		waitFor.accept(client -> client.player != null && client.player != oldPlayer.get() && client.level != null && client.level.dimension() == Level.NETHER);
		runOnClient.accept(client -> {
			assertCleared();
			assertEquivalent(expected.get(), sample(client));
			assertReusedAndEquivalent(sample(client), sample(client));
			if (Internal.getJeiRuntime() != runtime.get()) {
				throw new AssertionError("JEI restarted during player replacement; this did not test cache cleanup in an active runtime");
			}
		});
	}

	private static void assertCleared() {
		if (cached(AnvilHelper.class, "ANVIL_MENU") != null || cached(GrindstoneRecipeMaker.class, "GRINDSTONE_MENU") != null) {
			throw new AssertionError("Fake menus still retain the replaced player");
		}
	}

	private static Sample sample(Minecraft client) {
		AnvilMenu anvil = AnvilHelper.getFakeAnvilMenu();
		GrindstoneRecipeMaker.getGrindstoneRecipes(Internal.getJeiRuntime().getIngredientManager(), Services.PLATFORM.getRecipeHelper());
		GrindstoneMenu grindstone = (GrindstoneMenu) Objects.requireNonNull(cached(GrindstoneRecipeMaker.class, "GRINDSTONE_MENU"));
		assertOwner(anvil, Objects.requireNonNull(client.player));
		assertOwner(grindstone, client.player);
		ItemStack left = new ItemStack(Items.IRON_PICKAXE);
		left.setDamageValue(200);
		ItemStack right = left.copy();
		int cost = AnvilHelper.findLevelsCost(left, right);
		ItemStack anvilOutput = anvil.getSlot(2).getItem().copy();
		ItemStack grindstoneOutput = Services.PLATFORM.getRecipeHelper().getGrindstoneResult(grindstone, left, right).copy();
		if (cost < 0 || anvilOutput.isEmpty() || grindstoneOutput.isEmpty()) {
			throw new AssertionError("Expected nonempty repair recipes");
		}
		return new Sample(anvil, grindstone, anvilOutput, grindstoneOutput, cost);
	}

	private static void assertOwner(AbstractContainerMenu menu, Player player) {
		boolean found = false;
		for (var slot : menu.slots) {
			if (slot.container instanceof Inventory inventory) {
				found = true;
				if (inventory.player != player) {
					throw new AssertionError("Fake menu belongs to a previous player");
				}
			}
		}
		if (!found) {
			throw new AssertionError("Fake menu had no player inventory");
		}
	}

	private static @Nullable Object cached(Class<?> type, String name) {
		try {
			var field = type.getDeclaredField(name);
			field.setAccessible(true);
			return field.get(null);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}

	private static void assertReusedAndEquivalent(Sample before, Sample after) {
		if (before.anvil != after.anvil || before.grindstone != after.grindstone) {
			throw new AssertionError("Fake menus were rebuilt for the same player");
		}
		assertEquivalent(before, after);
	}

	private static void assertEquivalent(Sample before, Sample after) {
		if (before.cost != after.cost || !ItemStack.matches(before.anvilOutput, after.anvilOutput) || !ItemStack.matches(before.grindstoneOutput, after.grindstoneOutput)) {
			throw new AssertionError("Repair outputs or level cost changed after player replacement");
		}
	}

	private record Sample(AnvilMenu anvil, GrindstoneMenu grindstone, ItemStack anvilOutput, ItemStack grindstoneOutput, int cost) {
	}
}
