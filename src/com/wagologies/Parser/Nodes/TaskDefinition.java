package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Scope;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TaskDefinition implements Node {

    public Node name;
    public LinkedHashMap<Node, Node> parameterTypeDictionary;
    public Node body;
    public boolean returns;

    public TaskDefinition(Node name, LinkedHashMap<Node, Node> parameterTypeDictionary, boolean returns, Node body)
    {
        this.name = name;
        this.parameterTypeDictionary = parameterTypeDictionary;
        this.returns = returns;
        this.body = body;
    }

    @Override
    public Object Walk(Scope scope) {
        Scope.Variable[] parameters = new Scope.Variable[this.parameterTypeDictionary.size()];
        int index = 0;
        for (Map.Entry<Node, Node> nameTypeEntry : parameterTypeDictionary.entrySet()) {
            if(Arrays.stream(parameters).anyMatch(variable -> variable != null && variable.value.equals(((Name)nameTypeEntry.getKey()).name)))
            {
                throw new RuntimeException("You can't have multiple parameters with the same name!");
            }
            parameters[index] = new Scope.Variable(((Name)nameTypeEntry.getKey()).name, ((Type)nameTypeEntry.getValue()).type);
            index ++;
        }
        scope.functions.put(((Name)name).name,new Scope.FunctionData(body, returns, parameters));
        return null;
    }

    @Override
    public void Output(int level) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < level; i++) {
            output.append("  ");
        }
        System.out.println(output + "Task Definition" + (returns ? " (returns)" : ""));
        if(parameterTypeDictionary.size() > 0) {
            System.out.println(output + "  Parameters:");
        }
        for (Map.Entry<Node, Node> parameter : parameterTypeDictionary.entrySet()) {
            parameter.getValue().Output(level + 2);
            parameter.getKey().Output(level + 2);
        }
        System.out.println(output + "  Body:");
        body.Output(level + 2);
    }
}