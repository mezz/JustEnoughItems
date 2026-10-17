package mezz.jei.gui.overlay;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.IMouseOverable;
import mezz.jei.common.input.interaction.InputAction;
import mezz.jei.common.platform.IPlatformConfigHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.gui.input.InputCommands;
import mezz.jei.gui.util.CheatModeUtil;
import net.mezzdev.config.api.value.IConfigValue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public class ConfigButtonController implements IIconButtonController {
	private final IDrawable normalIcon;
	private final IDrawable cheatIcon;
	private final BooleanSupplier isSearchDisplayed;
	private final BooleanSupplier isListHiddenWhenNotSearching;
	private final IConfigValue<Boolean> hideIngredientsWhenNotSearching;
	private final IClientToggleState toggleState;
	private final IInternalKeyMappings keyBindings;

	public ConfigButtonController(BooleanSupplier isSearchDisplayed, BooleanSupplier isListHiddenWhenNotSearching, IConfigValue<Boolean> hideIngredientsWhenNotSearching, IClientToggleState toggleState, IInternalKeyMappings keyBindings) {
		Textures textures = Internal.getTextures();
		this.normalIcon = textures.getConfigButtonIcon();
		this.cheatIcon = textures.getConfigButtonCheatIcon();
		this.isSearchDisplayed = isSearchDisplayed;
		this.isListHiddenWhenNotSearching = isListHiddenWhenNotSearching;
		this.hideIngredientsWhenNotSearching = hideIngredientsWhenNotSearching;
		this.toggleState = toggleState;
		this.keyBindings = keyBindings;
	}

	@Override
	public void updateState(IButtonState state) {
		if (toggleState.isCheatItemsEnabled()) {
			state.setIcon(cheatIcon);
		} else {
			state.setIcon(normalIcon);
		}
	}

	@Override
	public void getTooltips(ITooltipBuilder tooltip) {
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
		} else if (!isSearchDisplayed.getAsBoolean()) {
			tooltip.add(
				Component.translatable("jei.tooltip.not.enough.space")
					.withStyle(ChatFormatting.GOLD)
			);
		} else if (isListHiddenWhenNotSearching.getAsBoolean()) {
			tooltip.add(Component.translatable("jei.tooltip.ingredient.list.hidden.when.not.searching")
				.withStyle(ChatFormatting.GOLD));
		}
		if (toggleState.isOverlayEnabled() && hideIngredientsWhenNotSearching.get() && !keyBindings.getToggleHideIngredientsWhenNotSearching().isUnbound()) {
			tooltip.addKeyUsageComponent("jei.tooltip.ingredient.list.show.always.hotkey", keyBindings.getToggleHideIngredientsWhenNotSearching());
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
	public boolean onPress(IJeiUserInput input) {
		if (toggleState.isOverlayEnabled()) {
			if (!input.isSimulate()) {
				if (input.is(keyBindings.getToggleCheatModeConfigButton())) {
					CheatModeUtil.toggleCheatMode(toggleState);
				} else if (input.is(keyBindings.getToggleHideIngredientsWhenNotSearching())) {
					toggleHideIngredientsWhenNotSearching();
				} else {
					openSettings();
				}
			}
			return true;
		}
		return false;
	}

	void registerInputCommands(InputCommands commands, IMouseOverable buttonArea) {
		commands.add(
			InputCommands.Scope.HOVERED,
			"Toggle Hide Ingredients When Not Searching",
			input -> toggleState.isOverlayEnabled() && input.is(keyBindings.getToggleHideIngredientsWhenNotSearching()) && buttonArea.isMouseOver(input.getMouseX(), input.getMouseY()),
			(screen, input) -> Optional.of(InputAction.run(this::toggleHideIngredientsWhenNotSearching)
				.within(buttonArea)
				.when(toggleState::isOverlayEnabled))
		);
	}

	private void toggleHideIngredientsWhenNotSearching() {
		hideIngredientsWhenNotSearching.set(!hideIngredientsWhenNotSearching.get());
	}

	private static void openSettings() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}

		IPlatformConfigHelper configHelper = Services.PLATFORM.getConfigHelper();
		Optional<Screen> configScreen = configHelper.getConfigScreen(ModIds.JEI_ID, mc.screen);

		if (configScreen.isPresent()) {
			mc.setScreen(configScreen.get());
		} else {
			Component message = getMissingConfigScreenMessage(configHelper);
			mc.player.displayClientMessage(message, false);
		}
	}

	private static Component getMissingConfigScreenMessage(IPlatformConfigHelper configHelper) {
		return Component.translatable("jei.message.mezzConfigGui")
			.setStyle(
				Style.EMPTY
					.withColor(ChatFormatting.DARK_BLUE)
					.withUnderlined(true)
					.withClickEvent(
						new ClickEvent(
							ClickEvent.Action.OPEN_URL,
							"https://www.curseforge.com/minecraft/mc-mods/mezzconfiggui"
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
