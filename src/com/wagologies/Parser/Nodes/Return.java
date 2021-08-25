package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Scope;

public class Return implements Node {

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
