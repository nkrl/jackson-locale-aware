package nkrl.jackson.l10n;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;

public class JacksonLocaleAwareModule extends Module {
    @Override
    public String getModuleName() {
        return "locale-aware";
    }

    @Override
    public Version version() {
        return new Version(2, 0, 0, null, "io.github.nkrl", "jackson-locale-aware");
    }

    @Override
    public void setupModule(SetupContext context) {
        //context.addSerializers(new SimpleSerializers().addSerializer(new LocaleAwareJacksonSerializer()));
        context.addBeanSerializerModifier(new LocaleAwareBeanSerializerModifier());
    }
}
