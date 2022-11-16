package mezz.jei.api.runtime.util;

import javax.annotation.Nonnegative;

/**
 * @since 11.5.0
 */
public interface IImmutableRect2i {
    @Nonnegative
    int getX();

    @Nonnegative
    int getY();

    @Nonnegative
    int getWidth();

    @Nonnegative
    int getHeight();

    boolean isEmpty();

    boolean contains(double x, double y);
}
