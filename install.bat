set ApkPath=%cd%\mingnan\build\outputs\apk  
cd %ApkPath%  
  
for /R %%s in (.,*) do (  
    ::要使用引号来包括apk的路径，不然adb install语法报错  
    adb install "%%s"  
)  
  
:: 执行完，cmd窗口暂留  
pause  