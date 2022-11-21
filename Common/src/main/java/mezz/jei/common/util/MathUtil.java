package mezz.jei.common.util;

import mezz.jei.api.runtime.util.IImmutableRect2i;
import mezz.jei.common.gui.overlay.options.HorizontalAlignment;
import mezz.jei.common.gui.overlay.options.VerticalAlignment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.phys.Vec2;

import javax.annotation.Nonnegative;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class MathUtil {
	@FunctionalInterface
	private interface Rectangle2dCropper {
		ImmutableRect2i crop(ImmutableRect2i original, ImmutableRect2i intersecting);
	}

	private static final List<Rectangle2dCropper> rectangle2dCroppers = List.of(
		MathUtil::cropTop,
		MathUtil::cropBottom,
		MathUtil::cropLeft,
		MathUtil::cropRight
	);

	private MathUtil() {

	}

	@SuppressWarnings("NumericCastThatLosesPrecision")
	public static int divideCeil(int numerator, int denominator) {
		return (int) Math.ceil((float) numerator / denominator);
	}

	public static boolean intersects(Collection<ImmutableRect2i> areas, ImmutableRect2i comparisonArea) {
		for (ImmutableRect2i area : areas) {
			if (intersects(area, comparisonArea)) {
				return true;
			}
		}
		return false;
	}

	public static boolean intersects(ImmutableRect2i rect1, ImmutableRect2i rect2) {
		if (rect1.isEmpty() || rect2.isEmpty()) {
			return false;
		}
		return rect2.getX() + rect2.getWidth() > rect1.getX() &&
			rect2.getY() + rect2.getHeight() > rect1.getY() &&
			rect2.getX() < rect1.getX() + rect1.getWidth() &&
			rect2.getY() < rect1.getY() + rect1.getHeight();
	}

	public static int intersectingArea(Collection<ImmutableRect2i> areas, ImmutableRect2i comparisonArea) {
		return areas.stream()
			.mapToInt(area -> intersection(area, comparisonArea).getArea())
			.sum();
	}

	public static ImmutableRect2i intersection(ImmutableRect2i rect1, ImmutableRect2i rect2) {
		final int x = Math.max(rect1.getX(), rect2.getX());
		final int maxX = Math.min(rect1.getX() + rect1.getWidth(), rect2.getX() + rect2.getWidth());
		final int y = Math.max(rect1.getY(), rect2.getY());
		final int maxY = Math.min(rect1.getY() + rect1.getHeight(), rect2.getY() + rect2.getHeight());
		if (maxX >= x && maxY >= y) {
			return new ImmutableRect2i(x, y, maxX - x, maxY - y);
		} else {
			return ImmutableRect2i.EMPTY;
		}
	}

	/**
	 * Crop the given "rect" to avoid "intersecting" while maximizing the available content space.
	 */
	private static ImmutableRect2i bestCrop(
		ImmutableRect2i rect,
		ImmutableRect2i intersecting,
		Collection<ImmutableRect2i> allIntersecting,
		int maxWidth,
		int maxHeight
	) {
		if (rect.isEmpty() || maxHeight == 0 || maxWidth == 0 || !intersects(rect, intersecting)) {
			return rect;
		}
		return rectangle2dCroppers.stream()
			.map(cropper -> cropper.crop(rect, intersecting))
			.filter(cropped -> !intersects(allIntersecting, cropped))
			.max(Comparator.comparingInt(r -> contentArea(r, maxWidth, maxHeight)))
			.orElse(rect);
	}

	private static Stream<ImmutableRect2i> crops(ImmutableRect2i rect, ImmutableRect2i intersecting) {
		if (!intersects(rect, intersecting)) {
			return Stream.of(rect);
		}
		return rectangle2dCroppers.stream()
			.map(cropper -> cropper.crop(rect, intersecting));
	}

	/**
	 * Calculates the area of flexible content that can fit in a given rect.
	 */
	@Nonnegative
	public static int contentArea(ImmutableRect2i rect, int maxWidth, int maxHeight) {
		return Math.min(rect.getWidth(), maxWidth) * Math.min(rect.getHeight(), maxHeight);
	}

	private static ImmutableRect2i cropTop(ImmutableRect2i original, ImmutableRect2i intersecting) {
		int newY = intersecting.getY() + intersecting.getHeight();
		int cropTopAmount = newY - original.getY();
		if (cropTopAmount < 0) {
			return original;
		}
		return original.cropTop(cropTopAmount);
	}

	private static ImmutableRect2i cropLeft(ImmutableRect2i original, ImmutableRect2i intersecting) {
		int newX = intersecting.getX() + intersecting.getWidth();
		int cropLeftAmount = newX - original.getX();
		if (cropLeftAmount < 0) {
			return original;
		}
		return original.cropLeft(cropLeftAmount);
	}

	private static ImmutableRect2i cropBottom(ImmutableRect2i original, ImmutableRect2i intersecting) {
		int newHeight = intersecting.getY() - original.getY();
		if (newHeight < 0) {
			return ImmutableRect2i.EMPTY;
		}
		return original.keepTop(newHeight);
	}

	private static ImmutableRect2i cropRight(ImmutableRect2i original, ImmutableRect2i intersecting) {
		int newWidth = intersecting.getX() - original.getX();
		if (newWidth < 0) {
			return ImmutableRect2i.EMPTY;
		}
		return original.keepLeft(newWidth);
	}

	public static boolean contains(Collection<ImmutableRect2i> areas, double x, double y) {
		for (ImmutableRect2i guiArea : areas) {
			if (guiArea.contains(x, y)) {
				return true;
			}
		}
		return false;
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

	public static ImmutableRect2i centerTextArea(IImmutableRect2i outer, Font fontRenderer, String text) {
		int width = fontRenderer.width(text);
		int height = fontRenderer.lineHeight;
		return centerArea(outer, width, height);
	}

	public static ImmutableRect2i centerTextArea(IImmutableRect2i outer, Font fontRenderer, FormattedText text) {
		int width = fontRenderer.width(text);
		int height = fontRenderer.lineHeight;
		return centerArea(outer, width, height);
	}

	public static ImmutableRect2i centerArea(IImmutableRect2i outer, int width, int height) {
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

	public static ImmutableRect2i align(ImmutableSize2i size, IImmutableRect2i availableArea, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		final int width = size.getWidth();
		final int x = switch (horizontalAlignment) {
			case LEFT -> availableArea.getX();
			case CENTER -> availableArea.getX() + ((availableArea.getWidth() - width) / 2);
			case RIGHT -> availableArea.getX() + (availableArea.getWidth() - width);
		};

		final int height = size.getHeight();
		final int y = switch (verticalAlignment) {
			case TOP -> availableArea.getY();
			case CENTER -> availableArea.getY() + ((availableArea.getHeight() - height) / 2);
			case BOTTOM -> availableArea.getY() + (availableArea.getHeight() - height);
		};

		return new ImmutableRect2i(x, y, width, height);
	}
}
