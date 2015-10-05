package mezz.jei.input;

public interface IClickable extends ICloseable {

	boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton);

}
