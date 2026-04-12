package mezz.jei.gui.overlay.elements;

import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupElementOverlay implements IElementOverlay {

	private static final ChatFormatting[] GROUP_COLORS = {
			ChatFormatting.GOLD,
			ChatFormatting.AQUA,
			ChatFormatting.LIGHT_PURPLE,
			ChatFormatting.YELLOW,
			ChatFormatting.GREEN,
//			ChatFormatting.RED, red was used by edit mode
			ChatFormatting.BLUE,
			ChatFormatting.WHITE,
			ChatFormatting.DARK_AQUA,
			ChatFormatting.DARK_PURPLE,
			ChatFormatting.DARK_GREEN,
			ChatFormatting.DARK_RED,
			ChatFormatting.DARK_BLUE,
			ChatFormatting.GRAY,
			ChatFormatting.DARK_GRAY,
			//Dark colors don't look obvious in the UI
	};

	private final int borderColor;
	private final int fillColor;

	public GroupElementOverlay(int colorIndex) {
		Integer rgb = GROUP_COLORS[colorIndex % GROUP_COLORS.length].getColor();
		int color = rgb != null ? rgb : 0xFFFFFF;
		this.borderColor = (0xCC << 24) | color;
		this.fillColor = (0x30 << 24) | color;
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, List<ImmutableRect2i> slots) {
		if (slots.isEmpty()) {
			return;
		}

		int w = slots.getFirst().getWidth();
		int h = slots.getFirst().getHeight();

		Set<Long> occupied = new HashSet<>(slots.size() * 2);
		for (ImmutableRect2i slot : slots) {
			occupied.add(pack(slot.getX(), slot.getY()));
		}

		for (ImmutableRect2i slot : slots) {
			int x = slot.getX();
			int y = slot.getY();
			int r = x + w;
			int b = y + h;

			guiGraphics.fill(x, y, r, b, fillColor);

			boolean top = occupied.contains(pack(x, y - h));
			boolean bot = occupied.contains(pack(x, y + h));
			boolean left = occupied.contains(pack(x - w, y));
			boolean right = occupied.contains(pack(x + w, y));

			if (!top) {
				guiGraphics.fill(x, y, r, y + 1, borderColor);
			}
			if (!bot) {
				guiGraphics.fill(x, b - 1, r, b, borderColor);
			}
			if (!left) {
				guiGraphics.fill(x, y, x + 1, b, borderColor);
			}
			if (!right) {
				guiGraphics.fill(r - 1, y, r, b, borderColor);
			}

			if (top && left && !occupied.contains(pack(x - w, y - h))) {
				guiGraphics.fill(x, y, x + 1, y + 1, borderColor);
			}
			if (top && right && !occupied.contains(pack(x + w, y - h))) {
				guiGraphics.fill(r - 1, y, r, y + 1, borderColor);
			}
			if (bot && left && !occupied.contains(pack(x - w, y + h))) {
				guiGraphics.fill(x, b - 1, x + 1, b, borderColor);
			}
			if (bot && right && !occupied.contains(pack(x + w, y + h))) {
				guiGraphics.fill(r - 1, b - 1, r, b, borderColor);
			}
		}
	}

	private static long pack(int x, int y) {
		return ((long) x << 32) | (y & 0xFFFFFFFFL);
	}
}
