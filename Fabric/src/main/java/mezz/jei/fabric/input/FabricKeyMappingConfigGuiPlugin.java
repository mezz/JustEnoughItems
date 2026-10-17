package mezz.jei.fabric.input;

import mezz.jei.gui.config.JeiConfigGuiPlugin;
import net.mezzdev.config.gui.api.IConfigGuiRegistration;
import net.mezzdev.config.gui.api.keybindings.IKeyMappingAdapter;
import net.mezzdev.config.gui.api.keybindings.IKeyMappingBinding;
import net.mezzdev.config.gui.api.keybindings.IKeyMappingHelper;
import net.mezzdev.config.gui.api.keybindings.IKeyMappingInput;
import net.mezzdev.config.gui.api.keybindings.KeyInputModifier;
import net.mezzdev.config.gui.api.keybindings.KeyMappingModifier;

import java.util.Set;
import java.util.stream.Collectors;

/** Registers JEI's standalone Fabric bindings with the optional config GUI. */
public class FabricKeyMappingConfigGuiPlugin extends JeiConfigGuiPlugin {
	@Override
	public void register(IConfigGuiRegistration registration) {
		super.register(registration);
		IKeyMappingHelper helper = registration.getKeyMappingHelper();
		registration.registerKeyMappingAdapter(FabricKeyMapping.class, mapping -> new Adapter(mapping, helper));
	}

	private static final class Adapter implements IKeyMappingAdapter {
		private final FabricKeyMapping mapping;
		private final IKeyMappingHelper helper;

		private Adapter(FabricKeyMapping mapping, IKeyMappingHelper helper) {
			this.mapping = mapping;
			this.helper = helper;
		}

		@Override
		public FabricKeyMapping getKeyMapping() {
			return mapping;
		}

		@Override
		public IKeyMappingBinding getValue() {
			return helper.createBinding(mapping.getBoundKey(), toBindingModifiers(mapping.getModifiers()));
		}

		@Override
		public IKeyMappingBinding getDefaultValue() {
			return helper.createBinding(mapping.getDefaultKey(), toBindingModifiers(mapping.getDefaultModifiers()));
		}

		@Override
		public void setValue(IKeyMappingBinding value) {
			IKeyMappingInput input = value.resolve();
			mapping.setKeyAndModifiers(input.getKey(), fromInputModifiers(input.getModifiers()));
		}

		@Override
		public IKeyMappingBinding createBinding(IKeyMappingInput input) {
			return input.withCustomModifiers(Set.of()).toBinding();
		}

		private static Set<KeyMappingModifier> toBindingModifiers(Set<FabricKeyModifier> modifiers) {
			return modifiers.stream().map(modifier -> switch (modifier) {
					case SHIFT -> KeyMappingModifier.SHIFT;
					case CONTROL -> KeyMappingModifier.CONTROL;
					case ALT -> KeyMappingModifier.ALT;
					case COMMAND -> KeyMappingModifier.COMMAND;
				})
				.collect(Collectors.toUnmodifiableSet());
		}

		private static Set<FabricKeyModifier> fromInputModifiers(Set<KeyInputModifier> modifiers) {
			return modifiers.stream().map(modifier -> switch (modifier) {
					case SHIFT -> FabricKeyModifier.SHIFT;
					case CONTROL -> FabricKeyModifier.CONTROL;
					case ALT -> FabricKeyModifier.ALT;
					case COMMAND -> FabricKeyModifier.COMMAND;
				})
				.collect(Collectors.toUnmodifiableSet());
		}
	}
}
