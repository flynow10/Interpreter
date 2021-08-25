package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Scope;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AST implements Node {

    public Node start;

    public AST(Node start)
    {
        this.start = start;
    }

    @Override
    public Object Walk(Scope scope) {
        Object node = start.Walk(scope);
        if(node != null)
        {
            throw new RuntimeException("Return statement can only be used inside of a function!");
        }
        return null;
    }

    @Override
    public void Output(int level) {
        System.out.println("AST");
        start.Output(level+1);
    }
}