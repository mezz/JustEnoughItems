package mezz.jei.gui.elements.config;

import mezz.jei.common.config.file.ConfigValue;
import mezz.jei.gui.input.IUserInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

final class ListEntryWidget<T> extends EntryWidget<List<T>> {
	final @Nullable Set<T> allowedValues;

	ListEntryWidget(ConfigValue<List<T>> value, @Nullable Set<T> allowedValues) {
		super(value);
		this.allowedValues = allowedValues;
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
