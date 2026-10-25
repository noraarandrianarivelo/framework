package mg.itu.util;

import java.lang.reflect.Method;

public class UtilitaireAuth {
    public static boolean needAuth(Method methode) {
        return methode.isAnnotationPresent(mg.itu.annotation.Auth.class);
    }

    
}
