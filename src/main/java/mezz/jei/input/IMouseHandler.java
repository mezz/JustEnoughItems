package mezz.jei.input;

public interface IMouseHandler {

	boolean isMouseOver(double mouseX, double mouseY);

	boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton);

	boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta);

}
