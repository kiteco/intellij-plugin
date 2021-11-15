package com.kite.intellij.backend.model;

/**
 */
public enum Kind {
    Function, Module, Type, Instance, Descriptor, Union, Symbol, Object, Unknown;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

    public static Kind fromJsonString(String value) {
        switch (value) {
            case "function": //js + py
                return Function;
            case "module": //js + py
                return Module;
            case "type": //py
                return Type;
            case "instance": //py
                return Instance;
            case "descriptor": //py
                return Descriptor;
            case "union": //js + py
                return Union;
            case "symbol": //js
                return Symbol;
            case "object": //js
                return Object;

            case "unknown": //fall through
            default:
                return Unknown;        }
    }
}
