@REM Maven Wrapper startup batch script

@ECHO OFF
SETLOCAL

SET "MAVEN_PROJECTBASEDIR=%~dp0"

IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
    ECHO Downloading Maven Wrapper JAR...
    mkdir "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" 2>nul
    powershell -Command "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar' -OutFile '%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar'" || (
        bitsadmin /transfer mavenWrapperDownload /download /priority normal "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar" "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
    )
    IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
        ECHO ERROR: Failed to download maven-wrapper.jar
        ECHO Please manually download it from:
        ECHO https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
        EXIT /B 1
    )
)

IF DEFINED JAVA_HOME (
    SET "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) ELSE (
    SET "JAVA_EXE=java"
)

"%JAVA_EXE%" ^
    %MAVEN_OPTS% ^
    %MAVEN_DEBUG_OPTS% ^
    -classpath "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" ^
    "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
    org.apache.maven.wrapper.MavenWrapperMain %*

ENDLOCAL
EXIT /B %ERRORLEVEL%
