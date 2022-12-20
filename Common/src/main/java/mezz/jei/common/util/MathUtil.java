package mezz.jei.common.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.phys.Vec2;

import java.util.Collection;

public final class MathUtil {
	private MathUtil() {

	}

	@SuppressWarnings("NumericCastThatLosesPrecision")
	public static int divideCeil(int numerator, int denominator) {
		return (int) Math.ceil((float) numerator / denominator);
	}

	public static boolean intersects(Collection<ImmutableRect2i> areas, ImmutableRect2i comparisonArea) {
		return areas.stream()
			.anyMatch(comparisonArea::intersects);
	}

	public static boolean contains(Rect2i rect, double x, double y) {
		return x >= rect.getX() &&
			y >= rect.getY() &&
			x < rect.getX() + rect.getWidth() &&
			y < rect.getY() + rect.getHeight();
	}

	public static ImmutableRect2i union(ImmutableRect2i rect1, ImmutableRect2i rect2) {
		if (rect1.isEmpty()) {
			return rect2;
		}
		if (rect2.isEmpty()) {
			return rect1;
		}
		long tx2 = rect1.getWidth();
		long ty2 = rect1.getHeight();
		long rx2 = rect2.getWidth();
		long ry2 = rect2.getHeight();
		int tx1 = rect1.getX();
		int ty1 = rect1.getY();
		tx2 += tx1;
		ty2 += ty1;
		int rx1 = rect2.getX();
		int ry1 = rect2.getY();
		rx2 += rx1;
		ry2 += ry1;
        if (tx1 > rx1) {
            tx1 = rx1;
        }
        if (ty1 > ry1) {
            ty1 = ry1;
        }
        if (tx2 < rx2) {
            tx2 = rx2;
        }
        if (ty2 < ry2) {
            ty2 = ry2;
        }
		tx2 -= tx1;
		ty2 -= ty1;
		tx2 = Math.min(tx2, Integer.MAX_VALUE);
		ty2 = Math.min(ty2, Integer.MAX_VALUE);
		return new ImmutableRect2i(tx1, ty1, (int) tx2, (int) ty2);
	}

	public static ImmutableRect2i centerTextArea(ImmutableRect2i outer, Font fontRenderer, String text) {
		int width = fontRenderer.width(text);
		int height = fontRenderer.lineHeight;
		return centerArea(outer, width, height);
	}

	public static ImmutableRect2i centerTextArea(ImmutableRect2i outer, Font fontRenderer, FormattedText text) {
		int width = fontRenderer.width(text);
		int height = fontRenderer.lineHeight;
		return centerArea(outer, width, height);
	}

	public static ImmutableRect2i centerArea(ImmutableRect2i outer, int width, int height) {
		return new ImmutableRect2i(
			outer.getX() + Math.round((outer.getWidth() - width) / 2.0f),
			outer.getY() + Math.round((outer.getHeight() - height) / 2.0f),
			width,
			height
		);
	}

	public static double distance(Vec2 start, Vec2 end) {
		double a = start.x - end.x;
		double b = start.y - end.y;
		return Math.sqrt(a * a + b * b);
	}

}
