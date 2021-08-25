package com.wagologies.Parser;

import com.wagologies.Scope;

public interface Node {
    Object Walk(Scope scope);
    void Output(int level);
}
