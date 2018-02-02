del /q src\main\java\com\microsoft\bot\connector
del /q src\main\java\com\microsoft\bot\connector\implementation

autorest ../swagger/ConnectorAPI.md --java --java-mode=connector