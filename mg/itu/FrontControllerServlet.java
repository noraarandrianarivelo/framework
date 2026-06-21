package mg.itu;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import mg.itu.util.*;
import java.util.*;

public class FrontControllerServlet extends HttpServlet {
    List<String> listeControllers = new ArrayList<>();

    public void init() throws ServletException {
        try {
            String nom_package = getServletConfig().getInitParameter("nomPackage");
            listeControllers = Utilitaire.recupererClassesAvecAnnotation(new Utilitaire(nom_package,
                    "mg.itu.annotation.Controller", java.lang.annotation.ElementType.TYPE));
        } catch (Exception e) {
            System.out.println("Erreur lors de la recuperation des controllers : " + e.getMessage());
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<h1>Front Controller</h1>");
        out.println("<p>URL recue : " + request.getRequestURL() + "</p>");

        for (String controller : listeControllers) {
            out.println("<p>Controller trouvé : " + controller + "</p>");
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}