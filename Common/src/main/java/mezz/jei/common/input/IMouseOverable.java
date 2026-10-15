package mezz.jei.common.input;

/** Hit testing for JEI input areas, independent of Minecraft's GUI event methods. */
@FunctionalInterface
public interface IMouseOverable {
	boolean containsMouse(double mouseX, double mouseY);
}
