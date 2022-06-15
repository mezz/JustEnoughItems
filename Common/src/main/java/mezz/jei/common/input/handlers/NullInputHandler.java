package mezz.jei.common.input.handlers;

import mezz.jei.common.input.IUserInputHandler;

public class NullInputHandler implements IUserInputHandler {
	public static final NullInputHandler INSTANCE = new NullInputHandler();

	private NullInputHandler() {

	}
}
