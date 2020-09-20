package mezz.jei.input;

public interface IMouseHandler {

	boolean isMouseOver(double mouseX, double mouseY);

	boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton);

	default boolean handleMouseDragStart(double mouseX, double mouseY, int mouseButton) {
		return false;
	}

	default boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		return false;
	}

}
