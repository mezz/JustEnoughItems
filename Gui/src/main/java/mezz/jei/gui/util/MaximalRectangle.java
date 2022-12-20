package mezz.jei.gui.util;

import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MaximalRectangle {
    public static Stream<ImmutableRect2i> getLargestRectangles(
        ImmutableRect2i area,
        Collection<ImmutableRect2i> exclusionAreas,
        int samplingScale
    ) {
        exclusionAreas = exclusionAreas.stream()
            .filter(area::intersects)
            .collect(Collectors.toUnmodifiableSet());

        if (exclusionAreas.isEmpty()) {
            return Stream.of(area);
        }

        MaximalRectangle maximalRectangle = new MaximalRectangle(area, samplingScale, exclusionAreas);
        return maximalRectangle.getLargestRectangles();
    }

    private final ImmutableRect2i area;
    private final int rows;
    private final int columns;
    private final int samplingScale;
    private final boolean[][] blockedAreas;

    private MaximalRectangle(ImmutableRect2i area, int samplingScale, Collection<ImmutableRect2i> exclusionAreas) {
        this.area = area;
        this.samplingScale = samplingScale;

        this.rows = area.getHeight() / samplingScale;
        this.columns = area.getWidth() / samplingScale;
        this.blockedAreas = new boolean[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                ImmutableRect2i rect = getRect(row, column, 1, 1);
                boolean intersects = MathUtil.intersects(exclusionAreas, rect);
                blockedAreas[row][column] = intersects;
            }
        }
    }

    private ImmutableRect2i getRect(int row, int column, int width, int height) {
        if (width == 0 || height == 0) {
            return ImmutableRect2i.EMPTY;
        }
        return new ImmutableRect2i(
            area.getX() + (column * samplingScale),
            area.getY() + (row * samplingScale),
            width * samplingScale,
            height * samplingScale
        );
    }

    private Stream<ImmutableRect2i> getLargestRectangles() {
        if (rows == 0 || columns == 0) {
            return Stream.empty();
        }

        int[] heights = new int[columns];
        int[] leftIndexes = new int[columns];
        int[] rightIndexes = new int[columns];
        Arrays.fill(rightIndexes, columns);

        return IntStream.range(0, rows)
            .boxed()
            .flatMap(row -> {
                int currentLeftIndex = 0;
                for (int column = 0; column < columns; column++) {
                    if (blockedAreas[row][column]) {
                        heights[column] = 0;
                        leftIndexes[column] = 0;
                        currentLeftIndex = column + 1;
                    } else {
                        heights[column]++;
                        leftIndexes[column] = Math.max(leftIndexes[column], currentLeftIndex);
                    }
                }

                int currentRightIndex = columns;
                for (int column = columns - 1; column >= 0; column--) {
                    if (blockedAreas[row][column]) {
                        rightIndexes[column] = columns;
                        currentRightIndex = column;
                    } else {
                        rightIndexes[column] = Math.min(rightIndexes[column], currentRightIndex);
                    }
                }

                return IntStream.range(0, columns)
                    .mapToObj(column -> {
                        int rightIndex = rightIndexes[column];
                        int leftIndex = leftIndexes[column];
                        int width = (rightIndex - leftIndex);
                        int height = heights[column];

                        return getRect(row - height + 1, leftIndex, width, height);
                    })
                    .filter(r -> !r.isEmpty());
            });
    }
}
