package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Scope;

public class Number implements Node {

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