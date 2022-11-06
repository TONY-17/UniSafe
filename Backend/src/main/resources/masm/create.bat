@echo off
setlocal enabledelayedexpansion
set PROJNAME=%~1
set ERRMSG=

if "%PROJNAME%"=="" (
    echo "Usage: create [asmFileNameWithoutExtension]"
    goto END
)

:CLEAN
echo "~~~ Cleaning project ~~~"
DEL /S %PROJNAME%.exe %PROJNAME%.ilk %PROJNAME%.pdb %PROJNAME%.lst %PROJNAME%.obj
IF /I "%ERRORLEVEL%" NEQ "0" (
    set ERRMSG="Cleaning"
    GOTO ERROR
)

:ASSEMBLE
echo "~~~ Assembling project ~~~"
.\assembler\ml.exe /c /coff /Fl /Zi src\%PROJNAME%.asm
IF /I "%ERRORLEVEL%" NEQ "0" (
    set ERRMSG="Assembling"
    GOTO ERROR
)

:LINK
echo "~~~ Linking project ~~~"
.\assembler\link.exe /debug /subsystem:console /entry:start /out:bin\%PROJNAME%.exe %PROJNAME%.obj .\assembler\kernel32.lib .\assembler\io.lib
IF /I "%ERRORLEVEL%" NEQ "0" (
    set ERRMSG="Linking"
    GOTO ERROR
)

:RUN
echo "~~~ Running project ~~~"
bin\%PROJNAME%.exe
IF /I "%ERRORLEVEL%" NEQ "0" (
    set ERRMSG="Running"
    GOTO ERROR
)
GOTO END

:ERROR
echo "!!! An error has occured !!!"
echo %ERRMSG%
pause

:END
echo "~~~ End ~~~"
