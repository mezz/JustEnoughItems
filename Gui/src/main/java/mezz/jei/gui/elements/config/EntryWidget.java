package mezz.jei.gui.elements.config;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IUserInputHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

sealed abstract class EntryWidget<T> permits
	BooleanEntryWidget,
	EnumEntryWidget,
	IntegerEntryWidget,
	ListEntryWidget {
	final IJeiConfigValue<T> value;
	ImmutableRect2i area;

	EntryWidget(IJeiConfigValue<T> value) {
		this.value = value;
	}

	public void updateBounds(ImmutableRect2i area) {
		this.area = area;
	}

	public T getValue() {
		return value.getValue();
	}

	public T getDefaultValue() {
		return value.getDefaultValue();
	}

	public Component getLocalizedName() {
		return value.getLocalizedName();
	}

	public Component getLocalizedDescription() {
		return value.getLocalizedDescription();
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return area.contains(mouseX, mouseY);
	}

	public abstract void draw(GuiGraphics guiGraphics, double mouseX, double mouseY);

	public final void drawTooltip(GuiGraphics guiGraphics, double mouseX, double mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			JeiTooltip tooltip = new JeiTooltip();
			tooltip.add(getLocalizedName().copy().withStyle(ChatFormatting.BOLD));
			tooltip.add(getLocalizedDescription());
		}
	}

	public void getTooltip(JeiTooltip tooltip) {

	}

	public abstract IUserInputHandler createInputHandler();

}
