package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Scope;

public class Output implements Node {

    @Override
    public Object Walk(Scope scope) {
        System.out.print(((String)scope.getVariable("output").value).replaceAll("(?<!\\\\)\\\\n", Character.toString((char)10)));
        return null;
    }

    @Override
    public void Output(int level) {

    }
}