# Simple Application Configuration Library

## Introduction
 A Java framework to read and update configuration entries which is being used in your application with support for 
 many different 'sources' where the configuration values can come from with ability to update the internal setup of 
 the framework during runtime. 

 Generic implementations of 'common' types of sources are also provided for ease of use.

 The framework has been specifically coded to support Java version from 5 upwards for use in 
 enterprise systems where the version of Java may be restricted to older versions.

## Maven Module Structure.
 The Framework is broken down into three main maven dependencies.
  - scs-parent - The parent pom.
  - scs-api - The base framework and minimal implementations.
  - scs-impl - Meta dependency to include all sub implementation modules, such as caching, database, text expansion, etc
 
### SCS-PARENT
 Maven POM to build the project from source.
 
### SCS-API
 Maven JAR Artifact which provides the base framework for configuration instances including a ```Thread``` safe memory 
 base implementation.

##### Usage
The main class into the API is the ```com.github.scs.Configuration``` which provides a builder 
pattern for creating configuration instances and defining sources.

```java
import com.github.scs.Configuration;
import com.github.scs.impl.MemoryConfigurationSource;

public class ConfigurationBuilderUsage {
    public static void main(String[] args) {
        MemoryConfigurationSource memorySource = new MemoryConfigurationSource();
        
        Configuration.ConfigurationBuilder configurationBuilder = Configuration.builder();
        Configuration configuration = configurationBuilder.source(memorySource).build();

        configuration.store("some-string-key", "value-of-some-key");
        System.out.println(configuration.retrieve("some-string-key"));

        configuration.storeBoolean("some-boolean-key", Boolean.TRUE);
        System.out.println(configuration.retrieveBoolean("some-boolean-key"));

        configuration.storeInteger("some-int-key", 22);
        System.out.println(configuration.retrieveInteger("some-int-key"));
    }
}
```

Additionally, a Global ```Configuration``` instance is provided to the system via the 
```com.github.scs.GlobalConfiguration``` object which can be configured as per the below example.

```java
public class ConfigurationBuilderUsage {
    public static void main(String[] args) {
        ConfigurationSource propertiesSource = new PropertyFileConfigurationSource("application.properties");
        ConfigurationSource dataBaseSource = new DataBaseSource("DB_TABLE_NAME");
        
        ConfigurationDefaults defaults = ConfigurationDefaults.buildCommon();
        GlobalConfiguration.configure(defaults, propertiesSource, dataBaseSource );
        
        String myConfigEntry = GlobalConfiguration.instance().retrieve("SomeKey", "DefaulValueIfNotFound");
    }
}
```

 ##### Maven Dependency
 ```xml
 <dependency>
     <groupId>com.github.scs</groupId>
     <artifactId>scs-api</artifactId>
     <version>${scs.version}</version>
 </dependency>
 ```

### SCS-IMPL

 ##### Maven Dependency
 ```xml
 <dependency>
     <groupId>com.github.scs</groupId>
     <artifactId>scs-impl</artifactId>
     <version>${scs.version}</version>
 </dependency>
 ```

### SCS-IMPL-CACHING 
 Maven module which provides caching implementations of the ```com.github.scs.api.ConfigurationSource```  interface.

 The following implementations are provided at the moment

 - ```com.github.scs.impl.GuavaCacheConfigurationSource``` Implementation of a ```ConfigurationSource``` which supports
 caching of the values retrieved by an underlying source by using the ```LoadingCache``` provided by Google's Guava 
 library
 
 - ```com.github.scs.impl.EHCachedConfigurationSource``` Implementation of a ```ConfigurationSource``` which first will 
 delegate the retrieve operation to the configured underlying source and then cache the result of this delegation based 
 on the caching configuration. This implementation supports both programmatic as well as external EHCache configurations
 
 - ```com.github.scs.impl.EHMemoryCachedConfigurationSource``` - Simpler API version of the 
 ```EHCachedConfigurationSource``` which configures the cache to use memory only with some sane defaults for max 
 entries, time-to-live and time-to-idle. For more complex caching requirements it is adivsed to use the more generic
 ```EHCachedConfigurationSource```
 
 ##### Maven Dependency
 ```xml
 <dependency>
     <groupId>com.github.scs</groupId>
     <artifactId>scs-impl-caching</artifactId>
     <version>${scs.version}</version>
 </dependency>
 ```