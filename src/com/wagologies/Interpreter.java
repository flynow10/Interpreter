package com.wagologies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Interpreter {

    public Lexer lexer;

    public void Start()
    {
        while (true)
        {
            Scanner scanner = new Scanner(System.in);
            String code = scanner.nextLine();
            if(code.equals("exit"))
            {
                break;
            }
            Parse(code);
        }
    }

    public void Parse(String input)
    {
        List<Lexer.Token> tokens = lexer.Lex(input, new ArrayList<Lexer.TokenRule>() {{
            add(new Lexer.TokenRule(Lexer.Token.Type.LPARENTHESES, Pattern.compile("^\\(")));
            add(new Lexer.TokenRule(Lexer.Token.Type.RPARENTHESES, Pattern.compile("^\\)")));
            add(new Lexer.TokenRule(Lexer.Token.Type.ADD, Pattern.compile("^\\+")));
            add(new Lexer.TokenRule(Lexer.Token.Type.SUBTRACT, Pattern.compile("^-")));
            add(new Lexer.TokenRule(Lexer.Token.Type.MULTIPLY, Pattern.compile("^\\*")));
            add(new Lexer.TokenRule(Lexer.Token.Type.DIVIDE, Pattern.compile("^/")));
            add(new Lexer.TokenRule(Lexer.Token.Type.EOL, Pattern.compile("^;")));
            add(new Lexer.TokenRule(Lexer.Token.Type.LBRACKET, Pattern.compile("^\\{")));
            add(new Lexer.TokenRule(Lexer.Token.Type.RBRACKET, Pattern.compile("^}")));
            add(new Lexer.TokenRule(Lexer.Token.Type.COMMA, Pattern.compile("^,")));
            add(new Lexer.TokenRule(Lexer.Token.Type.PIPE, Pattern.compile("^\\|")));
            add(new Lexer.TokenRule(Lexer.Token.Type.TASK, Pattern.compile("^task ")));
            add(new Lexer.TokenRule(Lexer.Token.Type.CALL, Pattern.compile("^call ")));
            add(new Lexer.TokenRule(Lexer.Token.Type.ASSIGN, Pattern.compile("^assign ")));
            add(new Lexer.TokenRule(Lexer.Token.Type.CONDITION, Pattern.compile("^condition")));
            add(new Lexer.TokenRule(Lexer.Token.Type.LOOP, Pattern.compile("^loop")));
            add(new Lexer.TokenRule(Lexer.Token.Type.EQUAL, Pattern.compile("^=")));
            add(new Lexer.TokenRule(Lexer.Token.Type.GREATERTHAN, Pattern.compile("^>")));
            add(new Lexer.TokenRule(Lexer.Token.Type.LESSTHAN, Pattern.compile("^<")));
            add(new Lexer.TokenRule(Lexer.Token.Type.ID, Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*")));
            add(new Lexer.TokenRule(Lexer.Token.Type.NUMBER, Pattern.compile("^[0-9]+")));
        }}, true);
        Parser.AST ast = Parser.Parse(tokens);
        Scope scope = new Scope();
        scope.global = scope;
        scope.functions.put("output", new Scope.FunctionData(new Parser.AST.Output(), "output"));
        ast.Walk(scope);
    }

    public static void main(String[] args)
    {
        Interpreter interpreter = new Interpreter();
        interpreter.lexer = new Lexer();
        if(args.length > 0)
        {
            File file = new File(args[0]);
            if(file.exists()) {
                if(getFileExtension(file).equals("vbl"))
                {
                    StringBuilder input = new StringBuilder();
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String currentInput;
                        while ((currentInput = br.readLine()) != null)
                        {
                            input.append(currentInput);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    interpreter.Parse(input.toString());
                    return;
                }
            }
        }
        interpreter.Start();
    }

    public static String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
}
