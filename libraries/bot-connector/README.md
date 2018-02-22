# Microsoft Bot Framework Connector for Java

Within the Bot Framework, the Bot Connector service enables your bot to exchange messages with users on channels that are configured in the Bot Framework Portal.

## How to Install
Add to maven dependencies

````
  <dependency>
      <groupId>com.microsoft.bot.connector</groupId>
      <artifactId>bot-connector</artifactId>
      <version>VERSION</version>
  </dependency>
````

## How to Use

### Authentication
Your bot communicates with the Bot Connector service using HTTP over a secured channel (SSL/TLS). When your bot sends a request to the Connector service, it must include information that the Connector service can use to verify its identity.

To authenticate the requests, you'll need configure the Connector with the App ID and password that you obtained for your bot during registration and the Connector will handle the rest.

More information: https://docs.microsoft.com/en-us/bot-framework/rest-api/bot-framework-rest-connector-authentication

### Example
Client creation (with authentication), conversation initialization and activity send to user.

 ```java
  String appId = "<your-app-id>";
  String appPassword = "<your-app-password>";
  
  MicrosoftAppCredentials credentials = new MicrosoftAppCredentials(appId, appPassword);
  
  ConnectorClientImpl client = new ConnectorClientImpl("https://slack.botframework.com", credentials);
  
  ChannelAccount bot = new ChannelAccount().withId("<bot-id>");
  ChannelAccount user = new ChannelAccount().withId("<user-id>");

  ConversationParameters conversation = new ConversationParameters()
        .withBot(bot)
        .withMembers(Collections.singletonList(user))
        .withIsGroup(false);

  ConversationResourceResponse result = client.conversations().createConversation(conversation);
        
  Activity activity = new Activity()
        .withType(ActivityTypes.MESSAGE)
        .withFrom(bot)
        .withRecipient(user)
        .withText("this a message from Bot Connector Client (Java)");

  ResourceResponse response = client.conversations().sendToConversation(result.id(), activity);
 ```
 
 ### Simple EchoBot Example ([source code](../../samples/bot-connector-sample))
EchoBot is a minimal bot that receives message activities and replies with the same content.
The sample shows how to use HTTPServer for listening to activities and the ConnectorClient for sending activities.

## Rest API Documentation

For the Connector Service API Documentation, please see our [API reference](https://docs.microsoft.com/en-us/Bot-Framework/rest-api/bot-framework-rest-connector-api-reference).

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

## License

Copyright (c) Microsoft Corporation. All rights reserved.

Licensed under the [MIT](https://github.com/Microsoft/vscode/blob/master/LICENSE.txt) License.
