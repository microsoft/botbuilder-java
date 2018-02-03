call npm install replace@0.3.0

del /q ..\bot-connector\src\main\java\com\microsoft\bot\connector\
del /q ..\bot-connector\src\main\java\com\microsoft\bot\connector\models\
del /q ..\bot-connector\src\main\java\com\microsoft\bot\connector\implementation\

del /q ..\botbuilder-schema\src\main\java\com\microsoft\bot\schema\models\

call autorest .\ConnectorAPI.md --java

robocopy .\generated\models ..\botbuilder-schema\src\main\java\com\microsoft\bot\schema\models *.* /move /xf *Exception.java

call .\node_modules\.bin\replace "import com.microsoft.bot.schema.models.ErrorResponseException;" "import com.microsoft.bot.connector.models.ErrorResponseException;" . -r -q --include="*.java"
call .\node_modules\.bin\replace "import com.microsoft.bot.schema.ConnectorClient;" "import com.microsoft.bot.connector.ConnectorClient;" . -r -q --include="*.java"
call .\node_modules\.bin\replace "import com.microsoft.bot.schema.Attachments;" "import com.microsoft.bot.connector.Attachments;" . -r -q --include="*.java"
call .\node_modules\.bin\replace "import com.microsoft.bot.schema.Conversations;" "import com.microsoft.bot.connector.Conversations;" . -r -q --include="*.java"
call .\node_modules\.bin\replace "import com.microsoft.rest.RestException;" "import com.microsoft.rest.RestException;import com.microsoft.bot.schema.models.ErrorResponse;" . -r -q --include="ErrorResponseException.java"
call .\node_modules\.bin\replace "package com.microsoft.bot.schema" "package com.microsoft.bot.connector" . -r -q --include="*.java"

robocopy .\generated ..\bot-connector\src\main\java\com\microsoft\bot\connector *.* /e /move