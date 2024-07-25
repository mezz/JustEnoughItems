package mezz.jei.gui.overlay;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PacketRequestCheatPermission;
import mezz.jei.common.platform.IPlatformConfigHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.input.UserInput;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public class ConfigButton extends GuiIconToggleButton {
	private final IInternalKeyMappings keyBindings;

	public static ConfigButton create(BooleanSupplier isListDisplayed, IClientToggleState toggleState, IInternalKeyMappings keyBindings) {
		Textures textures = Internal.getTextures();
		return new ConfigButton(textures.getConfigButtonIcon(), textures.getConfigButtonCheatIcon(), isListDisplayed, toggleState, keyBindings);
	}

	private final BooleanSupplier isListDisplayed;
	private final IClientToggleState toggleState;

	private ConfigButton(IDrawable disabledIcon, IDrawable enabledIcon, BooleanSupplier isListDisplayed, IClientToggleState toggleState, IInternalKeyMappings keyBindings) {
		super(disabledIcon, enabledIcon);
		this.isListDisplayed = isListDisplayed;
		this.toggleState = toggleState;
		this.keyBindings = keyBindings;
	}

	@Override
	protected void getTooltips(JeiTooltip tooltip) {
		tooltip.add(Component.translatable("jei.tooltip.config"));
		if (!toggleState.isOverlayEnabled()) {
			tooltip.add(
				Component.translatable("jei.tooltip.ingredient.list.disabled")
					.withStyle(ChatFormatting.GOLD)
			);
			tooltip.addKeyUsageComponent(
				"jei.tooltip.ingredient.list.disabled.how.to.fix",
				keyBindings.getToggleOverlay()
			);
		} else if (!isListDisplayed.getAsBoolean()) {
			tooltip.add(
				Component.translatable("jei.tooltip.not.enough.space")
					.withStyle(ChatFormatting.GOLD)
			);
		}
		if (toggleState.isCheatItemsEnabled()) {
			tooltip.add(
				Component.translatable("jei.tooltip.cheat.mode.button.enabled")
					.withStyle(ChatFormatting.RED)
			);

			if (!keyBindings.getToggleCheatMode().isUnbound()) {
				tooltip.addKeyUsageComponent(
					"jei.tooltip.cheat.mode.how.to.disable.hotkey",
					keyBindings.getToggleCheatMode()
				);
			} else if (!keyBindings.getToggleCheatModeConfigButton().isUnbound()) {
				tooltip.addKeyUsageComponent(
					"jei.tooltip.cheat.mode.how.to.disable.hover.config.button.hotkey",
					keyBindings.getToggleCheatModeConfigButton()
				);
			}
		}
	}

	@Override
	protected boolean isIconToggledOn() {
		return toggleState.isCheatItemsEnabled();
	}

	@Override
	protected boolean onMouseClicked(UserInput input) {
		if (toggleState.isOverlayEnabled()) {
			if (!input.isSimulate()) {
				if (input.is(keyBindings.getToggleCheatModeConfigButton())) {
					toggleState.toggleCheatItemsEnabled();
					if (toggleState.isCheatItemsEnabled()) {
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
