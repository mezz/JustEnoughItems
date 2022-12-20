package mezz.jei.common.util;

import javax.annotation.Nonnegative;

public class ImmutableSize2i {
    public static final ImmutableSize2i EMPTY = new ImmutableSize2i(0, 0);

    @Nonnegative
    private final int width;
    @Nonnegative
    private final int height;

    public ImmutableSize2i(@Nonnegative int width, @Nonnegative int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getArea() {
        return width * height;
    }
}
