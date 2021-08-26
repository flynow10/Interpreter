cd C:/Code/java/Interpreter/out/
rmdir .\classes\ /s /q
mkdir .\classes\
javac -d ..\out\classes\ ..\src\com\wagologies\*.java ..\src\com\wagologies\Parser\*.java ..\src\com\wagologies\Parser\Nodes\*.java
javapackager.exe -createjar -appclass com.wagologies.Interpreter -srcdir classes -outfile outjar -v