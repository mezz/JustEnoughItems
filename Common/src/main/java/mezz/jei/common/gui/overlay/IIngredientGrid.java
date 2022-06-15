package mezz.jei.common.gui.overlay;

import mezz.jei.common.input.IClickedIngredient;

import java.util.stream.Stream;

public interface IIngredientGrid {
    boolean isMouseOver(double mouseX, double mouseY);
    Stream<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY);
}
