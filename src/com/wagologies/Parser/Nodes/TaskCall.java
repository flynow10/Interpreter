package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Parser.Parser;
import com.wagologies.Scope;

import java.util.List;

public class TaskCall implements Node {

    public Node variable;
    public List<Node> parameters;

    public TaskCall(Node variable, List<Node> parameters) {
        this.variable = variable;
        this.parameters = parameters;
    }

    @Override
    public Object Walk(Scope scope) {
        Scope.FunctionData functionData = scope.getFunction(((Name)variable).name);
        if(functionData == null)
        {
            throw new RuntimeException("Function \"" + ((Name)variable).name + "\" does not exist!");
        }
        if(parameters.size() != functionData.parameters.length)
        {
            throw new RuntimeException("Trying to call function with incorrect parameters!");
        }
        Scope taskScope = new Scope(scope.global, null);
        for (int i = 0; i < functionData.parameters.length; i++) {
            Object value = parameters.get(i).Walk(scope);
            if(!(functionData.parameters[i].type.clazz.isInstance(value)))
            {
                throw new RuntimeException("Incompatible Types! The function \""+ ((Name)variable).name +"\" was expecting a " + functionData.parameters[i].type.name() + " for it's "+ Parser.formatNumber(i+1) +" parameter!");
            }
            taskScope.variables.put((String) functionData.parameters[i].value, new Scope.Variable(value, functionData.parameters[i].type));
        }
        Object returnStatement = functionData.ast.Walk(taskScope);
        if(!functionData.returns && returnStatement != null)
            throw new RuntimeException("Function \"" + ((Name)variable).name + "\" is not specified to return, but a return value was provided!");
        if(functionData.returns && returnStatement == null)
            throw new RuntimeException("Function \"" + ((Name)variable).name + "\" is specified to return, but no return value was provided!");
        return returnStatement;
    }

    @Override
    public void Output(int level) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < level; i++) {
            output.append("  ");
        }
        System.out.println(output + "Task Call: " + ((Name)variable).name);
        if(parameters.size() > 0) {
            System.out.println(output + "  Parameters:");
        }
        for (Node parameter : parameters) {
            parameter.Output(level + 2);
        }
    }
}
