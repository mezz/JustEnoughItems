package mezz.jei.gui.overlay;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnegative;

public final class Dimensions {
    @Nonnegative
    public final int rows;
    @Nonnegative
    public final int columns;

    @SuppressWarnings("ConstantConditions")
    public Dimensions(@Nonnegative int rows, @Nonnegative int columns) {
        Preconditions.checkArgument(rows >= 0, "rows must be greater or equal 0");
        Preconditions.checkArgument(columns >= 0, "columns must be greater or equal 0");
        this.rows = rows;
        this.columns = columns;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Dimensions other) {
            return (rows == other.rows) && (columns == other.columns);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + rows;
        hash = hash * 31 + columns;
        return hash;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("rows", rows)
            .add("columns", columns)
            .toString();
    }
}
