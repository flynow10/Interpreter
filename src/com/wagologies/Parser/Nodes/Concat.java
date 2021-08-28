package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Scope;

public class Concat implements Node {
    @Override
    public Object Walk(Scope scope) {
        return scope.getVariable("string").value + (String)scope.getVariable("joiningString").value;
    }

    @Override
    public void Output(int level) {

    }
}
