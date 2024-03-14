package nkrl.jackson.l10n;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.SimpleBeanPropertyDefinition;

import java.lang.reflect.Method;

/**
 * Writes a single localized property backed by a method accepting a locale as a parameter to the serialized output.
 */
public class LocaleAwareMethodPropertyWriter extends VirtualBeanPropertyWriter {

    private Method method;

    public LocaleAwareMethodPropertyWriter(AnnotatedMethod method, MapperConfig<?> config) { // TODO name param?
        super(SimpleBeanPropertyDefinition.construct(config, method), method.getAllAnnotations(), method.getType());
        this.method = method.getAnnotated();
    }

    protected LocaleAwareMethodPropertyWriter(BeanPropertyDefinition propDef, Annotations annotations, JavaType type) {
        super(propDef, annotations, type);
    }

    @Override
    protected Object value(Object bean, JsonGenerator gen, SerializerProvider provider) throws Exception {
        // TODO we probably have to call json.writeFieldName first
        return method.invoke(bean, provider.getLocale());
    }

    @Override
    public String getName() {
        // TODO override?
        return super.getName();
    }

    @Override
    public VirtualBeanPropertyWriter withConfig(MapperConfig<?> config, AnnotatedClass declaringClass, BeanPropertyDefinition propDef, JavaType type) {
        // this method should not be called on this property writer as we are instantiating it ourselves from serializer rather than via annotation and parameterless constructor, but it needs a dummy implementation because it is abstract
        return new LocaleAwareMethodPropertyWriter(propDef, declaringClass.getAnnotations(), type);
    }
}
