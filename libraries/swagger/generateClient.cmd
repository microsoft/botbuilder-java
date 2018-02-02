call npm install replace@0.3.0

del /q ..\bot-connector\src\main\java\com\microsoft\bot\connector\
del /q ..\bot-connector\src\main\java\com\microsoft\bot\connector\models\
del /q ..\bot-connector\src\main\java\com\microsoft\bot\connector\implementation\

del /q ..\botbuilder-schema\src\main\java\com\microsoft\bot\schema\models\

autorest ./ConnectorAPI.md --java

cd generated

robocopy .\models ..\..\botbuilder-schema\src\main\java\com\microsoft\bot\schema\models *.* /move /xf *Exception.java

call ..\node_modules\.bin\replace "import com.microsoft.bot.schema.models.ErrorResponseException;" "import com.microsoft.bot.connector.models.ErrorResponseException;" . -r --include="*.java"
call ..\node_modules\.bin\replace "import com.microsoft.bot.schema.ConnectorClient;" "import com.microsoft.bot.connector.ConnectorClient;" . -r --include="*.java"
call ..\node_modules\.bin\replace "import com.microsoft.bot.schema.Attachments;" "import com.microsoft.bot.connector.Attachments;" . -r --include="*.java"
call ..\node_modules\.bin\replace "import com.microsoft.bot.schema.Conversations;" "import com.microsoft.bot.connector.Conversations;" . -r --include="*.java"
call ..\node_modules\.bin\replace "import com.microsoft.rest.RestException;" "import com.microsoft.rest.RestException;\nimport com.microsoft.bot.schema.models.ErrorResponse;" . -r --include="ErrorResponseException.java"

robocopy .\ ..\..\bot-connector\src\main\java\com\microsoft\bot\connector *.* /move