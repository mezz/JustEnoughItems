package mezz.jei.gui.overlay;

import java.util.List;

import mezz.jei.config.JEIClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.textures.Textures;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

public class ConfigButton extends GuiIconToggleButton {
	public static ConfigButton create(IngredientListOverlay parent, IWorldConfig worldConfig) {
		Textures textures = Internal.getTextures();
		return new ConfigButton(textures.getConfigButtonIcon(), textures.getConfigButtonCheatIcon(), parent, worldConfig);
	}

	private final IngredientListOverlay parent;
	private final IWorldConfig worldConfig;

	private ConfigButton(IDrawable disabledIcon, IDrawable enabledIcon, IngredientListOverlay parent, IWorldConfig worldConfig) {
		super(disabledIcon, enabledIcon);
		this.parent = parent;
		this.worldConfig = worldConfig;
	}

	@Override
	protected void getTooltips(List<ITextComponent> tooltip) {
		tooltip.add(new TranslationTextComponent("jei.tooltip.config"));
		if (!worldConfig.isOverlayEnabled()) {
			TranslationTextComponent disabled = new TranslationTextComponent("jei.tooltip.ingredient.list.disabled");
			TranslationTextComponent overLay = new TranslationTextComponent(KeyBindings.toggleOverlay.getTranslationKey());
			TranslationTextComponent disabledFix = new TranslationTextComponent("jei.tooltip.ingredient.list.disabled.how.to.fix", overLay);
			tooltip.add(disabled.mergeStyle(TextFormatting.GOLD));
			tooltip.add(disabledFix.mergeStyle(TextFormatting.GOLD));
		} else if (!parent.isListDisplayed()) {
			TranslationTextComponent notEnoughSpace = new TranslationTextComponent("jei.tooltip.not.enough.space");
			tooltip.add(notEnoughSpace.mergeStyle(TextFormatting.GOLD));
		}
		if (worldConfig.isCheatItemsEnabled()) {
			TranslationTextComponent enabled = new TranslationTextComponent("jei.tooltip.cheat.mode.button.enabled");
			tooltip.add(enabled.mergeStyle(TextFormatting.RED));
			KeyBinding toggleCheatMode = KeyBindings.toggleCheatMode;
			if (toggleCheatMode.getKey().getKeyCode() != 0) {
				TranslationTextComponent cheatMode = new TranslationTextComponent(toggleCheatMode.getTranslationKey());
				TranslationTextComponent disableHotkey = new TranslationTextComponent("jei.tooltip.cheat.mode.how.to.disable.hotkey", cheatMode);
				tooltip.add(disableHotkey.mergeStyle(TextFormatting.RED));
			} else {
				TranslationTextComponent controlKeyLocalization = new TranslationTextComponent(Minecraft.IS_RUNNING_ON_MAC ? "key.jei.ctrl.mac" : "key.jei.ctrl");
				TranslationTextComponent noHotKey = new TranslationTextComponent("jei.tooltip.cheat.mode.how.to.disable.no.hotkey", controlKeyLocalization);
				tooltip.add(noHotKey.mergeStyle(TextFormatting.RED));
			}
		}
	}

	@Override
	protected boolean isIconToggledOn() {
		return worldConfig.isCheatItemsEnabled();
	}

	@Override
	protected boolean onMouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (worldConfig.isOverlayEnabled()) {
			long windowHandle = Minecraft.getInstance().getMainWindow().getHandle();
			if (InputMappings.isKeyDown(windowHandle, GLFW.GLFW_KEY_LEFT_CONTROL) || InputMappings.isKeyDown(windowHandle, GLFW.GLFW_KEY_RIGHT_CONTROL)) {
				worldConfig.toggleCheatItemsEnabled();
			} else {
				JEIClientConfig.openSettings();
			}
			return true;
		}
		return false;
	}
}
