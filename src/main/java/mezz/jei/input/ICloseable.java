package mezz.jei.input;

public interface ICloseable {

	void open();
	void close();

	boolean isOpen();

}
