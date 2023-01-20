package mezz.jei.gui.overlay;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.Internal;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PacketRequestCheatPermission;
import mezz.jei.common.platform.IPlatformConfigHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.gui.input.UserInput;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public class ConfigButton extends GuiIconToggleButton {
	private final IInternalKeyMappings keyBindings;

	public static ConfigButton create(BooleanSupplier isListDisplayed, IWorldConfig worldConfig, Textures textures, IInternalKeyMappings keyBindings) {
		return new ConfigButton(textures.getConfigButtonIcon(), textures.getConfigButtonCheatIcon(), isListDisplayed, worldConfig, textures, keyBindings);
	}

	private final BooleanSupplier isListDisplayed;
	private final IWorldConfig worldConfig;

	private ConfigButton(IDrawable disabledIcon, IDrawable enabledIcon, BooleanSupplier isListDisplayed, IWorldConfig worldConfig, Textures textures, IInternalKeyMappings keyBindings) {
		super(disabledIcon, enabledIcon, textures);
		this.isListDisplayed = isListDisplayed;
		this.worldConfig = worldConfig;
		this.keyBindings = keyBindings;
	}

	@Override
	protected void getTooltips(List<Component> tooltip) {
		tooltip.add(Component.translatable("jei.tooltip.config"));
		if (!worldConfig.isOverlayEnabled()) {
			MutableComponent disabled = Component.translatable("jei.tooltip.ingredient.list.disabled");
			MutableComponent disabledFix = Component.translatable(
				"jei.tooltip.ingredient.list.disabled.how.to.fix",
				keyBindings.getToggleOverlay().getTranslatedKeyMessage()
			);
			tooltip.add(disabled.withStyle(ChatFormatting.GOLD));
			tooltip.add(disabledFix.withStyle(ChatFormatting.GOLD));
		} else if (!isListDisplayed.getAsBoolean()) {
			MutableComponent notEnoughSpace = Component.translatable("jei.tooltip.not.enough.space");
			tooltip.add(notEnoughSpace.withStyle(ChatFormatting.GOLD));
		}
		if (worldConfig.isCheatItemsEnabled()) {
			MutableComponent enabled = Component.translatable("jei.tooltip.cheat.mode.button.enabled")
				.withStyle(ChatFormatting.RED);
			tooltip.add(enabled);

			if (!keyBindings.getToggleCheatMode().isUnbound()) {
				MutableComponent component = Component.translatable(
					"jei.tooltip.cheat.mode.how.to.disable.hotkey",
					keyBindings.getToggleCheatMode().getTranslatedKeyMessage()
				).withStyle(ChatFormatting.RED);
				tooltip.add(component);
			} else if (!keyBindings.getToggleCheatModeConfigButton().isUnbound()) {
				MutableComponent component = Component.translatable(
					"jei.tooltip.cheat.mode.how.to.disable.hover.config.button.hotkey",
					keyBindings.getToggleCheatModeConfigButton().getTranslatedKeyMessage()
				).withStyle(ChatFormatting.RED);
				tooltip.add(component);
			}
		}
	}

	@Override
	protected boolean isIconToggledOn() {
		return worldConfig.isCheatItemsEnabled();
	}

	@Override
	protected boolean onMouseClicked(UserInput input) {
		if (worldConfig.isOverlayEnabled()) {
			if (!input.isSimulate()) {
				if (input.is(keyBindings.getToggleCheatModeConfigButton())) {
					worldConfig.toggleCheatItemsEnabled();
					if (worldConfig.isCheatItemsEnabled()) {
						IConnectionToServer serverConnection = Internal.getServerConnection();
						serverConnection.sendPacketToServer(new PacketRequestCheatPermission());
					}
				} else {
					openSettings();
				}
			}
			return true;
		}
		return false;
	}

	private static void openSettings() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}

		IPlatformConfigHelper configHelper = Services.PLATFORM.getConfigHelper();
		Optional<Screen> configScreen = configHelper.getConfigScreen();

		if (configScreen.isPresent()) {
			mc.setScreen(configScreen.get());
		} else {
			Component message = getMissingConfigScreenMessage(configHelper);
			mc.player.displayClientMessage(message, false);
		}
	}

	private static Component getMissingConfigScreenMessage(IPlatformConfigHelper configHelper) {
		return Component.translatable("jei.message.configured")
			.setStyle(
				Style.EMPTY
					.withColor(ChatFormatting.DARK_BLUE)
					.withUnderlined(true)
					.withClickEvent(
						new ClickEvent(
							ClickEvent.Action.OPEN_URL,
							"https://www.curseforge.com/minecraft/mc-mods/configured"
						)
					)
			)
			.append("\n")
			.append(
				Component.translatable("jei.message.config.folder")
					.setStyle(
						Style.EMPTY
							.withColor(ChatFormatting.WHITE)
							.withUnderlined(true)
							.withClickEvent(
								new ClickEvent(
									ClickEvent.Action.OPEN_FILE,
									configHelper.createJeiConfigDir().toAbsolutePath().toString()
								)
							)
					)
			);
	}
}
