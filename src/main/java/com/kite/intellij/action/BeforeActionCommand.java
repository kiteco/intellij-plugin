package com.kite.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;

/**
 * A functional interface to run code before the action delegation is performed.
 *
  */
@FunctionalInterface
public interface BeforeActionCommand {
    void perform(Project project, AnAction kiteAction, AnAction originalAction);
}
