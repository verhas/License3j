@ECHO OFF
REM ----------------------------------------------------------------------------
REM License3j keyword line batch script for Windows XP
REM
REM Required ENV vars:
REM JAVA_HOME - location of a JDK home dir
REM
REM Optional ENV vars
REM APP_HOME - location of APP2's installed home dir
REM JAVA_OPTS - parameters passed to the Java VM when running APP
REM     e.g. to debug APP use
REM SET JAVA_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
REM ----------------------------------------------------------------------------

REM Configuration parameters.
REM
REM When using this batch script for any new application you should
REM alter only the lines that are between

REM <<CHANGE START>>
REM (this can be changed) and
REM <<CHANGE END>>
REM (this should not be changed)

SET ERROR_CODE=0

REM set local scope for the variables with windows NT shell
IF "%OS%"=="Windows_NT" @setlocal
IF "%OS%"=="WINNT" @setlocal

REM ==== START VALIDATION ====
IF NOT "%JAVA_HOME%" == "" GOTO JavaHomeIsOK

ECHO.
ECHO ERROR: JAVA_HOME not found in your environment.
ECHO Please SET the JAVA_HOME variable in your environment to match the
ECHO location of your Java installation
ECHO.
GOTO error

:JavaHomeIsOK
IF EXIST "%JAVA_HOME%\bin\java.exe" GOTO checkAppHome

ECHO.
ECHO ERROR: JAVA_HOME is SET to an invalid directory.
ECHO JAVA_HOME = "%JAVA_HOME%"
ECHO Please SET the JAVA_HOME variable in your environment to match the
ECHO location of your Java installation
ECHO.
GOTO error

:checkAppHome
IF NOT "%APP_HOME%"=="" GOTO valMHome

IF "%OS%"=="Windows_NT" SET "APP_HOME=%~dp0"
IF "%OS%"=="WINNT" SET "APP_HOME=%~dp0"
IF NOT "%APP_HOME%"=="" GOTO valMHome

ECHO.
ECHO ERROR: APP_HOME not found in your environment.
ECHO Please SET the APP_HOME variable in your environment to match the
ECHO location of the APP installation
ECHO.
GOTO error

:valMHome

IF EXIST "%APP_HOME%\%BATCH_FILE_NAME%" GOTO checkCpApp

ECHO.
ECHO ERROR: APP_HOME is SET to an invalid directory.
ECHO APP_HOME = "%APP_HOME%"
ECHO Please SET the APP_HOME variable in your environment to match the
ECHO location of the APP installation
ECHO.
GOTO error

:checkCpApp
IF EXIST "%APP_HOME%\cpapp.bat" GOTO init
REM oops.. cpapp.bat does not EXIST, try to create it
ECHO SET CLASSPATH=%%CLASSPATH%%;%%1> "%APP_HOME%\cpapp.bat"
IF EXIST "%APP_HOME%\cpapp.bat" GOTO init1
ECHO.
ECHO ERROR: APP_HOME directory does not contain the batch file cpapp.bat and
ECHO the script %BATCH_FILE_NAME% can not create it.
ECHO APP_HOME = "%APP_HOME%"
ECHO Either change the file permissions so that this bacth file can create
ECHO cpapp.bat or create the file. The file cpapp.bat should contain a
ECHO single line:
ECHO .
ECHO SET CLASSPATH=%%CLASSPATH%%;%%1
ECHO .
ECHO Note that there should NOT be any trailing space at the end of the
ECHO line.
ECHO.
GOTO error
REM ==== END VALIDATION ====


:init
:init1
cd %APP_HOME%
REM Decide how to startup depending on the version of windows

REM -- Windows NT with Novell Login
IF "%OS%"=="WINNT" GOTO WinNTNovell

REM -- Win98ME
IF NOT "%OS%"=="Windows_NT" GOTO Win9xArg

:WinNTNovell

REM -- 4NT shell
IF "%@eval[2+2]" == "4" GOTO 4NTArgs

REM -- Regular WinNT shell
SET APP_CMD_LINE_ARGS=%*
GOTO endInit

REM The 4NT Shell from jp software
:4NTArgs
SET APP_CMD_LINE_ARGS=%$
GOTO endInit

:Win9xArg
REM Slurp the keyword line arguments.  This loop allows for an unlimited number
REM of agruments (up to the keyword line limit, anyway).
SET APP_CMD_LINE_ARGS=
:Win9xApp
IF %1a==a GOTO endInit
SET APP_CMD_LINE_ARGS=%APP_CMD_LINE_ARGS% %1
shift
GOTO Win9xApp

REM Reaching here means variables are defined and arguments have been captured
:endInit
SET JAVA_EXE="%JAVA_HOME%\bin\java.exe"

REM -- 4NT shell
IF "%@eval[2+2]" == "4" GOTO 4NTCWJars

REM -- Regular WinNT shell
SET CLASSPATH=""
for %%i in ("%APP_HOME%"lib\*.jar) do call "%APP_HOME%\cpapp.bat" "%%i"
GOTO runf2jp

REM The 4NT Shell from jp software
:4NTCWJars
SET CLASSPATH=""
for %%i in ("%APP_HOME%lib\*.jar") do call "%APP_HOME%\cpapp.bat" "%%i"
GOTO runf2jp

REM Start APP2
:runf2jp
REM <<CHANGE START>>
REM The name of the batch file. It is used to check that the home directory
REM is calculated correctly.
SET BATCH_FILE_NAME=license3j.bat
REM the location of the configuration file
REM the main class that is to be executed
SET MAIN_CLASS=com.javax0.license3j.License3j
REM local java options
SET LOCAL_JAVA_OPTS=-Xmx1024m
REM arguments to be passed to the Java program before the BAT file arguments
SET ARGS1=
REM arguments to be passed to the Java program after the BAT file arguments
SET ARGS2=
REM <<CHANGE END>>

%JAVA_EXE% %JAVA_OPTS% %LOCAL_JAVA_OPTS% -classpath %CLASSPATH% %MAIN_CLASS% %ARGS1% %APP_CMD_LINE_ARGS% %ARGS2%
IF ERRORLEVEL 1 GOTO error
GOTO end

:error
IF "%OS%"=="Windows_NT" @endlocal
IF "%OS%"=="WINNT" @endlocal
SET ERROR_CODE=1

:end
REM SET local scope for the variables with windows NT shell
IF "%OS%"=="Windows_NT" GOTO endNT
IF "%OS%"=="WINNT" GOTO endNT

REM For old DOS remove the SET variables from ENV - we assume they were not SET
REM before we started - at least we don't leave any baggage around
SET JAVA_EXE=
SET APP_CMD_LINE_ARGS=
GOTO postExec

:endNT
@endlocal & SET ERROR_CODE=%ERROR_CODE%

:postExec

IF "%APP_TERMINATE_CMD%" == "on" exit %ERROR_CODE%

cmd /C exit /B %ERROR_CODE%

