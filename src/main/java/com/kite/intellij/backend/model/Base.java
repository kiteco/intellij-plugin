package com.kite.intellij.backend.model;

import java.util.Objects;

/**
 * {
 * id: ID
 * name: STRING    // unqualified name of class, e.g. "Model"
 * module: STRING  // fully qualified name of module, e.g. "django.db.models"
 * module_id: ID
 * }
 */
public class Base {
    public static final Base[] EMPTY_ARRAY = new Base[0];

    private final Id id;
    private final String name;
    private final String module;
    private final Id moduleId;

    public Base(Id id, String name, String module, Id moduleId) {
        this.id = id;
        this.name = name;
        this.module = module;
        this.moduleId = moduleId;
    }

    public Id getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getModule() {
        return module;
    }

    public Id getModuleId() {
        return moduleId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, module, moduleId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Base base = (Base) o;
        return Objects.equals(id, base.id) &&
                Objects.equals(name, base.name) &&
                Objects.equals(module, base.module) &&
                Objects.equals(moduleId, base.moduleId);
    }

    @Override
    public String toString() {
        return "Base{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", module='" + module + '\'' +
                ", moduleId=" + moduleId +
                '}';
    }
}
