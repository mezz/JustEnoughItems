package mezz.jei.api.registration;

/**
 * The order of values in this enum is not guaranteed. Use {@code ordinal()} with caution, if ever.
 * @see JeiRegistrationDelegate
 */
public enum JeiRegistrationStep {
	ITEM_SUBTYPES("Registering item subtypes"),
	FLUID_SUBTYPES("Registering fluid subtypes"),
	INGREDIENTS("Registering ingredients"),
	CATEGORIES("Registering categories"),
	VANILLA_CATEGORY_EXTENSIONS("Registering vanilla category extensions"),
	RECIPES("Registering recipes"),
	RECIPE_TRANSFER_HANDLERS("Registering recipes transfer handlers"),
	RECIPE_CATALYSTS("Registering recipe catalysts"),
	GUI_HANDLERS("Registering gui handlers"),
	ADVANCED("Registering advanced plugins"),
	RUNTIME("Registering Runtime"),
	RUNTIME_AVAILABLE("Sending Runtime"),
	RUNTIME_UNAVAILABLE("Sending Runtime Unavailable"),
	CONFIG_MANAGER("Sending ConfigManager");

	private final String description;

	JeiRegistrationStep(String description) {
		this.description = description;
	}

	public String toString() {
		return description;
	}

}
