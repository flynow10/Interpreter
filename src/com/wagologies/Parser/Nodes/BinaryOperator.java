package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Scope;

public class BinaryOperator implements Node {
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
        Object leftObject = left.Walk(scope);
        if(!(leftObject instanceof Integer))
        {
            throw new RuntimeException("Incompatible Types! Must be a number!");
        }
        int leftNumber = (int) leftObject;
        Object rightObject = right.Walk(scope);
        if(!(rightObject instanceof Integer))
        {
            throw new RuntimeException("Incompatible Types! Must be a number!");
        }
        int rightNumber = (int) rightObject;

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
