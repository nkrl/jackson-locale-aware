package nkrl.jackson.l10n;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

/**
 * Replaces the serializer for all beans marked with {@link JsonLocaleAware}.
 */
public class LocaleAwareBeanSerializerModifier extends BeanSerializerModifier {
    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDescription, JsonSerializer<?> serializer) {
        if (beanDescription.getClassAnnotations().has(JsonLocaleAware.class)) {
            return new LocaleAwareJacksonSerializer(new BeanSerializerBuilder(beanDescription));
        }
        else {
            return serializer;
        }
    }

    // TODO or override #changeProperties instead, which makes the custom serializer unnecessary
}
