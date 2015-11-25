package mezz.jei.input;

public interface IMouseHandler extends ICloseable {

	boolean isMouseOver(int mouseX, int mouseY);

	boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton);

	boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta);

}
