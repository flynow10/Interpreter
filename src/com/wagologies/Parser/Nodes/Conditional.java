package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Parser.Parser;
import com.wagologies.Scope;

public class Conditional implements Node {

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
        if(!(Parser.Type.TRALSE.clazz.isInstance(conditionObject)))
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
