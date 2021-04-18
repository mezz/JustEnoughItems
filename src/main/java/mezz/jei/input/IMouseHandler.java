package mezz.jei.input;

public interface IMouseHandler {

	boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton, boolean doClick);

	default boolean handleMouseDragStart(double mouseX, double mouseY, int mouseButton) {
		return false;
	}

	default boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		return false;
	}

}
