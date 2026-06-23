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
    Map<String, Mapping> urlMapping = new HashMap<>();

    public void init() throws ServletException {
        try {
            String nom_package = getServletConfig().getInitParameter("nomPackage");

            listeControllers = Utilitaire.recupererClassesAvecAnnotation(new Utilitaire(nom_package,
                    "mg.itu.annotation.Controller", ElementType.METHOD));

            urlMapping = Utilitaire.recupererUrlMapping(
                    new Utilitaire(nom_package, "mg.itu.annotation.UrlMapping", ElementType.METHOD));

        } catch (Exception e) {
            System.out.println("Erreur lors de la recuperation des controllers : " + e.getMessage());
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String url = request.getRequestURI().substring(request.getContextPath().length());

        out.println("<h1>Front Controller</h1>");
        out.println("<p>URL recue : " + url + "</p>");

        afficher(url, request, response);

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void afficher(String url, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        Mapping mapping = urlMapping.get(url);

        if (mapping != null) {
                out.println("Url trouvee : " + url);
                out.println("Classe: " + mapping.getClasse().getName());
                out.println("Méthode: " + mapping.getMethode().getName());
        } else {
            out.println("Url non trouvee : " + url);
            out.println("<h2>Liste des URL disponibles :</h2>");
            for (String urlDisponible : urlMapping.keySet()) {
                Mapping mappingDisponible = urlMapping.get(urlDisponible);
                out.println("<p>URL: " + urlDisponible + " | Classe: " + mappingDisponible.getClasse().getName() + " | Méthode: "
                        + mappingDisponible.getMethode().getName() + "</p>");
            }
        }
    }

}