package com.wagologies.Parser.Nodes;

import com.wagologies.Parser.Node;
import com.wagologies.Scope;

import java.util.Scanner;

public class Input implements Node {
    static Scanner scanner = new Scanner(System.in);
    public Input()
    {

    }
    @Override
    public Object Walk(Scope scope) {
        return scanner.nextLine();
    }

    @Override
    public void Output(int level) {

    }
}
