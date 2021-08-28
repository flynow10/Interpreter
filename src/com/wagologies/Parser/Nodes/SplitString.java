package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Scope;

public class SplitString implements Node {
    @Override
    public Object Walk(Scope scope) {
        return (int) ((String)scope.getVariable("string").value).charAt((int) scope.getVariable("index").value);
    }

    @Override
    public void Output(int level) {

    }
}
