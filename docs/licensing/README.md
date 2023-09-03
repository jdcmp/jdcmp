# Licensing

This document contains licensing information for this project and its dependencies.

## Main project

This project (excluding third party components) is available under the MIT license. See
[LICENSE.txt](../../LICENSE.txt) for details.

## Third party components

The following components are provided by third parties under separate licenses:

```
(BSD-3-Clause) asm (org.ow2.asm:asm:9.5 - http://asm.ow2.io/)
(BSD-3-Clause) asm-commons (org.ow2.asm:asm-commons:9.5 - http://asm.ow2.io/)
(BSD-3-Clause) asm-tree (org.ow2.asm:asm-tree:9.5 - http://asm.ow2.io/)
(The Apache Software License, Version 2.0) JetBrains Java Annotations (org.jetbrains:annotations:24.0.1 - https://github.com/JetBrains/java-annotations)
```

This list may not be up to date. You should consult the licensing information packaged inside the
`JAR` file for your particular version.

## Third party test components

The following test components are provided by third parties under separate licenses:

```
(Apache License, Version 2.0) Byte Buddy (without dependencies) (net.bytebuddy:byte-buddy:1.11.13 - https://bytebuddy.net/byte-buddy)
(Apache License, Version 2.0) Byte Buddy agent (net.bytebuddy:byte-buddy-agent:1.11.13 - https://bytebuddy.net/byte-buddy-agent)
(The Apache License, Version 2.0) org.apiguardian:apiguardian-api (org.apiguardian:apiguardian-api:1.1.2 - https://github.com/apiguardian-team/apiguardian)
(Apache License, Version 2.0) AssertJ Core (org.assertj:assertj-core:3.24.2 - https://assertj.github.io/doc/#assertj-core)
(Eclipse Public License v2.0) JUnit Jupiter (Aggregator) (org.junit.jupiter:junit-jupiter:5.10.0 - https://junit.org/junit5/)
(Eclipse Public License v2.0) JUnit Jupiter API (org.junit.jupiter:junit-jupiter-api:5.10.0 - https://junit.org/junit5/)
(Eclipse Public License v2.0) JUnit Jupiter Engine (org.junit.jupiter:junit-jupiter-engine:5.10.0 - https://junit.org/junit5/)
(Eclipse Public License v2.0) JUnit Jupiter Params (org.junit.jupiter:junit-jupiter-params:5.10.0 - https://junit.org/junit5/)
(Eclipse Public License v2.0) JUnit Platform Commons (org.junit.platform:junit-platform-commons:1.10.0 - https://junit.org/junit5/)
(Eclipse Public License v2.0) JUnit Platform Engine API (org.junit.platform:junit-platform-engine:1.10.0 - https://junit.org/junit5/)
(The MIT License) mockito-core (org.mockito:mockito-core:3.12.4 - https://github.com/mockito/mockito)
(Apache License, Version 2.0) Objenesis (org.objenesis:objenesis:3.2 - http://objenesis.org/objenesis)
(The Apache License, Version 2.0) org.opentest4j:opentest4j (org.opentest4j:opentest4j:1.3.0 - https://github.com/ota4j-team/opentest4j)
```

This list may not be up to date. You should consult the licensing information packaged inside the
`JAR` file for your particular version.

### Modifications

The following third party components have been modified:

* `org.ow2.asm:*`: Shaded to `io.github.jdcmp.codegen.shaded.org.objectweb.asm`
