package com.kite.intellij.action.signatureInfo;

public enum ParamActionType {
    Previous, Next;

    public boolean isNext() {
        return Next.equals(this);
    }
}
