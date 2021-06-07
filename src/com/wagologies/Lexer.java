package com.wagologies;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    public List<Token> Lex(String code, List<TokenRule> tokenRules, boolean comments)
    {
        List<Token> tokenList = new ArrayList<>();
        int i = 0;
        while (i < code.length())
        {
            boolean addedToken = false;
            if(comments)
            {
                String currentCode = code.substring(i);
                Matcher matcher = Pattern.compile("^/\\*.*?\\*/").matcher(currentCode);
                if(matcher.find())
                {
                    i += matcher.group().length();
                    continue;
                }
            }
            for (TokenRule tokenRule : tokenRules) {
                Token token = tokenRule.CheckString(code, i);
                if(token != null)
                {
                    tokenList.add(token);
                    i += token.value.length();
                    addedToken = true;
                    break;
                }
            }
            if(!addedToken)
                i++;
        }
        tokenList.add(new Token("", Token.Type.EOF));
        return tokenList;
    }

    public static class TokenRule {
        public Token.Type type;
        public Pattern pattern;

        public TokenRule(Token.Type type, Pattern pattern)
        {
            this.type = type;
            this.pattern = pattern;
        }

        public Token CheckString(String code, int index)
        {
            String currentCode = code.substring(index);
            Matcher matcher = pattern.matcher(currentCode);
            if(matcher.find())
            {
                return new Token(matcher.group(), type);
            }
            return null;
        }
    }

    public static class Token {
        public String value;
        public Type type;

        public Token(String value, Type type)
        {
            this.value = value;
            this.type = type;
        }

        public enum Type {
            NUMBER,
            LPARENTHESES,
            RPARENTHESES,
            ADD,
            SUBTRACT,
            MULTIPLY,
            DIVIDE,
            EOL,
            EOF,
            TASK,
            LBRACKET,
            RBRACKET,
            ID,
            COMMA,
            CALL,
            ASSIGN,
            PIPE,
            CONDITION,
            EQUAL,
            GREATERTHAN,
            LESSTHAN,
            LOOP
        }
    }
}
