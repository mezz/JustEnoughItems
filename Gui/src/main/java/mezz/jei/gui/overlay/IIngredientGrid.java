package mezz.jei.gui.overlay;

import mezz.jei.gui.input.IRecipeFocusSource;

public interface IIngredientGrid extends IRecipeFocusSource {
    boolean isMouseOver(double mouseX, double mouseY);
}
