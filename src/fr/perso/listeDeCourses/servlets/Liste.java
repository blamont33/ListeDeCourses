package fr.perso.listeDeCourses.servlets;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.perso.listeDeCourses.bll.ListeDeCoursesManager;
import fr.perso.listeDeCourses.bo.CompositionListe;
import fr.perso.listeDeCourses.bo.Ingredient;
import fr.perso.listeDeCourses.bo.ListeCourse;
import fr.perso.listeDeCourses.bo.Recette;

/**
 * Servlet implementation class listeDeCourses
 */
@WebServlet("/Liste")
public class Liste extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static ListeDeCoursesManager listeDeCoursesManager = new ListeDeCoursesManager();
	private static List<Ingredient> listeIngredient = new ArrayList<Ingredient>();
	private static int id_liste;
	private static List<Ingredient> listeFinale = new ArrayList<Ingredient>();

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getSession().removeAttribute("lien");
		request.getSession().removeAttribute("nomRecette");
		
		request.setAttribute("listeFinale", listeFinale);

		List<Ingredient> ListeComplete = listeDeCoursesManager.afficherIngredient();
		request.setAttribute("ListeComplete", ListeComplete);

		request.getServletContext().getRequestDispatcher("/WEB-INF/liste.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		
		
		//On récupère la liste des recettes composant le menu
		List<Recette> listeRecette = (List<Recette>) request.getSession().getAttribute("listeRecette");
		
	

		///////////////////////////////////////GENERATION LISTE DE COURSE////////////////////////////////////////////
		
		// Après avoir cliqué sur "générer liste de courses"
		String generer = request.getParameter("choix");
		if(generer!=null && generer.equals("generer")) {
			listeIngredient.clear();
		//On récupère tous les ingrédients présent dans la liste de recettes
		for (Recette r : listeRecette) {
			for (Ingredient i : r.getListeIngredient()) {
				listeIngredient.add(i);
			}
		}
		
		traitementListe(request);
		}
		///////////////////////////////////////GENERATION LISTE DE COURSE -FIN //////////////////////////////////////////////
		
		// ajouter un ingrédient
	
		String ajouter = request.getParameter("ajouter");
		if (ajouter != null) {
			if (ajouter.equals("Ajouter à la liste")) {
				System.out.println("j'ajoute un ingrédient");
				String nom = request.getParameter("nom");
				System.out.println(nom);
				int quantite = Integer.parseInt(request.getParameter("quantite"));
				String unite = request.getParameter("unite");
				int id_ingredient = listeDeCoursesManager.ingredientByName(nom).getId();
				Ingredient ingredient = new Ingredient(id_ingredient,nom, quantite, unite);
				listeIngredient.add(ingredient);
				
				traitementListe(request);
				
			}
		}

		// Supprimer un ingredient de la liste
		String supprimer = request.getParameter("supprimer");
		if (supprimer != null) {
			
				int id = Integer.parseInt(request.getParameter("supprimer"));

				int position = 0;
				for (Ingredient i : listeFinale) {
					if (i.getId() == id) {
						position = listeFinale.indexOf(i);
					}
				}

				listeFinale.remove(position);
				listeIngredient = listeFinale;
				traitementListe(request);
			
		}

		doGet(request, response);
	}

	public void traitementListe(HttpServletRequest request) {
		//On crée une liste en BDD pour récupérer son ID
		ListeCourse listeCourse = new ListeCourse("");
		listeDeCoursesManager.ajouterListe(listeCourse);
		id_liste = listeCourse.getId_liste();

		// On crée une instance de compositionListe qu'on ajoute en BDD pour pouvoir la traiter et additionner les mêmes ingrédients.
		CompositionListe compositionListe = new CompositionListe(listeIngredient, id_liste);
		listeDeCoursesManager.ajouterCompositionListe(compositionListe);

		//On récupère la liste de course additionnée en BDD
		listeFinale = listeDeCoursesManager.afficherListe(id_liste);


		request.setAttribute("listeFinale", listeFinale);
		//On supprime la liste en bdd
		listeDeCoursesManager.clearListe();
	}

}
