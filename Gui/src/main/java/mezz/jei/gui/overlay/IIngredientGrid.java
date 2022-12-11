package mezz.jei.gui.overlay;

import mezz.jei.api.runtime.IClickedIngredient;

import java.util.stream.Stream;

public interface IIngredientGrid {
    boolean isMouseOver(double mouseX, double mouseY);
    Stream<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY);
}
