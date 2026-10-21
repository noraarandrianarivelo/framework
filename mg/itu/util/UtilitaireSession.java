package mg.itu.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Parameter;

import jakarta.servlet.http.HttpSession;

public class UtilitaireSession {
    public static boolean possedeSession(Method methode) {
        for (Parameter p : methode.getParameters()) {
            if (p.getType().isAssignableFrom(Session.class)) {
                return true;
            }
        }
        return false;
    }

    
    public static void creerArguments(Method methode, Object[] arguments, Session session) {
        for (int i = 0; i < methode.getParameters().length; i++) {
            Parameter p = methode.getParameters()[i];
            if (session != null && p.getType().isAssignableFrom(Session.class)) {
                arguments[i] = session;
            }
        }
    }

    public static void creerMapSession(HttpSession sessionHttp, Session session) {
        session.clearAttributes();
        Enumeration<String> attributeNames = sessionHttp.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeValue = sessionHttp.getAttribute(attributeName);
            session.setAttribute(attributeName, attributeValue);
        }
    }

    public static void synchroniserSession(Session session, HttpSession sessionHttp) {
        Map<String, Object> attrs = session.getAllAttributesMap();

        List<String> aSupprimer = new ArrayList<>();
        Enumeration<String> noms = sessionHttp.getAttributeNames();
        while (noms.hasMoreElements()) {
            String nom = noms.nextElement();
            if (!attrs.containsKey(nom))
                aSupprimer.add(nom);
        }
        for (String nom : aSupprimer)
            sessionHttp.removeAttribute(nom);

        for (Map.Entry<String, Object> e : attrs.entrySet()) {
            sessionHttp.setAttribute(e.getKey(), e.getValue());
        }
    }
}
