package mg.itu.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.*;
import java.net.URL;
import java.util.*;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.lang.reflect.*;


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

    // Creer les arguments pour les methodes qui ont des parametres de ApplicationContext   
    public static void creerArguments(Method methode, Object[] arguments, Object applicationContext) {
        for (int i = 0; i < methode.getParameters().length; i++) {
            Parameter p = methode.getParameters()[i];
            if (applicationContext != null && p.getType().isAssignableFrom(applicationContext.getClass())) {
                arguments[i] = applicationContext;
            }
        }
    }



    public static boolean estApiRest(Method methode) {
        return methode.isAnnotationPresent(mg.itu.annotation.ApiRest.class);
    }

    public static String toJson(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    // TOKONY MANAMBOATRA FONCTION RAY MVERIFIER OE LE LIEN VE MANANA fonction
    // annote @UrlMapping (Matoa izy anaty urMethode, efa verifier zany oe manana
    // annotation @UrlMapping zany)
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
        } else if (type == LocalDate.class) {
            return LocalDate.parse(valeur);
        }
        // Ajouter d'autres types si nécessaire

        throw new IllegalArgumentException("Type non pris en charge : " + type.getName());
    }

    

    public static Map<String, Object> creerMapNomValeurParametre(Method methode, Map<String, String[]> param_value, Collection<Part> parts) {
        Map<String, Object> map_nom_valeur_param = new HashMap<>();

        for (Parameter p : methode.getParameters()) {

            Class<?> type_param = p.getType();

            String nom_param = p.getName();
            
            if(Utilitaire.estNatif(type_param)){
                if (param_value.containsKey(nom_param)) {

                    String[] valeur_param = param_value.get(nom_param);

                    if (valeur_param != null && valeur_param.length > 0) {
                        Object valeurConvertie = conversionObject(valeur_param[0], type_param);

                        map_nom_valeur_param.put(nom_param, valeurConvertie);
                    }
                }
            } else if (MapFile.class.isAssignableFrom(type_param)) {
                // Gérer le cas où le paramètre est de type MapFile
                for (Part part : parts) {
                    if (part.getName().equals(nom_param)) {
                        String nomFichier = part.getSubmittedFileName();
                        String extension = nomFichier.substring(nomFichier.lastIndexOf('.') + 1);
                        try {
                            byte[] bytes = part.getInputStream().readAllBytes();
                            MapFile mapFile = new MapFile(bytes, extension, nomFichier);
                            map_nom_valeur_param.put(nom_param, mapFile);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;
                    }
                }
            } else {
                for (Field field : type_param.getDeclaredFields()) {
                    String nom_attribut = field.getName();
                    if(Utilitaire.estNatif(field.getType())) {
                        if (param_value.containsKey(nom_attribut)) {
                            String[] valeur_param = param_value.get(nom_attribut);

                            if (valeur_param != null && valeur_param.length > 0) {
                                Object valeurConvertie = conversionObject(valeur_param[0], field.getType());

                                map_nom_valeur_param.put(nom_attribut, valeurConvertie);
                            }
                        }
                    } else {
                        // Si le type de l'attribut n'est pas natif, vous pouvez gérer la récursion ici
                        // pour remplir les attributs imbriqués si nécessaire.
                        remplirParametresRecursivement(field.getType(), param_value, map_nom_valeur_param);
                    }   
                }
            }


        }
        // Map<String, String[]> param_value = request.getParameterMap(); // mamerina ny
        // nom sy ny valeur anle input avy any am affichage
        // Ny avy any am affichage efa azo daholo
        // le mampifandray anazy amle anaranle parametre any am controller sisa

        return map_nom_valeur_param;
    }

    public static void remplirParametresRecursivement(Class<?> type, Map<String, String[]> param_value, Map<String, Object> map_nom_valeur_param) {

        for (Field field : type.getDeclaredFields()) {

            String nom_attribut = field.getName();
            Class<?> type_attribut = field.getType();

            if (Utilitaire.estNatif(type_attribut)) {

                if (param_value.containsKey(nom_attribut)) {

                    String[] valeur_param = param_value.get(nom_attribut);

                    if (valeur_param != null && valeur_param.length > 0) {

                        Object valeurConvertie = conversionObject(
                                valeur_param[0],
                                type_attribut
                        );

                        map_nom_valeur_param.put(
                                nom_attribut,
                                valeurConvertie
                        );
                    }
                }

            } else {
                remplirParametresRecursivement(
                        type_attribut,
                        param_value,
                        map_nom_valeur_param
                );
            }
        }
    }

    public static void creerArguments(Method methode, Object[] arguments, Map<String, String[]> param_value, Collection<Part> parts, Object applicationContext, Session sessionUser) {
        if (param_value != null) {
            Map<String, Object> map_nom_valeur_param = creerMapNomValeurParametre(methode, param_value, parts);

            for (int i = 0; i < methode.getParameters().length; i++) {
                Parameter p = methode.getParameters()[i];

                String nom_param = p.getName();

                if (Utilitaire.estNatif(p.getType())) { // si natif ny type anle parametre dia tonga de alaina fotsiny ny valeur mifanaraka aminy avy any am vue
                    if (map_nom_valeur_param.containsKey(nom_param)) {
                        arguments[i] = map_nom_valeur_param.get(nom_param);
                    }
                } 
                
                else if (List.class.isAssignableFrom(p.getType())) {
                   Type typeElement = getTypeElementListe(p);
                    if (typeElement == MapFile.class) {
                        arguments[i] = creerListeMapFiles(p, parts);
                    } else {
                        arguments[i] = creerListeObjets(p, param_value);
                    }
                } 
                
                else if (MapFile.class.isAssignableFrom(p.getType())) { // si MapFile
                    Object fichier = map_nom_valeur_param.get(nom_param);

                    if (fichier == null) {
                        throw new IllegalArgumentException(
                                "Aucun fichier reçu pour le paramètre '" + nom_param
                                        + "'. Vérifiez enctype=\"multipart/form-data\" et le nom du champ HTML.");
                    }

                    arguments[i] = fichier;
                } 
                
                else if (Utilitaire.estApplicationContext(p)) {
                    if(applicationContext != null) {
                        Utilitaire.creerArguments(methode, arguments, applicationContext);
                    }
                } 

                else if (UtilitaireSession.estSession(p)) {
                    UtilitaireSession.creerArguments(methode, arguments, sessionUser);
                }

                
                else { // sinon de jerena aloha ny attribut anle parametre de raha type natif de tonga de matchena ihany fa raha mbola tsy natif de idirana ndray ny attribut any
                    Object attributInstance = null;

                    if(attributInstance == null){
                        try {
                            attributInstance = p.getType().getDeclaredConstructor().newInstance();

                            remplirObjet(attributInstance, p.getType(), map_nom_valeur_param);
                            arguments[i] = attributInstance;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    // PARAMETRES LISTE OBJECT
    private static List<Object> creerListeObjets(Parameter parametre, Map<String, String[]> valeurs) {
        Type typeGenerique = parametre.getParameterizedType();
        if (!(typeGenerique instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Le paramètre liste " + parametre.getName()
                    + " doit déclarer son type, par exemple List<Produit>");
        }

        Type typeElement = ((ParameterizedType) typeGenerique)
                .getActualTypeArguments()[0];
        if (!(typeElement instanceof Class<?>)) {
            throw new IllegalArgumentException("Type d'élément de liste non pris en charge pour "
                    + parametre.getName());
        }

        Class<?> classeElement = (Class<?>) typeElement;
        String prefixe = parametre.getName() + "[";
        SortedSet<Integer> indices = new TreeSet<>();
        for (String nomValeur : valeurs.keySet()) {
            if (nomValeur.startsWith(prefixe)) {
                int finIndice = nomValeur.indexOf(']', prefixe.length());
                if (finIndice > prefixe.length()) {
                    try {
                        indices.add(Integer.parseInt(nomValeur.substring(prefixe.length(), finIndice)));
                    } catch (NumberFormatException ignored) {
                        // Ignore les noms qui ne suivent pas le format liste[index].champ.
                    }
                }
            }
        }

        List<Object> liste = new ArrayList<>();
        for (Integer index : indices) {
            try {
                Object element = classeElement.getDeclaredConstructor().newInstance();
                remplirObjetDepuisParametres(element, classeElement,
                        prefixe + index + "].", valeurs);
                liste.add(element);
            } catch (Exception e) {
                throw new IllegalArgumentException("Impossible de créer un élément de la liste "
                        + parametre.getName() + " de type " + classeElement.getName(), e);
            }
        }
        return liste;
    }

    private static void remplirObjetDepuisParametres(Object objet, Class<?> type, String prefixe,
            Map<String, String[]> valeurs) throws Exception {
        for (Field champ : type.getDeclaredFields()) {
            if (Modifier.isStatic(champ.getModifiers()) || champ.isSynthetic()) {
                continue;
            }
            champ.setAccessible(true);
            String nomChamp = prefixe + champ.getName();
            if (Utilitaire.estNatif(champ.getType())) {
                String[] valeursChamp = valeurs.get(nomChamp);
                if (valeursChamp != null && valeursChamp.length > 0) {
                    champ.set(objet, conversionObject(valeursChamp[0], champ.getType()));
                }
            } else if (!List.class.isAssignableFrom(champ.getType())) {
                Object sousObjet = champ.getType().getDeclaredConstructor().newInstance();
                remplirObjetDepuisParametres(sousObjet, champ.getType(), nomChamp + ".", valeurs);
                champ.set(objet, sousObjet);
            }
        }
    }


    // PARAMETRE LISTE MAPFILE
    private static Type getTypeElementListe(Parameter parametre) {
        Type typeGenerique = parametre.getParameterizedType();
        if (!(typeGenerique instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Le paramètre liste " + parametre.getName()
                    + " doit déclarer son type, par exemple List<Produit>");
        }
        return ((ParameterizedType) typeGenerique).getActualTypeArguments()[0];
    }

    private static List<MapFile> creerListeMapFiles(Parameter parametre, Collection<Part> parts) {
        String prefixe = parametre.getName() + "[";
        SortedMap<Integer, Part> fichiers = new TreeMap<>();
        for (Part part : parts) {
            String nom = part.getName();
            if (nom == null || !nom.startsWith(prefixe)) continue;
            int finIndice = nom.indexOf(']', prefixe.length());
            if (finIndice < 0 || finIndice != nom.length() - 1) continue;
            try {
                fichiers.put(Integer.parseInt(nom.substring(prefixe.length(), finIndice)), part);
            } catch (NumberFormatException ignored) {
                // Ignore les champs qui ne suivent pas le format liste[index].
            }
        }

        List<MapFile> resultat = new ArrayList<>();
        for (Part part : fichiers.values()) {
            String nomFichier = part.getSubmittedFileName();
            if (nomFichier == null || nomFichier.isBlank()) continue;
            int point = nomFichier.lastIndexOf('.');
            String extension = point >= 0 ? nomFichier.substring(point + 1) : "";
            try {
                resultat.add(new MapFile(part.getInputStream().readAllBytes(), extension, nomFichier));
            } catch (IOException e) {
                throw new IllegalArgumentException("Impossible de lire le fichier " + nomFichier, e);
            }
        }
        return resultat;
    }

    public static void remplirObjet(Object objet, Class<?> type, Map<String, Object> map_nom_valeur_param) {
        try {
            for (Field field : type.getDeclaredFields()) {

                field.setAccessible(true);

                String nom_attribut = field.getName();

                if (Utilitaire.estNatif(field.getType())) {

                    if (map_nom_valeur_param.containsKey(nom_attribut)) {
                        field.set(objet, map_nom_valeur_param.get(nom_attribut));
                    }

                } else {

                    Object objetAttribut = field.getType()
                            .getDeclaredConstructor()
                            .newInstance();

                    // Appel récursif
                    remplirObjet(
                            objetAttribut,
                            field.getType(),
                            map_nom_valeur_param
                    );

                    field.set(objet, objetAttribut);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean possedeApplicationContext(Method methode) {
        for (Parameter p : methode.getParameters()) {
            if (estApplicationContext(p)) {
                return true;
            }
        }
        return false;
    }

        public static boolean estApplicationContext(Parameter p) {
            if (p.getType().getSimpleName().equals("ApplicationContext")) {
                return true;
            }
        
            return false;
        }



    public static boolean possedeAttribut(Parameter parameter) {
        Class<?> type_param = parameter.getType();
        Field[] liste_attributs = type_param.getDeclaredFields();

        if (liste_attributs.length == 0) {
            return false;
        }

        return true;
    }

    public static boolean estNatif(Class<?> type) {
        return type.isPrimitive() || type == String.class || type == Integer.class || type == Long.class
                || type == Double.class || type == Boolean.class || type == LocalDate.class;
    }

    // raha natif ny type anle parametre dia tonga de alaina fotsiny ny valeur
    // mifanaraka aminy avy any am vue
    // sinon de jerena aloha ny attribut anle parametre de raha type natif de tonga
    // de matchena ihany fa raha mbola tsy natif de idirana ndray ny attribut any




    // parametre -> attribut -> si attribut natif -> matchena valeur avy any am vue
                        //   -> sinon -> idirana ndray ny attribut anle attribut




}
