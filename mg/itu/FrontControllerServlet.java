package mg.itu;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import mg.itu.util.*;
import java.util.*;

public class FrontControllerServlet extends HttpServlet {
    List<String> listeControllers = new ArrayList<>();
    Map<UrlMethod, Mapping> urlMapping = new HashMap<>();

    public void init() throws ServletException {
        try {
            String nom_package = getServletConfig().getInitParameter("nomPackage");

            listeControllers = Utilitaire.recupererClassesAvecAnnotation(new Utilitaire(nom_package,
                    "mg.itu.annotation.Controller", ElementType.METHOD));

            urlMapping = Utilitaire.recupererUrlMapping(
                    new Utilitaire(nom_package, "mg.itu.annotation.UrlMapping", ElementType.METHOD));

        } catch (Exception e) {
            // System.out.println("Erreur lors de la recuperation des controllers : " + e.getMessage());
            throw new ServletException(e.getMessage());
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String url = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod();

        out.println("<h1>Front Controller</h1>");
        out.println("<p>URL recue : " + url + "</p>");

        afficher(url, method, request, response);

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void afficher(String url, String method, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        UrlMethod urlMethod = new UrlMethod(url, method);
        Mapping mapping = urlMapping.get(urlMethod);

        if (mapping != null) {
                out.println("<p>URL: " + urlMethod.getUrl() + " avec la methode : " + urlMethod.getMethod() + "| Classe: " + mapping.getClasse().getName() + " | Fonction: "
                        + mapping.getMethode().getName() + "</p>");
                try {
                    Object instance = mapping.getClasse().getDeclaredConstructor().newInstance();
                    Method methode = mapping.getMethode();
                    Object resultat = methode.invoke(instance);

                    out.print("<script>console.log('" + resultat.toString() + "');</script>");
                } catch (Exception e) {
                    out.println("<p>Erreur lors de l'invocation de la méthode : " + e.getMessage() + "</p>");
                }
        } else {
            out.println("Url non trouvee : " + url);
            out.println("<h2>Liste des URL disponibles :</h2>");
            for (UrlMethod urlMethodDisponible : urlMapping.keySet()) {
                Mapping mappingDisponible = urlMapping.get(urlMethodDisponible);
                out.println("<p>URL: " + urlMethodDisponible.getUrl() + " avec la methode : " + urlMethodDisponible.getMethod() + "| Classe: " + mappingDisponible.getClasse().getName() + " | Fonction: "
                        + mappingDisponible.getMethode().getName() + "</p>");
            }
        }
    }

}