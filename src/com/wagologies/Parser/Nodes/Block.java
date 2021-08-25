package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Scope;

import java.util.List;

public class Block implements Node {

    public List<Node> nodes;
    public Block(List<Node> nodes)
    {
        this.nodes = nodes;
    }

    @Override
    public Object Walk(Scope scope) {
        for (Node node : nodes) {
            Object returnStatement = node.Walk(scope);
            if(returnStatement != null) {
                if (!(node instanceof TaskCall)) {
                    return returnStatement;
                }
            }
        }
        return null;
    }

    @Override
    public void Output(int level) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < level; i++) {
            output.append("  ");
        }
        if(nodes.size() < 1)
        {
            System.out.println(output + "Empty");
        }
        for (Node node : nodes) {
            node.Output(level);
        }
    }
}
