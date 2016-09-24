package com.tiny.abtest.utils;

import javax.script.*;

/**
 * Created by 16072453 on 2016/9/18.
 */
public class ScriptEngineUtils {

    private static final ScriptEngine jsEngine;

    static {
        ScriptEngineManager manager = new ScriptEngineManager();
        jsEngine = manager.getEngineByName("js");
    }

    public static CompiledScript compileJSScript(String expr) throws ScriptException {
        return ((Compilable) jsEngine).compile(expr);
    }
}
