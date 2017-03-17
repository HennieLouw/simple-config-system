# Simple Application Configuration Library

## Introduction

A Java framework to read and update an configuration entries which is being used in your 
application with support for many different 'sources' where the configuration values can 
come from with ability to update the internal setup of the framework during runtime. 

Generic implementations of 'common' types of sources are also provided for ease of use.

The framework has been specifically coded to support Java version from 5 upwards for use in 
enterprise systems where the version of Java may be restricted to older versions.

## Maven Module Structure.

The Framework is broken down into three main maven dependencies.
 - scs-parent - The parent pom.
 - scs-api - The base framework and minimal implementations.
 - scs-impl - Various implementations for ease of use.
 
### SCS-PARENT
Maven POM to build the project from source.
 
### SCS-API
Maven JAR Artifact which provides the base framework for configuration instances including a
Thread safe memory base implementation.

#### Usage
The main class into the API is the ```com.github.scs.Configuration``` which provides a builder 
pattern for creating configuration instances and defining sources.

```java
import com.github.scs.Configuration;
import com.github.scs.impl.MemoryConfigurationSource;

public class ConfigurationBuilderUsage {

    public static void main(String[] args) {
        MemoryConfigurationSource memorySource = new MemoryConfigurationSource();
        
        Configuration.ConfigurationBuilder configurationBuilder = Configuration.builder();
        Configuration configuration = configurationBuilder.source(memorySource)
                                                          .build();

        configuration.store("some-string-key", "value-of-some-key");
        System.out.println(configuration.retrieve("some-string-key"));

        configuration.storeBoolean("some-boolean-key", Boolean.TRUE);
        System.out.println(configuration.retrieveBoolean("some-boolean-key"));

        configuration.storeInteger("some-int-key", 22);
        System.out.println(configuration.retrieveInteger("some-int-key"));
    }
}
```

#### Maven Dependency
```xml
<dependency>
    <groupId>com.github.scs</groupId>
    <artifactId>scs-api</artifactId>
    <version>1.0</version>
</dependency>
```

### SCS-IMPL
TODO