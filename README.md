# Locale-aware Jackson

A module for the JSON serializer [Jackson](https://github.com/FasterXML/jackson-databind) that lets users add custom localized properties to the serialized output.

## Installation

### Using Maven

This package is currently publishing its releases to the GitHub package registry. Therefore, you have to reference it
in your _pom.xml_. Access to the registry requires authentication with a GitHub personal access token. 
Refer to the [GitHub documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-to-github-packages) to learn how to add a personal access token to Maven.

In your _pom.xml_, you need to depend on this artifact as well as Jackson:
```xml
<project>
    <repositories>
        <repository>
            <id>github</id>
            <url>https://maven.pkg.github.com/nkrl/jackson-locale-aware</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>io.github.nkrl</groupId>
            <artifactId>jackson-locale-aware</artifactId>
            <version>1.0.0</version>
        </dependency>
        
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.1</version>
        </dependency>
    </dependencies>
</project>
```

## Usage

To add locale-specific JSON properties, mark the class with the annotation `@JsonLocaleAware` and 
add Jackson's `@JsonGetter` or `@JsonProperty` annotations to a method that accepts the locale as a
parameter and returns the localized property value.

Following the naming convention for Java getters, the
`@JsonGetter` annotation will remove the `get` prefix from the automatically generated property name.
Customizing the property name is supported by setting the `value` element of the annotation.


```java
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import nkrl.jackson.l10n.append.JsonLocaleAware;

import java.util.Locale;

@JsonLocaleAware
public class Post {

    private String author;

    @JsonGetter
    public String getTitle(Locale locale) {
        // stub
    }

    @JsonProperty("content")
    public String translatedContent(Locale locale) {
        // stub
    }

    @JsonProperty
    public int views(Locale locale) {
        // stub
    }
}
```

When serializing objects of this class using Jackson as usual, the output will now include the localized properties `title`, `content` and `views` in the system default locale in addition to the unlocalized property `author`. 
To customize the locale, use `ObjectMapper.setLocale(Locale)`.

```java
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Locale;

public class JsonExample {
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setLocale(Locale.GERMANY);
        System.out.println(mapper.writeValueAsString(new Post()));
    }
}
```
