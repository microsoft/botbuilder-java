# ![Bot Framework for Java](./docs/media/BotFrameworkJava_header.png)

This repository contains code for the Java version of the [Microsoft Bot Framework SDK](https://github.com/Microsoft/botframework-sdk), which is part of the Microsoft Bot Framework - a comprehensive framework for building enterprise-grade conversational AI experiences.

This SDK enables developers to model conversation and build sophisticated bot applications using Java. SDKs for [.NET](https://github.com/Microsoft/botbuilder-dotnet), [Python](https://github.com/Microsoft/botbuilder-python) and [JavaScript](https://github.com/Microsoft/botbuilder-js) are also available.

To get started building bots using the SDK, see the [Azure Bot Service Documentation](https://docs.microsoft.com/en-us/azure/bot-service/?view=azure-bot-service-4.0).  If you are an existing user, then you can also [find out what's new with Bot Framework](https://docs.microsoft.com/en-us/azure/bot-service/what-is-new?view=azure-bot-service-4.0).

For more information jump to a section below.

- [!Bot Framework for Java](#)
  - [Build Status](#build-status)
  - [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Clone](#clone)
    - [Build and test locally](#build-and-test-locally)
    - [Linting rules](#linting-rules)
  - [Getting support and providing feedback](#getting-support-and-providing-feedback)
    - [Github issues](#github-issues)
    - [Stack overflow](#stack-overflow)
    - [Azure Support](#azure-support)
    - [Twitter](#twitter)
    - [Gitter Chat Room](#gitter-chat-room)
  - [Contributing and our code of conduct](#contributing-and-our-code-of-conduct)
  - [Reporting Security Issues](#reporting-security-issues)

## Build Status

 | Branch | Description | Build Status | Coverage Status |
 |--------|-------------|--------------|-----------------|
 |Main | 4.15.* Builds | [![Build Status](https://fuselabs.visualstudio.com/SDK_v4/_apis/build/status/Java/BotBuilder-Java-4.0-daily?branchName=main)](https://fuselabs.visualstudio.com/SDK_v4/_build/latest?definitionId=1202&branchName=main) | [![Coverage Status](https://coveralls.io/repos/github/microsoft/botbuilder-java/badge.svg?branch=823847c676b7dbb0fa348a308297ae375f5141ef)](https://coveralls.io/github/microsoft/botbuilder-java?branch=823847c676b7dbb0fa348a308297ae375f5141ef) |

## Getting Started
To get started building bots using the SDK, see the [Azure Bot Service Documentation](https://docs.microsoft.com/en-us/azure/bot-service/?view=azure-bot-service-4.0).

The [Bot Framework Samples](https://github.com/microsoft/botbuilder-samples) includes a rich set of samples repository.

If you want to debug an issue, would like to [contribute](#contributing), or understand how the Bot Builder SDK works, instructions for building and testing the SDK are below.

### Prerequisites
- [Git](https://git-scm.com/downloads)
- [Java](https://www.azul.com/downloads/zulu/)
- [Maven](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)

### Clone
Clone a copy of the repo:
```bash
git clone https://github.com/Microsoft/botbuilder-java.git
```
Change to the SDK's directory:
```bash
cd botbuilder-java
```

Now at the command prompt type:
```bash
mvn clean install
```

### Build and test locally
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

### Linting rules

This project uses linting rules to enforce code standardization. These rules are specified in the file [bot-checkstyle.xml](./etc/bot-checkstyle.xml) with [CheckStyle](https://checkstyle.org/) and are hooked to Maven's build cycle.

**INFO**: Since the CheckStyle and PMD plugins are hooked into the build cycle, this makes the build **fail** in cases where there are linting warnings in the project files.  Errors will be in the file ./target/checkstyle-result.xml and ./target/pmd.xml.

CheckStyle is available in different flavours:
- [Visual Studio Code plugin](https://marketplace.visualstudio.com/items?itemName=shengchen.vscode-checkstyle)
- [IntelliJ IDEA plugin](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea)
- [Eclipse plugin](https://checkstyle.org/eclipse-cs)
- [CLI Tool](https://checkstyle.org/cmdline.html)

**INFO**: Be sure to configure your IDE to use the file [bot-checkstyle.xml](./etc/bot-checkstyle.xml) instead of the default rules.

## Getting support and providing feedback
Below are the various channels that are available to you for obtaining support and providing feedback. Please pay carful attention to which channel should be used for which type of content. e.g. general "how do I..." questions should be asked on Stack Overflow, Twitter or Gitter, with GitHub issues being for feature requests and bug reports.

### Github issues
[Github issues](https://github.com/Microsoft/botbuilder-python/issues) should be used for bugs and feature requests.

### Stack overflow
[Stack Overflow](https://stackoverflow.com/questions/tagged/botframework) is a great place for getting high-quality answers. Our support team, as well as many of our community members are already on Stack Overflow providing answers to 'how-to' questions.

### Azure Support
If you issues relates to [Azure Bot Service](https://azure.microsoft.com/en-gb/services/bot-service/), you can take advantage of the available [Azure support options](https://azure.microsoft.com/en-us/support/options/).

### Twitter
We use the [@botframework](https://twitter.com/botframework) account on twitter for announcements and members from the development team watch for tweets for @botframework.

### Gitter Chat Room
The [Gitter Channel](https://gitter.im/Microsoft/BotBuilder) provides a place where the Community can get together and collaborate.

## Contributing and our code of conduct
We welcome contributions and suggestions. Please see our [contributing guidelines](./contributing.md) for more information.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact
 [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## Reporting Security Issues
Security issues and bugs should be reported privately, via email, to the Microsoft Security Response Center (MSRC)
at [secure@microsoft.com](mailto:secure@microsoft.com).  You should receive a response within 24 hours.  If for some
 reason you do not, please follow up via email to ensure we received your original message. Further information,
 including the [MSRC PGP](https://technet.microsoft.com/en-us/security/dn606155) key, can be found in the
[Security TechCenter](https://technet.microsoft.com/en-us/security/default).

Copyright (c) Microsoft Corporation. All rights reserved.

Licensed under the [MIT](./LICENSE.md) License.



