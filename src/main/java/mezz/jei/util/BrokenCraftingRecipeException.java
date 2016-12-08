package mezz.jei.util;

public class BrokenCraftingRecipeException extends RuntimeException {
	public BrokenCraftingRecipeException(String message, Throwable cause) {
		super(message, cause);
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}
}
