package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.Objects;
import java.util.function.Supplier;

interface RecipeButtonContents {
	void draw(GuiGraphics graphics, int width, int height, boolean active, boolean hovered);

	default void addTooltip(ITooltipBuilder tooltip, int width) {
	}

	record Text(Supplier<Component> message) implements RecipeButtonContents {
		@Override
		public void draw(GuiGraphics graphics, int width, int height, boolean active, boolean hovered) {
			var font = Minecraft.getInstance().font;
			Component message = Objects.requireNonNull(this.message.get(), "message result");
			FormattedText text = message;
			if (font.width(message) > width - 4) {
				text = StringUtil.truncateStringToWidth(message, width - 4, font);
			}
			var label = Language.getInstance().getVisualOrder(text);
			int color;
			if (!active) {
				color = 0xFFA0A0A0;
			} else if (hovered) {
				color = 0xFFFFFFFF;
			} else {
				color = 0xFFE0E0E0;
			}
			graphics.drawString(font, label, (width - font.width(label)) / 2, (height - font.lineHeight) / 2 + 1, color, true);
		}

		@Override
		public void addTooltip(ITooltipBuilder tooltip, int width) {
			Component message = Objects.requireNonNull(this.message.get(), "message result");
			if (Minecraft.getInstance().font.width(message) > width - 4) {
				tooltip.add(message);
			}
		}
	}

	record Icon(Supplier<IDrawable> icon) implements RecipeButtonContents {
		@Override
		public void draw(GuiGraphics graphics, int width, int height, boolean active, boolean hovered) {
			IDrawable icon = Objects.requireNonNull(this.icon.get(), "icon result");
			var pose = graphics.pose();
			pose.pushMatrix();
			try {
				pose.translate((width - icon.getWidth()) / 2.0f, (height - icon.getHeight()) / 2.0f);
				icon.draw(graphics);
			} finally {
				pose.popMatrix();
			}
		}
	}
}
