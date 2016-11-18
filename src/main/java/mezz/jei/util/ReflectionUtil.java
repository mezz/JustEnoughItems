package mezz.jei.util;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class ReflectionUtil {

	private static Table<Class, Class, Optional<Field>> cache = HashBasedTable.create();

	@Nullable
	public static <T> T getFieldWithClass(final Object object, final Class<T> fieldClass) {
		Field field = getField(object, fieldClass);
		if (field != null) {
			try {
				Object fieldValue = field.get(object);
				if (fieldClass.isInstance(fieldValue)) {
					return fieldClass.cast(fieldValue);
				}
			} catch (IllegalAccessException ignored) {

			}
		}

		return null;
	}

	@Nullable
	private static Field getField(final Object object, final Class<?> fieldClass) {
		Class<?> objectClass = object.getClass();
		Optional<Field> cachedField = cache.get(fieldClass, objectClass);
		if (cachedField != null) {
			if (cachedField.isPresent()) {
				return cachedField.get();
			} else {
				return null;
			}
		}

		try {
			Field[] fields = objectClass.getDeclaredFields();
			for (Field field : fields){
				if (fieldClass.isAssignableFrom(field.getType())){
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					cache.put(fieldClass, objectClass, Optional.of(field));
					return field;
				}
			}
		} catch (SecurityException ignored) {

		}
		cache.put(fieldClass, objectClass, Optional.<Field>absent());
		return null;
	}

}
