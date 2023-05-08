package mezz.jei.api.registration;

import mezz.jei.api.IModPlugin;

import java.util.function.Consumer;

/**
 * Advanced API to provide a stand-between when JEI is calling methods on the registered plugins.
 * Only one registration delegate may exist. By default, it simply calls the method with no further
 * logic.
 */
public interface JeiRegistrationDelegate {

	static void set(JeiRegistrationDelegate delegate) {
		JeiRegistrationDelegateHolder.current = delegate;
	}

	static JeiRegistrationDelegate get() {
		return JeiRegistrationDelegateHolder.current;
	}

	void callOnPlugin(JeiRegistrationStep step, IModPlugin plugin, Consumer<IModPlugin> func);

}

// Can't have a private in an interface, including an inner class. So this will do.
class JeiRegistrationDelegateHolder {
	static JeiRegistrationDelegate current = (step, plugin, func) -> func.accept(plugin);
}
