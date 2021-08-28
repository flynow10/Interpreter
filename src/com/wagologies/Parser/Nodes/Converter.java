package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Scope;

public class Converter implements Node {

    public ConvertType convertType;

    public Converter(ConvertType convertType)
    {
        this.convertType = convertType;
    }

    @Override
    public Object Walk(Scope scope) {
        switch (convertType)
        {
            case TRALSE2NUMBER:
                return (boolean) scope.getVariable("value").value ? 1 : 0;
            case NUMBER2TRALSE:
                return (int)scope.getVariable("value").value == 1;
            case PARSENUMBER:
                return tryParseInt((String) scope.getVariable("value").value, 0);
            case NUMBER2STRING:
                return String.valueOf(scope.getVariable("value").value);
        }
        return null;
    }

    public int tryParseInt(String value, int defaultVal) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    @Override
    public void Output(int level) {

    }
    public enum ConvertType {
        TRALSE2NUMBER,
        NUMBER2TRALSE,
        NUMBER2STRING,
        PARSENUMBER,
    }
}
