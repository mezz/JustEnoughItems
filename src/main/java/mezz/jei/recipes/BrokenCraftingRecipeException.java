package mezz.jei.recipes;

public class BrokenCraftingRecipeException extends RuntimeException {
	public BrokenCraftingRecipeException(String message, Throwable cause) {
		super(message, cause);
	}
}
