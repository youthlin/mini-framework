package com.youthlin.mvc.listener;

import java.lang.reflect.Method;

/**
 * 创建： youthlin.chen
 * 时间： 2017-08-13 16:07.
 */
public class ControllerAndMethod {
    private Object controller;
    private Method method;

    //package private
    ControllerAndMethod(Object controller, Method method) {
        this.controller = controller;
        this.method = method;
    }

    @Override public String toString() {
        return "ControllerAndMethod{" +
                "controller=" + controller +
                ", method=" + method +
                '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ControllerAndMethod that = (ControllerAndMethod) o;

        //noinspection SimplifiableIfStatement
        if (controller != null ? !controller.equals(that.controller) : that.controller != null)
            return false;
        return method != null ? method.equals(that.method) : that.method == null;
    }

    @Override public int hashCode() {
        int result = controller != null ? controller.hashCode() : 0;
        result = 31 * result + (method != null ? method.hashCode() : 0);
        return result;
    }

    public Object getController() {
        return controller;
    }

    public ControllerAndMethod setController(Object controller) {
        this.controller = controller;
        return this;
    }

    public Method getMethod() {
        return method;
    }

    public ControllerAndMethod setMethod(Method method) {
        this.method = method;
        return this;
    }
}
