package controllers;

import src.Controller;
import src.GetMapping;
import src.PostMapping;
import src.ModelView;
import src.RequestParam;

@Controller
public class TracabiliteController {

    /**
     * Page 1 — Affiche le formulaire de saisie de date
     */
    @GetMapping("/tracabilite")
    public ModelView showForm() {
        return new ModelView("/WEB-INF/views/tracabilite-form.jsp");
    }

    /**
     * Reçoit la date de Page 1 et redirige vers Page 2 (résultats)
     * Page 2 sera implémentée par Dev2
     */
    @PostMapping("/tracabilite/resultat")
    public ModelView showResultat(@RequestParam("date") String date) {
        ModelView mv = new ModelView("/WEB-INF/views/tracabilite-resultat.jsp");
        mv.addItem("date", date);
        return mv;
    }
}
