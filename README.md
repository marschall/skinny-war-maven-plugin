Skinny WAR Maven Plugin
=======================

The truststore Maven plugin gives you an easy way to build [skinny WARs](https://maven.apache.org/plugins/maven-ear-plugin/examples/skinny-wars.html).

Usage
-----

Simply run the `skinny-war-maven-plugin` after the `maven-ear-plugin`


```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.acme</groupId>
  <artifactId>application</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>ear</packaging>

  <build>
    <plugins>
      <plugin>
        <artifactId>maven-ear-plugin</artifactId>
      </plugin>
      <plugin>
        <groupId>com.github.marschall</groupId>
        <artifactId>skinny-war-maven-plugin</artifactId>
        <version>1.0.0</version>
        <executions>
          <execution>
            <id>repackage</id>
            <goals>
              <goal>repackage</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>

```


For more information check out the generated [plugin page](https://marschall.github.io/skinny-war-maven-plugin/).

