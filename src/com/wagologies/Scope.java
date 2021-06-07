package com.wagologies;

import java.util.*;

public class Scope {

    public Scope global = null;

    public Scope parent = null;

    public HashMap<String, Variable> variables = new HashMap<>();

    public HashMap<String, FunctionData> functions = new HashMap<>();

    public Scope() {

    }

    public Scope(Scope global, Scope parent)
    {
        this.global = global;
        this.parent = parent;
    }

    public static class FunctionData {
        public Parser.Node ast;
        public List<String> parameters = new ArrayList<>();
        public FunctionData(Parser.Node ast,  String... parameters)
        {
            this.ast = ast;
            this.parameters.addAll(Arrays.asList(parameters));
        }
    }

    public static class Variable {
        public int value;
        public Variable(int value)
        {
            this.value = value;
        }
    }

    public FunctionData getFunction(String name) {
        Scope currentScope = this;
        while (currentScope != null) {
            if (currentScope.functions.containsKey(name)) {
                return currentScope.functions.get(name);
            }
            currentScope = currentScope.parent;
        }
        if(this.global.functions.containsKey(name))
        {
            return this.global.functions.get(name);
        }
        return null;
    }

    public Variable getVariable(String name) {
        Scope currentScope = this;
        while (currentScope != null) {
            if (currentScope.variables.containsKey(name)) {
                return currentScope.variables.get(name);
            }
            currentScope = currentScope.parent;
        }
        if(this.global.functions.containsKey(name))
        {
            return this.global.variables.get(name);
        }
        return null;
    }
}
