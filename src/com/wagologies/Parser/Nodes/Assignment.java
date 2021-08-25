package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Scope;

public class Assignment implements Node {

    public Node name;
    public Node value;
    public Node type;

    public Assignment(Node name, Node type, Node value)
    {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public Object Walk(Scope scope) {
        String name = ((Name)this.name).name;
        Scope.Variable variable = scope.getVariable(name);
        if(variable != null)
        {
            variable.type = ((Type)type).type;
            variable.value = value.Walk(scope);
        }

        scope.variables.put(name, new Scope.Variable(value.Walk(scope), ((Type) type).type));
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
