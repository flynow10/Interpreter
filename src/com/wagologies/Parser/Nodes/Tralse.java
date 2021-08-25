package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Scope;

public class Tralse implements Node {

    public boolean value;

    public Tralse(boolean value) {
        this.value = value;
    }

    @Override
    public Object Walk(Scope scope) {
        return value;
    }

    @Override
    public void Output(int level) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < level; i++) {
            output.append("  ");
        }
        System.out.println(output + "Tralse: " + value);
    }
}
