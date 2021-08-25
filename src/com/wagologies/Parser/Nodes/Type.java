package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Parser.Parser;
import com.wagologies.Scope;

public class Type implements Node {

    public Parser.Type type;

    public Type(Parser.Type type)
    {
        this.type = type;
    }
    @Override
    public Object Walk(Scope scope) {
        return null;
    }

    @Override
    public void Output(int level) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < level; i++) {
            output.append("  ");
        }
        System.out.println(output + "Type: " + type.name());
    }
}