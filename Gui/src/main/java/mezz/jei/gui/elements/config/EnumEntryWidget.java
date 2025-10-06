package mezz.jei.gui.elements.config;

import mezz.jei.common.config.file.ConfigValue;
import mezz.jei.gui.input.IUserInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

final class EnumEntryWidget<T extends Enum<T>> extends EntryWidget<T> {

	EnumEntryWidget(ConfigValue<T> value) {
		super(value);
	}

	@Override
	public void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {
		Component localizedName = getLocalizedName();
		guiGraphics.drawString(Minecraft.getInstance().font, localizedName, area.getX(), area.getY(), 0xFFFFFF);
		Component valueText = Component.literal(getValue().toString());
		guiGraphics.drawString(Minecraft.getInstance().font, valueText, area.getX() + 16, area.getY(), 0xFFFFFF);
	}

	@Override
	public IUserInputHandler createInputHandler() {
		return null;
	}
}
