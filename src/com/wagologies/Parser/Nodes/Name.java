package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Scope;

public class Name implements Node {

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
