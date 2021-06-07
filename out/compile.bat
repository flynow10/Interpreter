rmdir .\classes\ /s /q
mkdir .\classes\
javac -d ..\out\classes\ ..\src\com\wagologies\Interpreter.java ..\src\com\wagologies\Lexer.java ..\src\com\wagologies\Parser.java ..\src\com\wagologies\Scope.java
javapackager.exe -createjar -appclass com.wagologies.Interpreter -srcdir classes -outfile outjar -v