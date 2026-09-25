package mezz.jei.fabric.test;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.platform.Services;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
final class ConfigScreenClientTest {
	private ConfigScreenClientTest() {
	}

	static void run(ClientGameTestContext context) {
		if (FabricLoader.getInstance().isModLoaded("modmenu")) {
			throw new AssertionError("This test must run without Mod Menu to exercise the direct MezzConfig GUI integration");
		}
		if (!FabricLoader.getInstance().isModLoaded("mezz_config_gui")) {
			throw new AssertionError("Expected MezzConfig GUI to be installed in the client test runtime");
		}
		Screen originalScreen = context.computeOnClient(client -> client.gui.screen());
		Screen inventory = context.computeOnClient(client -> new InventoryScreen(Objects.requireNonNull(client.player)));
		try {
			context.setScreen(() -> inventory);
			context.runOnClient(client -> {
				Screen configScreen = Services.PLATFORM.getConfigHelper()
					.getConfigScreen(ModIds.JEI_ID, inventory)
					.orElseThrow(() -> new AssertionError("JEI settings must open with MezzConfig GUI installed and Mod Menu absent"));
				client.gui.setScreen(configScreen);
				String expectedTitle = Component.translatable("jei.config").getString();
				if (!configScreen.getTitle().getString().equals(expectedTitle)) {
					throw new AssertionError("Expected JEI settings, got: " + configScreen.getTitle().getString());
				}
			});
			context.waitTick();
			context.takeScreenshot("jei-config-without-mod-menu");
			context.runOnClient(client -> {
				Objects.requireNonNull(client.gui.screen()).onClose();
				if (client.gui.screen() != inventory) {
					throw new AssertionError("Closing JEI settings must return to the inventory screen");
				}
			});
		} finally {
			context.setScreen(() -> originalScreen);
		}
	}
}
