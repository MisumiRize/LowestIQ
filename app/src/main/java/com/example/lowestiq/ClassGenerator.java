package com.example.lowestiq;

import android.view.KeyEvent;

import com.google.dexmaker.Code;
import com.google.dexmaker.Comparison;
import com.google.dexmaker.DexMaker;
import com.google.dexmaker.Label;
import com.google.dexmaker.Local;
import com.google.dexmaker.MethodId;
import com.google.dexmaker.TypeId;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ClassGenerator {

    private DexMaker dexMaker = new DexMaker();
    private File outputDir;

    public static final String TERRIBLE_METHOD = "terribleMethod";

    public ClassGenerator(File outputDir) {
        this.outputDir = outputDir;
    }

    public Class<?> generateAndLoad() throws IOException, ClassNotFoundException {
        TypeId<?> cls = declareClass();
        Code code = declareMethod(cls);
        implementMethod(code);

        ClassLoader classLoader = dexMaker.generateAndLoad(ClassGenerator.class.getClassLoader(), outputDir);

        return classLoader.loadClass("TerribleClass");
    }

    private TypeId<?> declareClass() {
        TypeId<?> cls = TypeId.get("LTerribleClass;");
        dexMaker.declare(cls, "TerribleClass.generated", Modifier.PUBLIC, TypeId.OBJECT);
        return cls;
    }

    private Code declareMethod(TypeId<?> cls) {
        MethodId<?, String> method = cls.getMethod(TypeId.STRING, TERRIBLE_METHOD, TypeId.INT);
        return dexMaker.declare(method, Modifier.PUBLIC | Modifier.STATIC);
    }

    private void implementMethod(Code code) {
        Local<Integer> param = code.getParameter(0, TypeId.INT);
        Local<Integer> compareWith = code.newLocal(TypeId.INT);
        Local<String> returnValue = code.newLocal(TypeId.STRING);

        Field[] fields = KeyEvent.class.getFields();
        for (Field field : fields) {
            if (!field.getName().startsWith("KEYCODE_")) {
                continue;
            }
            try {
                int keyCode = field.getInt(null);
                code.loadConstant(compareWith, keyCode);
                code.loadConstant(returnValue, KeyEvent.keyCodeToString(keyCode));

                Label label = new Label();
                code.compare(Comparison.NE, label, param, compareWith);
                code.returnValue(returnValue);
                code.mark(label);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        code.loadConstant(returnValue, "no match");
        code.returnValue(returnValue);
    }
}
