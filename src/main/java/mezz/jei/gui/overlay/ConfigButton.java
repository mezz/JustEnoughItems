package mezz.jei.gui.overlay;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.config.JEIModConfigGui;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.HoverChecker;
import org.lwjgl.input.Keyboard;

public class ConfigButton {
	private final IngredientListOverlay parent;
	private final GuiButton configButton;
	private final IDrawable configButtonIcon;
	private final IDrawable configButtonCheatIcon;
	private final HoverChecker configButtonHoverChecker;

	public ConfigButton(IngredientListOverlay parent) {
		this.parent = parent;
		this.configButton = new GuiButton(2, 0, 0, 0, 0, "");
		ResourceLocation configButtonIconLocation = Constants.RECIPE_BACKGROUND;
		GuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
		this.configButtonIcon = guiHelper.createDrawable(configButtonIconLocation, 0, 166, 16, 16);
		this.configButtonCheatIcon = guiHelper.createDrawable(configButtonIconLocation, 16, 166, 16, 16);
		this.configButtonHoverChecker = new HoverChecker(this.configButton, 0);
	}

	public void updateBounds(Rectangle area) {
		this.configButton.width = area.width;
		this.configButton.height = area.height;
		this.configButton.x = area.x;
		this.configButton.y = area.y;
	}

	public void draw(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
		this.configButton.drawButton(minecraft, mouseX, mouseY, partialTicks);

		IDrawable icon = Config.isCheatItemsEnabled() ? this.configButtonCheatIcon : this.configButtonIcon;
		icon.draw(minecraft, this.configButton.x + 2, this.configButton.y + 2);
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return this.configButtonHoverChecker.checkHover(mouseX, mouseY);
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY, boolean hasRoom) {
		if (isMouseOver(mouseX, mouseY)) {
			List<String> tooltip = new ArrayList<>();
			tooltip.add(Translator.translateToLocal("jei.tooltip.config"));
			if (!Config.isOverlayEnabled()) {
				tooltip.add(TextFormatting.GOLD + Translator.translateToLocal("jei.tooltip.ingredient.list.disabled"));
				tooltip.add(TextFormatting.GOLD + Translator.translateToLocalFormatted("jei.tooltip.ingredient.list.disabled.how.to.fix", KeyBindings.toggleOverlay.getDisplayName()));
			} else if (!hasRoom) {
				tooltip.add(TextFormatting.GOLD + Translator.translateToLocal("jei.tooltip.not.enough.space"));
			}
			if (Config.isCheatItemsEnabled()) {
				tooltip.add(TextFormatting.RED + Translator.translateToLocal("jei.tooltip.cheat.mode"));
				KeyBinding toggleCheatMode = KeyBindings.toggleCheatMode;
				if (toggleCheatMode.getKeyCode() != 0) {
					tooltip.add(TextFormatting.RED + Translator.translateToLocalFormatted("jei.tooltip.cheat.mode.how.to.disable.hotkey", toggleCheatMode.getDisplayName()));
				} else {
					String controlKeyLocalization = Translator.translateToLocal(Minecraft.IS_RUNNING_ON_MAC ? "key.jei.ctrl.mac" : "key.jei.ctrl");
					tooltip.add(TextFormatting.RED + Translator.translateToLocalFormatted("jei.tooltip.cheat.mode.how.to.disable.no.hotkey", controlKeyLocalization));
				}
			}
			TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY, Constants.MAX_TOOLTIP_WIDTH);
		}
	}

	public boolean handleMouseClick(Minecraft minecraft, int mouseX, int mouseY) {
		if (Config.isOverlayEnabled() && configButton.mousePressed(minecraft, mouseX, mouseY)) {
			configButton.playPressSound(minecraft.getSoundHandler());
			if (Keyboard.getEventKeyState() && (Keyboard.getEventKey() == Keyboard.KEY_LCONTROL || Keyboard.getEventKey() == Keyboard.KEY_RCONTROL)) {
				Config.toggleCheatItemsEnabled();
			} else {
				if (minecraft.currentScreen != null) {
					GuiScreen configScreen = new JEIModConfigGui(minecraft.currentScreen);
					parent.updateScreen(configScreen);
					minecraft.displayGuiScreen(configScreen);
				}
			}
			return true;
		}
		return false;
	}
}
