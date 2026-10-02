package mg.itu.util;

import java.io.File;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

import com.google.gson.Gson;

import com.mysql.cj.util.Util;

import java.time.LocalDate;
 

public class Utilitaire {
    private String nom_package;
    private String annotation;
    private ElementType niveau;

    public Utilitaire(String nom_package, String annotation, ElementType niveau) {
        this.nom_package = nom_package;
        this.annotation = annotation;
        this.niveau = niveau;
    }

    public String getNom_package() {
        return nom_package;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public ElementType getNiveau() {
        return niveau;
    }

    public void setNiveau(ElementType niveau) {
        this.niveau = niveau;
    }

    public void setNom_package(String nom_package) {
        this.nom_package = nom_package;
    }

    public static void recupererClasses(String nomPackage, List<Class<?>> classes) throws Exception {

        String cheminDossier = nomPackage.replace('.', '/');

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL ressource = classLoader.getResource(cheminDossier);

        if (ressource == null) {
            throw new IllegalArgumentException("Le package " + nomPackage + " n'existe pas.");
        }

        File dossier = new File(ressource.toURI());

        if (dossier.exists() && dossier.isDirectory()) {
            File[] fichiers = dossier.listFiles();
            if (fichiers != null) {
                for (File fichier : fichiers) {
                    if (fichier.isFile() && fichier.getName().endsWith(".class")) {
                        String nomClasse = nomPackage + '.'
                                + fichier.getName().substring(0, fichier.getName().length() - 6);

                        classes.add(Class.forName(nomClasse));
                    }
                }
            }
        }
    }

    public static void recupererClassesAvecAnnotation(Utilitaire utilitaire, List<String> listeAvecAnnotation)
            throws Exception {
        try {
            utilitaire.recupererElements(utilitaire, listeAvecAnnotation);

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erreur lors de la récupération des classes : " + e.getMessage());
        }
    }

    public void recupererElements(Utilitaire utilitaire, List<String> resultat) throws Exception {

        List<Class<?>> classes = new ArrayList<>();
        recupererClasses(utilitaire.getNom_package(), classes);

        Class<?> annotationClass = Class.forName(utilitaire.getAnnotation());

        if (!annotationClass.isAnnotation()) {
            throw new Exception("Ce n'est pas une annotation");
        }

        Class<? extends Annotation> annotation = annotationClass.asSubclass(Annotation.class);

        switch (utilitaire.getNiveau()) {

            case TYPE:
                for (Class<?> classe : classes) {
                    if (classe.isAnnotationPresent(annotation)) {
                        resultat.add(classe.toString());
                    }
                }
                break;

            case FIELD:
                for (Class<?> classe : classes) {
                    for (Field field : classe.getDeclaredFields()) {
                        if (field.isAnnotationPresent(annotation)) {
                            resultat.add(field.toString());
                        }
                    }
                }
                break;

            case METHOD:
                for (Class<?> classe : classes) {
                    for (Method method : classe.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(annotation)) {
                            resultat.add(method.toString());
                        }
                    }
                }
                break;
        }

    }

    public static Map<UrlMethod, Mapping> recupererUrlMapping(Utilitaire utilitaire) throws Exception {
        Map<UrlMethod, Mapping> urlMapping = new HashMap<>();

        List<Class<?>> classes = new ArrayList<>();
        recupererClasses(utilitaire.getNom_package(), classes);

        Class<?> annotationClass = Class.forName(utilitaire.getAnnotation());

        if (!annotationClass.isAnnotation()) {
            throw new Exception("Ce n'est pas une annotation");
        }

        Class<? extends Annotation> annotation = annotationClass.asSubclass(Annotation.class);

        Method valueMethod = annotation.getMethod("value");
        Method methodUrl = annotation.getMethod("method");

        for (Class<?> classe : classes) {
            for (Method method : classe.getDeclaredMethods()) {

                if (method.isAnnotationPresent(annotation)) {

                    Annotation ann = method.getAnnotation(annotation);

                    String url = (String) valueMethod.invoke(ann);
                    String methodOfUrl = (String) methodUrl.invoke(ann);

                    UrlMethod urlMethod = new UrlMethod(url, methodOfUrl);

                    if (urlMapping.containsKey(urlMethod)) {
                        throw new Exception("URL Deja utilise par un autre controller : " + urlMethod.getUrl()
                                + " avec la methode : " + urlMethod.getMethod());
                    }

                    urlMapping.put(urlMethod, new Mapping(classe, method));
                }
            }
        }

        return urlMapping;
    }

    // je pense que il faut que je cree une toute nouvelle fonction, de asina map mitazona ny anarana sy ny valeur anle parametre
    public static void creerArguments(Method methode, Object[] arguments, Object applicationContext) {
        for (int i = 0; i < methode.getParameters().length; i++) {
            Parameter p = methode.getParameters()[i];
            if (applicationContext != null && p.getType().isAssignableFrom(applicationContext.getClass())) {
                arguments[i] = applicationContext;
            }            
        }
    }

    public static void creerArguments(Method methode, Object[] arguments) {
        for (int i = 0; i < methode.getParameters().length; i++) {
            Parameter p = methode.getParameters()[i];
        }
    }

    public static boolean estApiRest(Method methode) {
        return methode.isAnnotationPresent(mg.itu.annotation.ApiRest.class);
    }

    public static String toJson(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    // TOKONY MANAMBOATRA FONCTION RAY MVERIFIER OE LE LIEN VE MANANA fonction annote @UrlMapping (Matoa izy anaty urMethode, efa verifier zany oe manana annotation @UrlMapping zany)
    // VERIFIENA OE LE METHODE ANNOTE @UrlMapping ve misy annotation hafa @ApiRest

    public static Object conversionObject(String valeur, Class<?> type) {
        if (valeur == null || valeur.isEmpty()) {
            return null;
        }

        if (type == String.class) {
            return valeur;
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(valeur);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(valeur);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(valeur);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(valeur);
        } else if (type == LocalDate.class){
            return LocalDate.parse(valeur);
        }
        // Ajouter d'autres types si nécessaire

        throw new IllegalArgumentException("Type non pris en charge : " + type.getName());
    }

    public static Map<String, Object> creerMapNomValeurParametre(Method methode, Map<String, String[]> param_value) {
        Map<String, Object> map_nom_valeur_param = new HashMap<>();

        for (Parameter p : methode.getParameters()) {

            Class<?> type_param = p.getType();
            String nom_param = p.getName();

            if (param_value.containsKey(nom_param)) {

                String[] valeur_param = param_value.get(nom_param);

                if (valeur_param != null && valeur_param.length > 0) {
                    Object valeurConvertie =
                            conversionObject(valeur_param[0], type_param);

                    map_nom_valeur_param.put(nom_param, valeurConvertie);
                }
            }
        }
        // Map<String, String[]> param_value = request.getParameterMap(); // mamerina ny nom sy ny valeur anle input avy any am affichage
        // Ny avy any am affichage efa azo daholo
        // le mampifandray anazy amle anaranle parametre any am controller sisa
        
        return map_nom_valeur_param;
    }

    public static void creerArguments(Method methode, Object[] arguments, Map<String, String[]> param_value) {
        if(param_value != null){
            Map<String, Object> map_nom_valeur_param = creerMapNomValeurParametre(methode, param_value);

            for (int i = 0; i < methode.getParameters().length; i++) {
                Parameter p = methode.getParameters()[i];
                String nom_param = p.getName();

                if (map_nom_valeur_param.containsKey(nom_param)) {
                    arguments[i] = map_nom_valeur_param.get(nom_param);
                }
            }
        } else {
            creerArguments(methode, arguments);
        }
    }

    public static boolean possedeApplicationContext(Method methode) {
        for (Parameter p : methode.getParameters()) {
            if (p.getType().getSimpleName().equals("ApplicationContext")) {
                return true;
            }
        }
        return false;
    }

}
