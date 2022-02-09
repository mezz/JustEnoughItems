package mezz.jei;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import mezz.jei.color.ColorNamer;
import mezz.jei.events.EventBusHelper;
import mezz.jei.gui.GuiEventHandler;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.input.InputEventHandler;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.startup.JeiReloadListener;

/**
 * For JEI internal use only, these are normally accessed from the API.
 */
public final class Internal {
	@Nullable
	private static JeiHelpers helpers;
	@Nullable
	private static JeiRuntime runtime;
	@Nullable
	private static IngredientManager ingredientManager;
	@Nullable
	private static ColorNamer colorNamer;
	@Nullable
	private static IngredientFilter ingredientFilter;
	@Nullable
	private static GuiEventHandler guiEventHandler;
	@Nullable
	private static InputEventHandler inputEventHandler;
	@Nullable
	private static Textures textures;
	@Nullable
	private static JeiReloadListener reloadListener;

	private Internal() {

	}

	public static JeiHelpers getHelpers() {
		Preconditions.checkState(helpers != null, "JeiHelpers has not been created yet.");
		return helpers;
	}

	public static void setHelpers(JeiHelpers helpers) {
		Internal.helpers = helpers;
	}

	public static Textures getTextures() {
		Preconditions.checkState(textures != null, "Textures has not been created yet.");
		return textures;
	}

	public static void setTextures(Textures textures) {
		Internal.textures = textures;
	}

	@Nullable
	public static JeiRuntime getRuntime() {
		return runtime;
	}

	public static void setRuntime(JeiRuntime runtime) {
		JeiRuntime jeiRuntime = Internal.runtime;
		if (jeiRuntime != null) {
			jeiRuntime.close();
		}
		Internal.runtime = runtime;
	}

	public static IngredientManager getIngredientManager() {
		Preconditions.checkState(ingredientManager != null, "Ingredient Manager has not been created yet.");
		return ingredientManager;
	}

	public static void setIngredientManager(IngredientManager ingredientManager) {
		Internal.ingredientManager = ingredientManager;
	}

	public static ColorNamer getColorNamer() {
		Preconditions.checkState(colorNamer != null, "Color Namer has not been created yet.");
		return colorNamer;
	}

	public static void setColorNamer(ColorNamer colorNamer) {
		Internal.colorNamer = colorNamer;
	}

	public static IngredientFilter getIngredientFilter() {
		Preconditions.checkState(ingredientFilter != null, "Ingredient Filter has not been created yet.");
		return ingredientFilter;
	}

	public static void setIngredientFilter(IngredientFilter ingredientFilter) {
		if (Internal.ingredientFilter != null) {
			EventBusHelper.unregister(Internal.ingredientFilter);
		}
		Internal.ingredientFilter = ingredientFilter;
		EventBusHelper.register(ingredientFilter);
	}

	public static void setGuiEventHandler(GuiEventHandler guiEventHandler) {
		if (Internal.guiEventHandler != null) {
			EventBusHelper.unregister(Internal.guiEventHandler);
		}

		Internal.guiEventHandler = guiEventHandler;
		guiEventHandler.registerToEventBus();
	}

	public static void setInputEventHandler(InputEventHandler inputEventHandler) {
		if (Internal.inputEventHandler != null) {
			EventBusHelper.unregister(Internal.inputEventHandler);
		}

		Internal.inputEventHandler = inputEventHandler;
		inputEventHandler.registerToEventBus();
	}

	@Nullable
	public static JeiReloadListener getReloadListener() {
		return reloadListener;
	}

	public static void setReloadListener(JeiReloadListener listener) {
		Preconditions.checkState(reloadListener == null, "Reload Listener has already been assigned.");
		reloadListener = listener;
	}
}
