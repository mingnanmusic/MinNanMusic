set ApkPath=%cd%\mingnan\build\outputs\apk  
cd %ApkPath%  
  
for /R %%s in (.,*) do (  
    ::Ҫʹ������������apk��·������Ȼadb install�﷨����  
    adb install "%%s"  
)  
  
:: ִ���꣬cmd��������  
pause  