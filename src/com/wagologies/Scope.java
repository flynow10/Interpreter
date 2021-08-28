package com.wagologies;

import com.wagologies.Parser.Node;
import com.wagologies.Parser.Parser;

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
        public Node ast;
        public boolean returns;
        public Variable[] parameters;
        public FunctionData(Node ast, boolean returns, Variable... parameters)
        {
            this.ast = ast;
            this.returns = returns;
            this.parameters = parameters;
        }
    }

    public static class Variable {
        public Object value;
        public Parser.Type type;
        public Variable(Object value, Parser.Type type)
        {
            this.value = value;
            this.type = type;
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
        if(this.global.variables.containsKey(name))
        {
            return this.global.variables.get(name);
        }
        return null;
    }
}
