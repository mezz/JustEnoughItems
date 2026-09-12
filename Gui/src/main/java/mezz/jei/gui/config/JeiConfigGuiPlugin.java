package mezz.jei.gui.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientConfigs;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.gui.util.CheatModeUtil;
import net.mezzdev.config.api.value.serializer.IConfigValueSerializer;
import net.mezzdev.config.gui.api.ConfigGuiPlugin;
import net.mezzdev.config.gui.api.IConfigGuiPlugin;
import net.mezzdev.config.gui.api.IConfigGuiRegistration;
import net.mezzdev.config.gui.api.IConfigScreenBuilder;
import net.minecraft.network.chat.Component;

/**
 * JEI config GUI customizations.
 */
@ConfigGuiPlugin
public class JeiConfigGuiPlugin implements IConfigGuiPlugin {
	@Override
	public String getModId() {
		return ModIds.JEI_ID;
	}

	@Override
	public void register(IConfigGuiRegistration registration) {
		registration.configureScreen(screenBuilder -> {
			screenBuilder.setTitle(Component.translatable("jei.config"));
			screenBuilder.configureCategory("debug")
				.clearDefaultValues();
			screenBuilder.configureCategory("input")
				.addKeyMappings(Internal.getKeyMappings().getConfigKeyMappings());
			Internal.getOptionalJeiRuntime()
				.ifPresent(ignored -> configureRuntimeToggleValues(screenBuilder));
		});
	}

	private static void configureRuntimeToggleValues(IConfigScreenBuilder screenBuilder) {
		IClientConfigs clientConfigs = Internal.getClientConfigs();
		IClientConfig clientConfig = clientConfigs.getClientConfig();
		IClientToggleState toggleState = Internal.getClientToggleState();
		IConfigValueSerializer<Boolean> serializer = clientConfig.cheatToHotbarUsingHotkeysEnabled().getEditorInfo().getSerializer();

		screenBuilder.configureCategory("ingredientList")
			.addScreenValue(new RuntimeToggleScreenValue(
				"overlaysEnabled",
				"jei.config.client.ingredientList.overlaysEnabled",
				true,
				toggleState::isOverlayEnabled,
				toggleState::setOverlayEnabled,
				toggleState::addOverlayEnabledListener,
				serializer
			));

		screenBuilder.configureCategory("bookmarkList")
			.addScreenValue(new RuntimeToggleScreenValue(
				"bookmarkOverlayEnabled",
				"jei.config.client.bookmarkList.bookmarkOverlayEnabled",
				true,
				toggleState::isBookmarkEnabled,
				toggleState::setBookmarkEnabled,
				toggleState::addBookmarkEnabledListener,
				serializer
			));

		screenBuilder.configureCategory("cheating")
			.addScreenValue(new RuntimeToggleScreenValue(
				"cheatModeEnabled",
				"jei.config.client.cheating.cheatModeEnabled",
				false,
				toggleState::isCheatItemsEnabled,
				value -> CheatModeUtil.setCheatModeEnabled(toggleState, value),
				toggleState::addCheatItemsEnabledListener,
				serializer
			));

		screenBuilder.configureCategory("advanced")
			.addScreenValue(new RuntimeToggleScreenValue(
				"editModeEnabled",
				"jei.config.client.advanced.editModeEnabled",
				false,
				toggleState::isEditModeEnabled,
				toggleState::setEditModeEnabled,
				toggleState::addEditModeEnabledListener,
				serializer
			));
	}
}
