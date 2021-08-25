package com.wagologies.Parser.Nodes;


import com.wagologies.Parser.Node;
import com.wagologies.Scope;

public class Comparison implements Node {

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
