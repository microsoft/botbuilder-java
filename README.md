# ![Bot Framework for Java](./docs/media/BotFrameworkJava_header.png)

### [Click here to find out what's new](https://github.com/Microsoft/botframework/blob/master/whats-new.md#whats-new)

# Bot Framework SDK for Java (Preview)
[![Build Status](https://travis-ci.org/Microsoft/botbuilder-java.svg?branch=master)](https://travis-ci.org/Microsoft/botbuilder-java)
[![Coverage Status](https://coveralls.io/repos/github/microsoft/botbuilder-java/badge.svg?branch=823847c676b7dbb0fa348a308297ae375f5141ef)](https://coveralls.io/github/microsoft/botbuilder-java?branch=823847c676b7dbb0fa348a308297ae375f5141ef)
[![roadmap badge](https://img.shields.io/badge/visit%20the-roadmap-blue.svg)](https://github.com/Microsoft/botbuilder-java/wiki/Roadmap)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/Microsoft/botbuilder-java/blob/master/LICENSE)
[![Gitter](https://img.shields.io/gitter/room/Microsoft/BotBuilder.svg)](https://gitter.im/Microsoft/BotBuilder)

This repository contains code for the Java version of the Microsoft Bot Framework SDK. The Bot Framework SDK v4 enable developers to model conversation and build sophisticated bot applications.

This repo is part the [Microsoft Bot Framework](https://github.com/microsoft/botframework) - a comprehensive framework for building enterprise-grade conversational AI experiences.

To get started see the [Azure Bot Service Documentation](https://docs.microsoft.com/en-us/azure/bot-service/?view=azure-bot-service-4.0) for the v4 SDK.

## Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## Linting rules

This project uses linting rules to enforce code standardization. These rules are specified in the file [bot-checkstyle.xml](./etc/bot-checkstyle.xml) with [CheckStyle](https://checkstyle.org/) and are hooked to Maven's build cycle.

**INFO**: Since the CheckStyle and PMD plugins are hooked into the build cycle, this makes the build **fail** in cases where there are linting warnings in the project files.  Errors will be in the file ./target/checkstyle-result.xml and ./target/pmd.xml.

CheckStyle is available in different flavours:
- [Visual Studio Code plugin](https://marketplace.visualstudio.com/items?itemName=shengchen.vscode-checkstyle)
- [IntelliJ IDEA plugin](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea)
- [Eclipse plugin](https://checkstyle.org/eclipse-cs)
- [CLI Tool](https://checkstyle.org/cmdline.html)

**INFO**: Be sure to configure your IDE to use the file [bot-checkstyle.xml](./etc/bot-checkstyle.xml) instead of the default rules.

## Build and IDE

Any IDE that can import and work with Maven projects should work.  As a matter of practice we use the command line to perform Maven builds.  If your IDE can be configured to defer build and run to Maven it should also work.
- Java
  - We use the [Azul JDK 8](https://www.azul.com/downloads/azure-only/zulu/?version=java-8-lts&architecture=x86-64-bit&package=jdk) to build and test with.  While not a requirement to develop the SDK with, it is recommended as this is what Azure is using for Java 1.8.  If you do install this JDK, make sure your IDE is targeting that JVM, and your path (from command line) and JAVA_HOME point to that.

- Visual Studio Code
  - Extensions
    - Java Extension Pack by Microsoft
    - EditorConfig for VS Code by EditorConfig (Recommended)

- IntelliJ
  - Extensions
    - Checkstyle by IDEA
  - Recommended setup
    - When importing the SDK for the first time, make sure "Auto import" is checked.

## Reporting Security Issues

Security issues and bugs should be reported privately, via email, to the Microsoft Security
Response Center (MSRC) at [secure@microsoft.com](mailto:secure@microsoft.com). You should
receive a response within 24 hours. If for some reason you do not, please follow up via
email to ensure we received your original message. Further information, including the
[MSRC PGP](https://technet.microsoft.com/en-us/security/dn606155) key, can be found in
the [Security TechCenter](https://technet.microsoft.com/en-us/security/default).

Copyright (c) Microsoft Corporation. All rights reserved.

