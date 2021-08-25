package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Parser.Parser;
import com.wagologies.Scope;

public class Loop implements Node {
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
        if(!(Parser.Type.TRALSE.clazz.isInstance(conditionObject)))
        {
            throw new RuntimeException("Incomparable Types! Condition must be a boolean");
        }
        boolean condition = (boolean) conditionObject;
        int index = 0;
        while (condition)
        {
            Scope innerScope = new Scope(scope.global, scope);
            innerScope.variables.put("index", new Scope.Variable(index, Parser.Type.NUMBER));
            Object returnStatement = body.Walk(innerScope);
            if(returnStatement != null)
            {
                return returnStatement;
            }
            conditionObject = this.condition.Walk(scope);
            if(!(Parser.Type.TRALSE.clazz.isInstance(conditionObject)))
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