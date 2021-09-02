package com.wagologies.Parser;

import com.wagologies.Lexer;
import com.wagologies.Parser.Nodes.*;
import com.wagologies.Parser.Nodes.Number;

import java.util.*;
import java.util.stream.Collectors;

public class Parser {

//    ast             : block
//    task_definition : TASK ( RETURNS | empty ) variable LPARENTHESES ( empty | type ID ( COMMA type PIPE ID )* ) RPARENTHESES LBRACKET block RBRACKET
//    task_call       : CALL variable LPARENTHESES ( expr COMMA )* RPARENTHESES
//    block           : ( statement )*
//    statement       : (( task_call | assignment | return ) EOL ) | ( conditional | task_definition | loop )
//    assignment      : ASSIGN variable PIPE type expr
//    return          : RETURN expr
//    expr            : term (( PLUS | MINUS ) term )* | STRING | tralse
//    term            : factor (( MULTIPLY | DIVIDE ) term )*
//    factor          : INTEGER | LPARENTHESES expr RPARENTHESES | variable | task_call
//    variable        : ID
//    conditional     : CONDITION LPARENTHESES expr RPARENTHESES LBRACKET block RBRACKET
//    comparison      : COMPARISON LPARENTHESES expr ( EQUAL | GREATERTHAN | LESSTHAN ) expr RPARENTHESES
//    loop            : LOOP LPARENTHESES expr RPARENTHESES LBRACKET block RBRACKET
//    type            : STRING_TYPE | INTEGER_TYPE | TRALSE_TYPE |
//    tralse          : TRUE | FALSE | condition
//    empty           :
    public List<Lexer.Token> tokens;

    public int currentTokenIndex;

    public Parser(List<Lexer.Token> tokens)
    {
        this.tokens = tokens;
    }

    public Lexer.Token getCurrentToken()
    {
        return tokens.get(currentTokenIndex);
    }

    public void Eat(Lexer.Token.Type type, Lexer.Token.Type... otherOptions)
    {
        if(type == getCurrentToken().type)
            currentTokenIndex++;
        else {
            List<Lexer.Token.Type> types = new ArrayList<>(Collections.singletonList(type));
            types.addAll(Arrays.asList(otherOptions));
            Error(Arrays.copyOf(types.toArray(), types.size(), Lexer.Token.Type[].class));
        }
    }

    public void Error(Lexer.Token.Type... otherOptions)
    {
        throw new RuntimeException(Arrays.stream(otherOptions).map(Enum::toString).collect(Collectors.joining(",")) + " expected instead found " + getCurrentToken().type + ".");
    }

    //    ast             : block
    public AST Parse()
    {
        Node node = Block();
        return new AST(node);
    }

    //    block           : (statement)*
    public Node Block()
    {
        List<Node> nodes = new ArrayList<>();
        while (getCurrentToken().type != Lexer.Token.Type.RBRACKET && getCurrentToken().type != Lexer.Token.Type.EOF)
        {
            nodes.add(Statement());
        }
        return new Block(nodes);
    }

    //    statement       : ((task_call | assignment) EOL) | (conditional | task_definition)
    public Node Statement()
    {
        switch (getCurrentToken().type)
        {
            case CALL:
                Node taskCallNode = TaskCall();
                Eat(Lexer.Token.Type.EOL);
                return taskCallNode;
            case ASSIGN:
                Node assignmentNode = Assignment();
                Eat(Lexer.Token.Type.EOL);
                return assignmentNode;
            case RETURN:
                Node returnNode = Return();
                Eat(Lexer.Token.Type.EOL);
                return returnNode;
            case CONDITION:
                return Conditional();
            case TASK:
                return TaskDefinition();
            case LOOP:
                return Loop();
        }
        Error(Lexer.Token.Type.CALL, Lexer.Token.Type.ASSIGN, Lexer.Token.Type.CONDITION, Lexer.Token.Type.TASK, Lexer.Token.Type.LOOP, Lexer.Token.Type.RETURN);
        return null;
    }

    //    task_call       : CALL variable LPARENTHESES ( | expr (COMMA expr)* ) RPARENTHESES
    public Node TaskCall()
    {
        Eat(Lexer.Token.Type.CALL, Lexer.Token.Type.ASSIGN, Lexer.Token.Type.CONDITION, Lexer.Token.Type.TASK, Lexer.Token.Type.LOOP, Lexer.Token.Type.RETURN);
        Node variable = Variable();
        Eat(Lexer.Token.Type.LPARENTHESES);
        List<Node> parameters = new ArrayList<>();
        if(getCurrentToken().type != Lexer.Token.Type.RPARENTHESES)
        {
            parameters.add(Expression());
        }
        while (getCurrentToken().type == Lexer.Token.Type.COMMA)
        {
            Eat(Lexer.Token.Type.COMMA);
            parameters.add(Expression());
        }
        Eat(Lexer.Token.Type.RPARENTHESES);
        return new TaskCall(variable, parameters);
    }

    //    assignment      : ASSIGN variable PIPE type PIPE expr
    public Node Assignment()
    {
        Eat(Lexer.Token.Type.ASSIGN, Lexer.Token.Type.CALL, Lexer.Token.Type.CONDITION, Lexer.Token.Type.TASK, Lexer.Token.Type.LOOP, Lexer.Token.Type.RETURN);
        Node variable = Variable();
        Eat(Lexer.Token.Type.PIPE);
        Node type = Type();
        Node value = Expression();
        return new Assignment(variable, type, value);
    }

    //    conditional     : CONDITION LPARENTHESES expr RPARENTHESES LBRACKET block RBRACKET
    public Node Conditional()
    {
        Eat(Lexer.Token.Type.CONDITION, Lexer.Token.Type.CALL, Lexer.Token.Type.ASSIGN, Lexer.Token.Type.TASK, Lexer.Token.Type.LOOP, Lexer.Token.Type.RETURN);
        Eat(Lexer.Token.Type.LPARENTHESES);
        Node condition = Expression();
        Eat(Lexer.Token.Type.RPARENTHESES);
        Eat(Lexer.Token.Type.LBRACKET);
        Node body = Block();
        Eat(Lexer.Token.Type.RBRACKET);
        return new Conditional(condition, body);
    }

    //    task_definition : TASK ( RETURNS | empty ) variable LPARENTHESES ( empty | type PIPE ID ( COMMA type PIPE ID )* ) RPARENTHESES LBRACKET block RBRACKET
    public Node TaskDefinition()
    {
        Eat(Lexer.Token.Type.TASK, Lexer.Token.Type.CALL, Lexer.Token.Type.ASSIGN, Lexer.Token.Type.CONDITION, Lexer.Token.Type.LOOP, Lexer.Token.Type.RETURN);
        boolean returns = getCurrentToken().type == Lexer.Token.Type.RETURNS;
        if(returns)
            Eat(Lexer.Token.Type.RETURNS);
        Node name = Variable();
        Eat(Lexer.Token.Type.LPARENTHESES);
        LinkedHashMap<Node, Node> parameterTypeDictionary = new LinkedHashMap<>();
        if(getCurrentToken().type != Lexer.Token.Type.RPARENTHESES)
        {
            Node type = Type();
            Node variable = Variable();
            parameterTypeDictionary.put(variable, type);
        }
        while (getCurrentToken().type == Lexer.Token.Type.COMMA)
        {
            Eat(Lexer.Token.Type.COMMA);
            Node type = Type();
            Eat(Lexer.Token.Type.PIPE);
            Node variable = Variable();
            parameterTypeDictionary.put(variable, type);
        }
        Eat(Lexer.Token.Type.RPARENTHESES);
        Eat(Lexer.Token.Type.LBRACKET);
        Node body = Block();
        Eat(Lexer.Token.Type.RBRACKET);
        return new TaskDefinition(name, parameterTypeDictionary, returns, body);
    }

    //    comparison      : COMPARISON (expr ( EQUAL | GREATERTHAN | LESSTHAN ) expr)
    public Node Comparison()
    {
        Eat(Lexer.Token.Type.COMPARISON);
        Eat(Lexer.Token.Type.LPARENTHESES);
        Node left = Expression();
        Comparison.Operator operator;
        switch (getCurrentToken().type)
        {
            case EQUAL:
                Eat(Lexer.Token.Type.EQUAL);
                operator = Comparison.Operator.EQUAL;
                break;
            case GREATERTHAN:
                Eat(Lexer.Token.Type.GREATERTHAN);
                operator = Comparison.Operator.GREATERTHAN;
                break;
            case LESSTHAN:
                Eat(Lexer.Token.Type.LESSTHAN);
                operator = Comparison.Operator.LESSTHAN;
                break;
            default:
                Error(Lexer.Token.Type.EQUAL, Lexer.Token.Type.GREATERTHAN, Lexer.Token.Type.LESSTHAN);
                return null;
        }
        Node right = Expression();
        Eat(Lexer.Token.Type.RPARENTHESES);
        return new Comparison(left, operator, right);
    }

    //    loop            : LOOP LPARENTHESES condition RPARENTHESES LBRACKET block RBRACKET
    public Node Loop()
    {
        Eat(Lexer.Token.Type.LOOP, Lexer.Token.Type.CALL, Lexer.Token.Type.ASSIGN, Lexer.Token.Type.TASK, Lexer.Token.Type.CONDITION, Lexer.Token.Type.RETURN);
        Eat(Lexer.Token.Type.LPARENTHESES);
        Node condition = Expression();
        Eat(Lexer.Token.Type.RPARENTHESES);
        Eat(Lexer.Token.Type.LBRACKET);
        Node body = Block();
        Eat(Lexer.Token.Type.RBRACKET);
        return new Loop(condition, body);
    }

    //    return          : RETURN expr
    public Node Return()
    {
        Eat(Lexer.Token.Type.RETURN, Lexer.Token.Type.LOOP, Lexer.Token.Type.CALL, Lexer.Token.Type.ASSIGN, Lexer.Token.Type.TASK, Lexer.Token.Type.CONDITION);
        return new Return(Expression());
    }

    //    variable        : ID
    public Node Variable() {
        String name = getCurrentToken().value;
        Eat(Lexer.Token.Type.ID);
        return new Name(name);
    }

    //    expr            : term (( PLUS | MINUS ) term )* | STRING | tralse
    public Node Expression()
    {
        if(!new ArrayList<Lexer.Token.Type>()
        {{
            add(Lexer.Token.Type.STRING);
            add(Lexer.Token.Type.TRUE);
            add(Lexer.Token.Type.FALSE);
            add(Lexer.Token.Type.COMPARISON);
        }}.contains(getCurrentToken().type)) {
            Node node = Term();
            while (getCurrentToken().type == Lexer.Token.Type.ADD || getCurrentToken().type == Lexer.Token.Type.SUBTRACT) {
                BinaryOperator.Operator operator;
                if (getCurrentToken().type == Lexer.Token.Type.ADD) {
                    Eat(Lexer.Token.Type.ADD, Lexer.Token.Type.SUBTRACT);
                    operator = BinaryOperator.Operator.PLUS;
                } else if (getCurrentToken().type == Lexer.Token.Type.SUBTRACT) {
                    Eat(Lexer.Token.Type.SUBTRACT, Lexer.Token.Type.ADD);
                    operator = BinaryOperator.Operator.MINUS;
                } else {
                    Error(Lexer.Token.Type.ADD, Lexer.Token.Type.SUBTRACT);
                    return null;
                }
                node = new BinaryOperator(node, operator, Term());
            }
            return node;
        }
        else {
            if(getCurrentToken().type == Lexer.Token.Type.STRING)
            {
                String value = getCurrentToken().value.replaceAll("(?<!\\\\)\"", "").replaceAll("\\\\\"", "\"");
                Eat(Lexer.Token.Type.STRING);
                return new StringNode(value);
            }
            return Tralse();
        }
    }

    //    term            : factor ((MULTIPLY | DIVIDE) term)*
    public Node Term()
    {
        Node node = Factor();
        while (getCurrentToken().type == Lexer.Token.Type.MULTIPLY || getCurrentToken().type == Lexer.Token.Type.DIVIDE)
        {
            BinaryOperator.Operator operator;
            if(getCurrentToken().type == Lexer.Token.Type.MULTIPLY)
            {
                Eat(Lexer.Token.Type.MULTIPLY, Lexer.Token.Type.DIVIDE);
                operator = BinaryOperator.Operator.MULTIPLY;
            }
            else if(getCurrentToken().type == Lexer.Token.Type.DIVIDE)
            {
                Eat(Lexer.Token.Type.DIVIDE, Lexer.Token.Type.MULTIPLY);
                operator = BinaryOperator.Operator.DIVIDE;
            }
            else
            {
                Error(Lexer.Token.Type.MULTIPLY, Lexer.Token.Type.DIVIDE);
                return null;
            }
            node = new BinaryOperator(node, operator, Factor());
        }
        return node;
    }

    //    factor          : INTEGER | LPARENTHESES expr RPARENTHESES | variable | task_call
    public Node Factor()
    {
        Lexer.Token token = getCurrentToken();
        switch (getCurrentToken().type) {
            case NUMBER:
                Eat(Lexer.Token.Type.NUMBER, Lexer.Token.Type.LPARENTHESES);
                return new Number(Integer.parseInt(token.value));
            case LPARENTHESES:
                Eat(Lexer.Token.Type.LPARENTHESES, Lexer.Token.Type.NUMBER);
                Node node = Expression();
                Eat(Lexer.Token.Type.RPARENTHESES);
                return node;
            case ID:
                return Variable();
            case CALL:
                return TaskCall();
            default:
                Error(Lexer.Token.Type.NUMBER, Lexer.Token.Type.LPARENTHESES);
                return null;
        }
    }

    //    tralse          : TRUE | FALSE | condition
    public Node Tralse()
    {
        if(getCurrentToken().type == Lexer.Token.Type.TRUE)
        {
            Eat(Lexer.Token.Type.TRUE);
            return new Tralse(true);
        }
        if(getCurrentToken().type == Lexer.Token.Type.FALSE)
        {
            Eat(Lexer.Token.Type.FALSE);
            return new Tralse(false);
        }
        if(getCurrentToken().type == Lexer.Token.Type.COMPARISON)
        {
            return Comparison();
        }
        Error(Lexer.Token.Type.TRUE,Lexer.Token.Type.FALSE, Lexer.Token.Type.COMPARISON);
        return null;
    }

    //    type            : STRING_TYPE | INTEGER_TYPE | TRALSE_TYPE
    public Node Type()
    {
        switch (getCurrentToken().type)
        {
            case STRING_TYPE:
                Eat(Lexer.Token.Type.STRING_TYPE);
                return new com.wagologies.Parser.Nodes.Type(Type.STRING);
            case NUMBER_TYPE:
                Eat(Lexer.Token.Type.NUMBER_TYPE);
                return new com.wagologies.Parser.Nodes.Type(Type.NUMBER);
            case TRALSE_TYPE:
                Eat(Lexer.Token.Type.TRALSE_TYPE);
                return new com.wagologies.Parser.Nodes.Type(Type.TRALSE);
            default:
                Error(Lexer.Token.Type.STRING_TYPE, Lexer.Token.Type.NUMBER_TYPE, Lexer.Token.Type.TRALSE_TYPE);
        }
        return null;
    }

    public static AST Parse(List<Lexer.Token> tokens)
    {
        Parser parser = new Parser(tokens);
        return parser.Parse();
    }

    public enum Type {
        STRING(String.class),
        NUMBER(Integer.class),
        TRALSE(Boolean.class);

        public Class<?> clazz;
        Type(Class<?> clazz) {
            this.clazz = clazz;
        }
    }
    public static String formatNumber(int i)
    {
        String numberFormatted = String.valueOf(i);
        if (i % 100 >= 11 && i % 100 <= 20) {
            numberFormatted += "th";
        }
        else if (i % 10 == 1) {
            numberFormatted += "st";
        }
        else if (i % 10 == 2) {
            numberFormatted += "nd";
        }
        else if (i % 10 == 3)
        {
            numberFormatted += "rd";
        } else {
            numberFormatted += "th";
        }
        return numberFormatted;
    }
}
