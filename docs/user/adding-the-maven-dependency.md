# Adding the Maven Dependency

This document describes how to add this library to an existing Maven project.

## With dependencyManagement

**Step 1:** Use a property inside a top-level POM to define the version once.

```xml
<properties>
    <jdcmp.version>0.1</jdcmp.version>
</properties>
```

**Step 2:** Use `<dependencyManagement>` inside a top-level POM to avoid repeating
`<version>...</version>`.

```xml
<dependencyManagement>
    <dependency>
      <groupId>io.github.jdcmp</groupId>
      <artifactId>comparison-impl-codegen</artifactId>
      <version>${jdcmp.version}</version>
    </dependency>
</dependencyManagement>
```

**Step 3:** Place `<dependency>` inside `<dependencies>` of a child-level POM.

```xml
<dependency>
    <groupId>io.github.jdcmp</groupId>
    <artifactId>comparison-impl-codegen</artifactId>
</dependency>
```

### Example top-level POM

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.github.jdcmp</groupId>
    <artifactId>demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>

    <properties>
        <jdcmp.version>0.1</jdcmp.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.github.jdcmp</groupId>
                <artifactId>comparison-impl-codegen</artifactId>
                <version>${jdcmp.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <modules>
        <module>child</module>
    </modules>

</project>
```

### Example child-level POM

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>io.github.jdcmp</groupId>
        <artifactId>demo</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    
    <artifactId>demo-child</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>io.github.jdcmp</groupId>
            <artifactId>comparison-impl-codegen</artifactId>
        </dependency>
    </dependencies>

</project>
```
