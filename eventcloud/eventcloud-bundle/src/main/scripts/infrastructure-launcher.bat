@echo off

rem Some useful vars
set CURRENT_DIR=%~dp0
cd %CURRENT_DIR%\..
set BUNDLE_HOME=%cd%
cd %CURRENT_DIR%
set PATH_TO_LIBRARIES=%BUNDLE_HOME%\libs
set PATH_TO_RESOURCES=%BUNDLE_HOME%\resources
set CLASSPATH=%PATH_TO_LIBRARIES%\*;%CLASSPATH%

rem Paths to output files
set LOGS_DIR=%BUNDLE_HOME%\logs
set REGISTRY_OUTPUT_FILE=%LOGS_DIR%\eventclouds-registry.output
set WS_EVENTCLOUDS_MANAGEMENT_OUTPUT_FILE=%LOGS_DIR%\ws-eventclouds-management.output

rem Paths to instance files
set INSTANCES_DIR=%BUNDLE_HOME%\instances
set REGISTRY_INSTANCE_FILE=%INSTANCES_DIR%\eventclouds-registry
set WS_EVENTCLOUDS_MANAGEMENT_INSTANCE_FILE=%INSTANCES_DIR%\ws-eventclouds-management

rem Ports assignation
set EVENTCLOUDS_REGISTRY_PORT=8081
set EVENTCLOUDS_PORTS_LOWER_BOUND=1100
set PROXIES_PORTS_LOWER_BOUND=9000
set WS_EVENTCLOUDS_MANAGEMENT_HTTP_PORT=8082
set WS_EVENTCLOUDS_MANAGEMENT_PNP_PORT=8083

rem Count number of arguments
set argc=0
for %%x in (%*) do set /a argc+=1

rem Check arguments
if %argc% EQU 1 (
  if "%1" == "/k" (
    call:undeploy
    goto:eof
  ) else if "%1" == "/kc" (
    call:undeploy
    rmdir /s /q %LOGS_DIR% 2> nul
    goto:eof
  ) else (
    echo usage: infrastructure-launcher.bat [/k^|/kc]
    echo        /k  kills the running instance
    echo        /kc kills the running instance and removes the log files
  )
) else (
  if exist %INSTANCES_DIR%\ (
    echo Eventclouds registry already running. Use /k or /kc option to kill all existing instances.
    goto:eof
  ) else (
    if not exist %INSTANCES_DIR%\ md %INSTANCES_DIR%
    if not exist %LOGS_DIR%\ md %LOGS_DIR%
    call:deploy
  )
)
goto:eof

:deploy
  where java > nul 2>&1
  if %errorlevel% == 1 (
    echo Java is required!
    echo Download from http://www.oracle.com/technetwork/java/javase/downloads/index.html and install it
    goto:eof
  )

  where python > nul 2>&1
  if %errorlevel% == 1 (
    echo Python is required!
    echo Download from http://www.python.org/download/ and install it
    echo Open the Control Panel -> System or Security –> System -> Advanced System Settings -> Environment Variables;
    echo Edit the Path system variable to append ";C:\Python27\;C:\Python27\Scripts"
    echo Download easy_install from http://peak.telecommunity.com/dist/ez_setup.py
    echo Execute "python ez_setup.py"
    echo Execute "easy_install watchdog"
    goto:eof
  )

  where easy_install > nul 2>&1
  if %errorlevel% == 1 (
    echo Easy install is required!
    echo Download easy_install from http://peak.telecommunity.com/dist/ez_setup.py
    echo Execute "python ez_setup.py"
    echo Execute "easy_install watchdog"
    goto:eof
  )

  where watchmedo > nul 2>&1
  if %errorlevel% == 1 (
    echo Watchdog library is required!
    echo Execute "easy_install watchdog"
    goto:eof
  )

  call:deploy_eventclouds_registry
  call:deploy_ws_management_proxy
  echo Logs available at:
  echo     %LOGS_DIR%
goto:eof


:deploy_eventclouds_registry
  for /F %%i in ("%REGISTRY_INSTANCE_FILE%") do set BASENAME_REGISTRY_INSTANCE_FILE=%%~ni
  start "EventcloudsRegistry" /b javaw -Xms128m -Xmx256m ^
     -server ^
     -Djava.security.policy=%PATH_TO_RESOURCES%/proactive.security.policy ^
     -Deventcloud.bundle.home=%BUNDLE_HOME% ^
     -Deventcloud.configuration=%PATH_TO_RESOURCES%/eventcloud.properties ^
     -Deventcloud.instance.file=%REGISTRY_INSTANCE_FILE% ^
     -Dlog4j.configuration=file:%PATH_TO_RESOURCES%/log4j.properties ^
     -Dlogback.configurationFile=file:%PATH_TO_RESOURCES%/logback.xml ^
     -Dlogging.output.filename=%BASENAME_REGISTRY_INSTANCE_FILE% ^
     -Dproactive.communication.protocol=pnp ^
     -Dproactive.pnp.port=%EVENTCLOUDS_REGISTRY_PORT% ^
     -cp %CLASSPATH% ^
     fr.inria.eventcloud.deployment.cli.launchers.EventCloudsRegistryLauncher > %REGISTRY_OUTPUT_FILE% 2>&1

  if not exist %REGISTRY_INSTANCE_FILE% (
    start "EventcloudsRegistry Watchdog" /w ^
       python %BUNDLE_HOME%/scripts/filename-watchdog.py %INSTANCES_DIR% ^
       %BASENAME_REGISTRY_INSTANCE_FILE% > nul 2>&1  
  )
  
  set /p EVENTCLOUDS_REGISTRY_URL= < %REGISTRY_INSTANCE_FILE%
  echo Eventclouds registry deployed at:
  echo     %EVENTCLOUDS_REGISTRY_URL%
goto:eof

:deploy_ws_management_proxy
  for /F %%i in ("%WS_EVENTCLOUDS_MANAGEMENT_INSTANCE_FILE%") do set BASENAME_WS_EVENTCLOUDS_MANAGEMENT_INSTANCE_FILE=%%~ni
  start "EventcloudsManagementWebservice" /b javaw -Xms256m -Xmx10240m ^
     -server ^
     -Djava.security.policy=%PATH_TO_RESOURCES%/proactive.security.policy ^
     -Deventcloud.bundle.home=%BUNDLE_HOME% ^
     -Deventcloud.configuration=%PATH_TO_RESOURCES%/eventcloud.properties ^
     -Deventcloud.instance.file=%WS_EVENTCLOUDS_MANAGEMENT_INSTANCE_FILE% ^
     -Dlog4j.configuration=file:%PATH_TO_RESOURCES%/log4j.properties ^
     -Dlogback.configurationFile=file:%PATH_TO_RESOURCES%/logback.xml ^
     -Dlogging.output.filename=%BASENAME_WS_EVENTCLOUDS_MANAGEMENT_INSTANCE_FILE% ^
     -Dproactive.communication.protocol=pnp ^
     -Dproactive.pnp.port=%WS_EVENTCLOUDS_MANAGEMENT_PNP_PORT% ^
     -cp %CLASSPATH% fr.inria.eventcloud.deployment.cli.launchers.EventCloudManagementWsLaucher ^
     --registry-url %EVENTCLOUDS_REGISTRY_URL% --port-lower-bound %PROXIES_PORTS_LOWER_BOUND% ^
     -p %WS_EVENTCLOUDS_MANAGEMENT_HTTP_PORT% > %WS_EVENTCLOUDS_MANAGEMENT_OUTPUT_FILE% 2>&1
	 
     if not exist %WS_EVENTCLOUDS_MANAGEMENT_INSTANCE_FILE% (
       start "EventcloudsManagementWebservice Watchdog" /w ^
	      python %BUNDLE_HOME%/scripts/filename-watchdog.py %INSTANCES_DIR% ^
          %BASENAME_WS_EVENTCLOUDS_MANAGEMENT_INSTANCE_FILE% > nul 2>&1  
     )
	 
     set /p WS_EVENTCLOUDS_MANAGEMENT_URL= < %WS_EVENTCLOUDS_MANAGEMENT_INSTANCE_FILE%
	 SET WS_EVENTCLOUDS_MANAGEMENT_URL=%WS_EVENTCLOUDS_MANAGEMENT_URL:0.0.0.0=localhost%
     echo Eventclouds management web service deployed at:
     echo     %WS_EVENTCLOUDS_MANAGEMENT_URL%
goto:eof

:undeploy
  rem TODO(laurent) try to improve termination by killing only the java 
  rem processes which have been started from the script
  taskkill /f /fi "imagename eq javaw.exe" > nul 2>&1
  rmdir /s /q %INSTANCES_DIR% 2> nul
goto:eof
