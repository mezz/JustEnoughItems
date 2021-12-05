package mezz.jei.input.mouse.handlers;

import mezz.jei.input.mouse.IUserInputHandler;

public class NullUserInputHandler implements IUserInputHandler {
	public static final NullUserInputHandler INSTANCE = new NullUserInputHandler();

	private NullUserInputHandler() {

	}
}
