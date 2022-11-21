package mezz.jei.common.util;


import java.util.Arrays;
import java.util.Collection;

public class MaximalRectangle {
    public static ImmutableRect2i getLargestRectangle(
        ImmutableRect2i area,
        Collection<ImmutableRect2i> exclusionAreas,
        ImmutableSize2i minSize,
        int samplingScale
    ) {
        MaximalRectangle maximalRectangle = new MaximalRectangle(area, exclusionAreas, minSize, samplingScale);
        return maximalRectangle.getLargestRectangle();
    }

    private final int xOffset;
    private final int yOffset;
    private final int samplingScale;
    private final int minCellRangeWidth;
    private final int minCellRangeHeight;
    private final CellState[][] inputMatrix;

    private enum CellState {
        OPEN, BLOCKED
    }

    private static class CellRange {
        private final int column;
        private final int row;
        private final int width;
        private final int height;

        public CellRange(int column, int row, int width, int height) {
            this.column = column;
            this.row = row;
            this.width = width;
            this.height = height;
        }

        public int getArea() {
            return width * height;
        }

        public ImmutableRect2i getOutput(int xOffset, int yOffset, int samplingScale) {
            return new ImmutableRect2i(
                xOffset + (column * samplingScale),
                yOffset + (row * samplingScale),
                width * samplingScale,
                height * samplingScale
            );
        }
    }

    private MaximalRectangle(
        ImmutableRect2i area,
        Collection<ImmutableRect2i> exclusionAreas,
        ImmutableSize2i minSize,
        int samplingScale
    ) {
        this.xOffset = area.getX();
        this.yOffset = area.getY();
        this.samplingScale = samplingScale;
        this.minCellRangeWidth = MathUtil.divideCeil(minSize.getWidth(), samplingScale);
        this.minCellRangeHeight = MathUtil.divideCeil(minSize.getHeight(), samplingScale);
        int rows = area.getHeight() / samplingScale;
        int columns = area.getWidth() / samplingScale;
        this.inputMatrix = new CellState[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                CellRange cellRange = new CellRange(column, row, 1, 1);
                ImmutableRect2i cell = cellRange.getOutput(xOffset, yOffset, samplingScale);
                boolean intersects = MathUtil.intersects(exclusionAreas, cell);
                this.inputMatrix[row][column] = intersects ? CellState.BLOCKED : CellState.OPEN;
            }
        }
    }

    private ImmutableRect2i getLargestRectangle() {
        int numRows = this.inputMatrix.length;
        if (numRows == 0) {
            return ImmutableRect2i.EMPTY;
        }
        int numColumns = this.inputMatrix[0].length;
        if (numColumns == 0) {
            return ImmutableRect2i.EMPTY;
        }

        CellRange max = new CellRange(0, 0, 0, 0);
        int maxArea = 0;

        int[] heights = new int[numColumns];
        int[] leftIndexes = new int[numColumns];
        int[] rightIndexes = new int[numColumns];
        Arrays.fill(rightIndexes, numColumns);

        for (int row = 0; row < numRows; row++) {
            int currentLeftIndex = 0;
            for (int column = 0; column < numColumns; column++) {
                if (this.inputMatrix[row][column] == CellState.OPEN) {
                    heights[column]++;
                    leftIndexes[column] = Math.max(leftIndexes[column], currentLeftIndex);
                } else {
                    heights[column] = 0;
                    leftIndexes[column] = 0;
                    currentLeftIndex = column + 1;
                }
            }

            int currentRightIndex = numColumns;
            for (int column = numColumns - 1; column >= 0; column--) {
                if (this.inputMatrix[row][column] == CellState.OPEN) {
                    rightIndexes[column] = Math.min(rightIndexes[column], currentRightIndex);
                } else {
                    rightIndexes[column] = numColumns;
                    currentRightIndex = column;
                }
            }

            for (int column = 0; column < numColumns; column++) {
                int rightIndex = rightIndexes[column];
                int leftIndex = leftIndexes[column];
                int width = (rightIndex - leftIndex);
                if (width < minCellRangeWidth) {
                    continue;
                }
                int height = heights[column];
                if (height < minCellRangeHeight) {
                    continue;
                }
                int area = width * height;
                if (maxArea < area) {
                    maxArea = area;
                    max = new CellRange(
                        leftIndex,
                        row - height + 1,
                        width,
                        height
                    );
                }
            }
        }
        return max.getOutput(xOffset, yOffset, samplingScale);
    }
}
