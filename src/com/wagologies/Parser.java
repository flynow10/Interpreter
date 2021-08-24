package com.wagologies;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Parser {

//    ast             : block
//    task_definition : TASK ( RETURNS | empty ) variable LPARENTHESES ( empty | ID ( COMMA ID )* ) RPARENTHESES LBRACKET block RBRACKET
//    task_call       : CALL variable LPARENTHESES ( expr COMMA )* RPARENTHESES
//    block           : ( statement )*
//    statement       : (( task_call | assignment | return ) EOL ) | ( conditional | task_definition | loop )
//    assignment      : ASSIGN variable PIPE expr
//    return          : RETURN expr
//    expr            : term (( PLUS | MINUS ) term )*
//    term            : factor (( MULTIPLY | DIVIDE ) term )*
//    factor          : INTEGER | LPARENTHESES expr RPARENTHESES | variable | task_call
//    variable        : ID
//    conditional     : CONDITION LPARENTHESES condition RPARENTHESES LBRACKET block RBRACKET
//    condition       : expr ( EQUAL | GREATERTHAN | LESSTHAN ) expr
//    loop            : LOOP LPARENTHESES condition RPARENTHESES LBRACKET block RBRACKET
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
        return new AST.Block(nodes);
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
        return new AST.TaskCall(variable, parameters);
    }

    //    assignment      : ASSIGN variable PIPE expr
    public Node Assignment()
    {
        Eat(Lexer.Token.Type.ASSIGN, Lexer.Token.Type.CALL, Lexer.Token.Type.CONDITION, Lexer.Token.Type.TASK, Lexer.Token.Type.LOOP, Lexer.Token.Type.RETURN);
        Node variable = Variable();
        Eat(Lexer.Token.Type.PIPE);
        Node value = Expression();
        return new AST.Assignment(variable, value);
    }

    //    conditional     : CONDITION LPARENTHESES condition RPARENTHESES LBRACKET block RBRACKET
    public Node Conditional()
    {
        Eat(Lexer.Token.Type.CONDITION, Lexer.Token.Type.CALL, Lexer.Token.Type.ASSIGN, Lexer.Token.Type.TASK, Lexer.Token.Type.LOOP, Lexer.Token.Type.RETURN);
        Eat(Lexer.Token.Type.LPARENTHESES);
        Node condition = Condition();
        Eat(Lexer.Token.Type.RPARENTHESES);
        Eat(Lexer.Token.Type.LBRACKET);
        Node body = Block();
        Eat(Lexer.Token.Type.RBRACKET);
        return new AST.Conditional(condition, body);
    }

    //    task_definition : TASK variable LPARENTHESES ( empty | ID (COMMA ID)* ) RPARENTHESES LBRACKET block RBRACKET
    public Node TaskDefinition()
    {
        Eat(Lexer.Token.Type.TASK, Lexer.Token.Type.CALL, Lexer.Token.Type.ASSIGN, Lexer.Token.Type.CONDITION, Lexer.Token.Type.LOOP, Lexer.Token.Type.RETURN);
        boolean returns = getCurrentToken().type == Lexer.Token.Type.RETURNS;
        if(returns)
            Eat(Lexer.Token.Type.RETURNS);
        Node name = Variable();
        Eat(Lexer.Token.Type.LPARENTHESES);
        List<Node> parameters = new ArrayList<>();
        if(getCurrentToken().type != Lexer.Token.Type.RPARENTHESES)
        {
            parameters.add(Variable());
        }
        while (getCurrentToken().type == Lexer.Token.Type.COMMA)
        {
            Eat(Lexer.Token.Type.COMMA);
            parameters.add(Variable());
        }
        Eat(Lexer.Token.Type.RPARENTHESES);
        Eat(Lexer.Token.Type.LBRACKET);
        Node body = Block();
        Eat(Lexer.Token.Type.RBRACKET);
        return new AST.TaskDefinition(name, parameters, returns, body);
    }

    //    condition       : expr comparison expr
    public Node Condition()
    {
        Node left = Expression();
        AST.Comparison.Operator operator;
        switch (getCurrentToken().type)
        {
            case EQUAL:
                Eat(Lexer.Token.Type.EQUAL);
                operator = AST.Comparison.Operator.EQUAL;
                break;
            case GREATERTHAN:
                Eat(Lexer.Token.Type.GREATERTHAN);
                operator = AST.Comparison.Operator.GREATERTHAN;
                break;
            case LESSTHAN:
                Eat(Lexer.Token.Type.LESSTHAN);
                operator = AST.Comparison.Operator.LESSTHAN;
                break;
            default:
                Error(Lexer.Token.Type.EQUAL, Lexer.Token.Type.GREATERTHAN, Lexer.Token.Type.LESSTHAN);
                return null;
        }
        Node right = Expression();
        return new AST.Comparison(left, operator, right);
    }

    //    loop            : LOOP LPARENTHESES condition RPARENTHESES LBRACKET block RBRACKET
    public Node Loop()
    {
        Eat(Lexer.Token.Type.LOOP, Lexer.Token.Type.CALL, Lexer.Token.Type.ASSIGN, Lexer.Token.Type.TASK, Lexer.Token.Type.CONDITION, Lexer.Token.Type.RETURN);
        Eat(Lexer.Token.Type.LPARENTHESES);
        Node condition = Condition();
        Eat(Lexer.Token.Type.RPARENTHESES);
        Eat(Lexer.Token.Type.LBRACKET);
        Node body = Block();
        Eat(Lexer.Token.Type.RBRACKET);
        return new AST.Loop(condition, body);
    }

    //    return          : RETURN expr
    public Node Return()
    {
        Eat(Lexer.Token.Type.RETURN, Lexer.Token.Type.LOOP, Lexer.Token.Type.CALL, Lexer.Token.Type.ASSIGN, Lexer.Token.Type.TASK, Lexer.Token.Type.CONDITION);
        return new AST.Return(Expression());
    }

    //    variable        : ID
    public Node Variable() {
        String name = getCurrentToken().value;
        Eat(Lexer.Token.Type.ID);
        return new AST.Name(name);
    }

    //    expr            : term ((PLUS | MINUS) term)*
    public Node Expression()
    {
        Node node = Term();
        while (getCurrentToken().type == Lexer.Token.Type.ADD || getCurrentToken().type == Lexer.Token.Type.SUBTRACT)
        {
            AST.BinaryOperator.Operator operator;
            if(getCurrentToken().type == Lexer.Token.Type.ADD)
            {
                Eat(Lexer.Token.Type.ADD, Lexer.Token.Type.SUBTRACT);
                operator = AST.BinaryOperator.Operator.PLUS;
            }
            else if(getCurrentToken().type == Lexer.Token.Type.SUBTRACT)
            {
                Eat(Lexer.Token.Type.SUBTRACT, Lexer.Token.Type.ADD);
                operator = AST.BinaryOperator.Operator.MINUS;
            }
            else
            {
                Error(Lexer.Token.Type.ADD, Lexer.Token.Type.SUBTRACT);
                return null;
            }
            node = new AST.BinaryOperator(node, operator, Term());
        }
        return node;
    }

    //    term            : factor ((MULTIPLY | DIVIDE) term)*
    public Node Term()
    {
        Node node = Factor();
        while (getCurrentToken().type == Lexer.Token.Type.MULTIPLY || getCurrentToken().type == Lexer.Token.Type.DIVIDE)
        {
            AST.BinaryOperator.Operator operator;
            if(getCurrentToken().type == Lexer.Token.Type.MULTIPLY)
            {
                Eat(Lexer.Token.Type.MULTIPLY, Lexer.Token.Type.DIVIDE);
                operator = AST.BinaryOperator.Operator.MULTIPLY;
            }
            else if(getCurrentToken().type == Lexer.Token.Type.DIVIDE)
            {
                Eat(Lexer.Token.Type.DIVIDE, Lexer.Token.Type.MULTIPLY);
                operator = AST.BinaryOperator.Operator.DIVIDE;
            }
            else
            {
                Error(Lexer.Token.Type.MULTIPLY, Lexer.Token.Type.DIVIDE);
                return null;
            }
            node = new AST.BinaryOperator(node, operator, Factor());
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
                return new AST.Number(Integer.parseInt(token.value));
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

    public static AST Parse(List<Lexer.Token> tokens)
    {
        Parser parser = new Parser(tokens);
        return parser.Parse();
    }

    public interface Node {
        Object Walk(Scope scope);
        void Output(int level);
    }

    public static class AST implements Node {

        public Node start;

        public AST(Node start)
        {
            this.start = start;
        }

        @Override
        public Object Walk(Scope scope) {
            Object node = start.Walk(scope);
            if(node != null)
            {
                throw new RuntimeException("Return statement can only be used inside of a function!");
            }
            return null;
        }

        @Override
        public void Output(int level) {
            System.out.println("AST");
            start.Output(level+1);
        }

        public static class TaskDefinition implements Node {

            public Node name;
            public List<Node> parameters;
            public Node body;
            public boolean returns;

            public TaskDefinition(Node name, List<Node> parameters, boolean returns, Node body)
            {
                this.name = name;
                this.parameters = parameters;
                this.returns = returns;
                this.body = body;
            }

            @Override
            public Object Walk(Scope scope) {
                String[] parameters = new String[this.parameters.size()];
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = ((Name)this.parameters.get(i)).name;
                }
                scope.functions.put(((Name)name).name,new Scope.FunctionData(body, returns, parameters));
                return null;
            }

            @Override
            public void Output(int level) {
                StringBuilder output = new StringBuilder();
                for (int i = 0; i < level; i++) {
                    output.append("  ");
                }
                System.out.println(output + "Task Definition" + (returns ? " (returns)" : ""));
                if(parameters.size() > 0) {
                    System.out.println(output + "  Parameters:");
                }
                for (Node parameter : parameters) {
                    parameter.Output(level + 2);
                }
                System.out.println(output + "  Body:");
                body.Output(level + 2);
            }
        }

        public static class Block implements Node {

            public List<Node> nodes;
            public Block(List<Node> nodes)
            {
                this.nodes = nodes;
            }

            @Override
            public Object Walk(Scope scope) {
                for (Node node : nodes) {
                    Object returnStatement = node.Walk(scope);
                    if(returnStatement != null) {
                        if (!(node instanceof TaskCall)) {
                            return returnStatement;
                        }
                    }
                }
                return null;
            }

            @Override
            public void Output(int level) {
                StringBuilder output = new StringBuilder();
                for (int i = 0; i < level; i++) {
                    output.append("  ");
                }
                if(nodes.size() < 1)
                {
                    System.out.println(output + "Empty");
                }
                for (Node node : nodes) {
                    node.Output(level);
                }
            }
        }

        public static class BinaryOperator implements Node {
            public Node left;
            public Operator operator;
            public Node right;

            public BinaryOperator(Node left, Operator operator, Node right)
            {
                this.left = left;
                this.operator = operator;
                this.right = right;
            }

            @Override
            public Object Walk(Scope scope) {
                int leftNumber = (int) left.Walk(scope);
                int rightNumber = (int) right.Walk(scope);

                if(operator == Operator.PLUS)
                {
                    return leftNumber + rightNumber;
                }
                else if(operator == Operator.MINUS)
                {
                    return leftNumber - rightNumber;
                }
                else if(operator == Operator.MULTIPLY)
                {
                    return leftNumber * rightNumber;
                }
                else
                {
                    return leftNumber / rightNumber;
                }
            }

            @Override
            public void Output(int level) {
                StringBuilder output = new StringBuilder();
                for (int i = 0; i < level; i++) {
                    output.append("  ");
                }
                System.out.println(output + "Binary Operator; Operator: " + operator);
                System.out.println(output + "  Left");
                left.Output(level + 2);
                System.out.println(output + "  Right:");
                right.Output(level + 2);
            }

            public enum Operator {
                PLUS,
                MINUS,
                MULTIPLY,
                DIVIDE
            }
        }

        public static class Comparison implements Node {

            public Node left;
            public Operator operator;
            public Node right;

            public Comparison(Node left, Operator operator, Node right)
            {
                this.left = left;
                this.operator = operator;
                this.right = right;
            }

            @Override
            public Object Walk(Scope scope) {
                Object leftObject = this.left.Walk(scope);
                Object rightObject = this.right.Walk(scope);
                if(!(leftObject instanceof Integer) || !(rightObject instanceof Integer))
                {
                    throw new RuntimeException("Incomparable Types! Comparison must be between integers");
                }
                int left = (int) leftObject;
                int right = (int) rightObject;
                switch (operator)
                {
                    case EQUAL:
                        return left == right;
                    case GREATERTHAN:
                        return left > right;
                    case LESSTHAN:
                        return left < right;
                }
                return null;
            }

            @Override
            public void Output(int level) {
                StringBuilder output = new StringBuilder();
                for (int i = 0; i < level; i++) {
                    output.append("  ");
                }
                System.out.println(output + "Comparison; Operator: " + operator);
                System.out.println(output + "  Left:");
                left.Output(level + 2);
                System.out.println(output + "  Right:");
                right.Output(level + 2);
            }

            public enum Operator {
                EQUAL,
                GREATERTHAN,
                LESSTHAN
            }
        }

        public static class Conditional implements Node {

            public Node condition;
            public Node body;

            public Conditional(Node condition, Node body)
            {
                this.condition = condition;
                this.body = body;
            }

            @Override
            public Object Walk(Scope scope) {
                Object conditionObject = condition.Walk(scope);
                if(!(conditionObject instanceof Boolean))
                {
                    throw new RuntimeException("Incomparable Types! Condition must be a boolean");
                }
                boolean condition = (boolean) conditionObject;
                if(condition)
                {
                    Scope innerScope = new Scope(scope.global, scope);
                    return body.Walk(innerScope);
                }
                return null;
            }

            @Override
            public void Output(int level) {
                StringBuilder output = new StringBuilder();
                for (int i = 0; i < level; i++) {
                    output.append("  ");
                }
                System.out.println(output + "Conditional:");
                System.out.println(output + "  Condition:");
                condition.Output(level + 2);
                System.out.println(output + "  Body:");
                body.Output(level + 2);
            }
        }

        public static class Assignment implements Node {

            public Node name;
            public Node value;

            public Assignment(Node name, Node value)
            {
                this.name = name;
                this.value = value;
            }

            @Override
            public Object Walk(Scope scope) {
                String name = ((Name)this.name).name;
                Scope.Variable variable = scope.getVariable(name);
                if(variable != null)
                {
                    variable.value = (Integer) value.Walk(scope);
                }
                scope.variables.put(name, new Scope.Variable((Integer) value.Walk(scope)));
                return null;
            }

            @Override
            public void Output(int level) {
                StringBuilder output = new StringBuilder();
                for (int i = 0; i < level; i++) {
                    output.append("  ");
                }
                System.out.println(output + "Assignment to " + ((Name)name).name);
                value.Output(level + 1);
            }
        }

        public static class Number implements Node {

            public int number;

            public Number(int number)
            {
                this.number = number;
            }

            @Override
            public Object Walk(Scope scope) {
                return number;
            }

            @Override
            public void Output(int level) {
                StringBuilder output = new StringBuilder();
                for (int i = 0; i < level; i++) {
                    output.append("  ");
                }
                System.out.println(output + "Number: " + number);
            }
        }

        public static class Name implements Node {

            public String name;

            public Name(String name)
            {
                this.name = name;
            }

            @Override
            public Object Walk(Scope scope) {
                Scope.Variable variable = scope.getVariable(name);
                if(variable == null)
                    throw new RuntimeException("Variable \"" + name + "\" does not exist!");
                else
                {
                    return variable.value;
                }
            }

            @Override
            public void Output(int level) {
                StringBuilder output = new StringBuilder();
                for (int i = 0; i < level; i++) {
                    output.append("  ");
                }
                System.out.println(output + "Name: " + name);
            }
        }

        public static class TaskCall implements Node {

            public Node variable;
            public List<Node> parameters;

            public TaskCall(Node variable, List<Node> parameters) {
                this.variable = variable;
                this.parameters = parameters;
            }

            @Override
            public Object Walk(Scope scope) {
                Scope.FunctionData functionData = scope.getFunction(((Name)variable).name);
                if(functionData == null)
                {
                    throw new RuntimeException("Function \"" + ((Name)variable).name + "\" does not exist!");
                }
                if(parameters.size() != functionData.parameters.size())
                {
                    throw new RuntimeException("Trying to call function with incorrect parameters!");
                }
                Scope taskScope = new Scope(scope.global, null);
                for (int i = 0; i < functionData.parameters.size(); i++) {
                    taskScope.variables.put(functionData.parameters.get(i), new Scope.Variable((Integer) parameters.get(i).Walk(scope)));
                }
                Object returnStatement = functionData.ast.Walk(taskScope);
                if(!functionData.returns && returnStatement != null)
                    throw new RuntimeException("Function \"" + ((Name)variable).name + "\" is not specified to return, but a return value was provided!");
                if(functionData.returns && returnStatement == null)
                    throw new RuntimeException("Function \"" + ((Name)variable).name + "\" is specified to return, but no return value was provided!");
                return returnStatement;
            }

            @Override
            public void Output(int level) {
                StringBuilder output = new StringBuilder();
                for (int i = 0; i < level; i++) {
                    output.append("  ");
                }
                System.out.println(output + "Task Call: " + ((Name)variable).name);
                if(parameters.size() > 0) {
                    System.out.println(output + "  Parameters:");
                }
                for (Node parameter : parameters) {
                    parameter.Output(level + 2);
                }
            }
        }

        public static class Loop implements Node {
            public Node condition;
            public Node body;

            public Loop(Node condition, Node body)
            {
                this.condition = condition;
                this.body = body;
            }

            @Override
            public Object Walk(Scope scope) {
                Object conditionObject = this.condition.Walk(scope);
                if(!(conditionObject instanceof Boolean))
                {
                    throw new RuntimeException("Incomparable Types! Condition must be a boolean");
                }
                boolean condition = (boolean) conditionObject;
                int index = 0;
                while (condition)
                {
                    Scope innerScope = new Scope(scope.global, scope);
                    innerScope.variables.put("index", new Scope.Variable(index));
                    Object returnStatement = body.Walk(innerScope);
                    if(returnStatement != null)
                    {
                        return returnStatement;
                    }
                    conditionObject = this.condition.Walk(scope);
                    if(!(conditionObject instanceof Boolean))
                    {
                        throw new RuntimeException("Incomparable Types! Condition must be a boolean");
                    }
                    condition = (boolean) conditionObject;
                    index++;
                }
                return null;
            }

            @Override
            public void Output(int level) {
                StringBuilder output = new StringBuilder();
                for (int i = 0; i < level; i++) {
                    output.append("  ");
                }
                System.out.println(output + "Loop:");
                condition.Output(level+1);
                body.Output(level+1);
            }
        }

        public static class Output implements Node {

            public Type type;

            public Output()
            {
                type = Type.CHAR;
            }
            public Output(Type type)
            {
                this.type = type;
            }

            public enum Type {
                CHAR,
                INT
            }
            @Override
            public Object Walk(Scope scope) {
                switch (type)
                {
                    case CHAR:
                        System.out.print((char)scope.getVariable("output").value);
                        break;
                    case INT:
                        System.out.print(scope.getVariable("output").value);
                        break;
                }
                return null;
            }

            @Override
            public void Output(int level) {

            }
        }

        public static class Input implements Node {
            public Input()
            {

            }
            @Override
            public Object Walk(Scope scope) {
                try {
                    return (int)(char) System.in.read();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read input!");
                }
            }

            @Override
            public void Output(int level) {

            }
        }

        public static class Random implements Node {
            public Random()
            {

            }

            @Override
            public Object Walk(Scope scope) {
                return (int) (Math.random()*(scope.getVariable("max").value-scope.getVariable("min").value))+scope.getVariable("min").value;
            }

            @Override
            public void Output(int level) {

            }
        }

        public static class Return implements Node {

            public Node expression;

            public Return(Node expression) {
                this.expression = expression;
            }

            @Override
            public Object Walk(Scope scope) {
                return expression.Walk(scope);
            }

            @Override
            public void Output(int level) {
                StringBuilder output = new StringBuilder();
                for (int i = 0; i < level; i++) {
                    output.append("  ");
                }
                System.out.println(output + "Return:");
                expression.Output(level + 1);
            }
        }
    }
}
