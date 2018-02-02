del /q src\main\java\com\microsoft\bot\schema\*

autorest ../swagger/ConnectorAPI.md --java --java-mode=schema

rd /q /s src\main\java\com\microsoft\bot\schema\implementation