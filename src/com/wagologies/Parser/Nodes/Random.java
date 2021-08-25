package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Scope;

public class Random implements Node {
    public Random()
    {

    }

    @Override
    public Object Walk(Scope scope) {
        return (int) (Math.random()*((int)scope.getVariable("max").value-(int)scope.getVariable("min").value))+(int)scope.getVariable("min").value;
    }

    @Override
    public void Output(int level) {

    }
}
