package controllers;

import src.Controller;
import src.GetMapping;
import src.PostMapping;
import src.ModelView;
import src.RequestParam;

import dao.VehiculeDAO;
import models.Vehicule;

import java.util.List;

@Controller
public class VehiculeController {

    private VehiculeDAO vehiculeDAO = new VehiculeDAO();

    // ==================== GESTION DES VÉHICULES ====================

    /**
     * Affiche la liste de tous les véhicules
     */
    @GetMapping("/vehicules")
    public ModelView listVehicules() {
        ModelView mv = new ModelView("/WEB-INF/views/vehicule-list.jsp");
        try {
            List<Vehicule> vehicules = vehiculeDAO.findAll();
            mv.addItem("vehicules", vehicules);
        } catch (Exception e) {
            mv.addItem("error", "Erreur lors du chargement des véhicules: " + e.getMessage());
        }
        return mv;
    }

    /**
     * Affiche le formulaire d'ajout d'un véhicule
     */
    @GetMapping("/vehicule/form")
    public ModelView showForm() {
        ModelView mv = new ModelView("/WEB-INF/views/vehicule-form.jsp");
        return mv;
    }

    /**
     * Affiche le formulaire de modification d'un véhicule
     */
    @GetMapping("/vehicule/edit")
    public ModelView showEditForm(@RequestParam("id") String id) {
        ModelView mv = new ModelView("/WEB-INF/views/vehicule-form.jsp");
        try {
            int vehiculeId = Integer.parseInt(id);
            Vehicule vehicule = vehiculeDAO.findById(vehiculeId);
            if (vehicule != null) {
                mv.addItem("vehicule", vehicule);
            } else {
                mv.addItem("error", "Véhicule non trouvé");
            }
        } catch (Exception e) {
            mv.addItem("error", "Erreur lors du chargement du véhicule: " + e.getMessage());
        }
        return mv;
    }

    /**
     * Enregistre un nouveau véhicule ou met à jour un existant
     */
    @PostMapping("/vehicule/save")
    public ModelView saveVehicule(@RequestParam("id") String id,
            @RequestParam("marque") String marque,
            @RequestParam("capacite") String capacite,
            @RequestParam("typeCarburant") String typeCarburant,
            @RequestParam("vitesseMoyenne") String vitesseMoyenne,
            @RequestParam("tempsAttente") String tempsAttente) {

        ModelView mv = new ModelView("/WEB-INF/views/vehicule-form.jsp");

        try {
            // Création ou récupération du véhicule
            Vehicule vehicule;
            if (id != null && !id.trim().isEmpty() && !id.equals("0")) {
                vehicule = vehiculeDAO.findById(Integer.parseInt(id));
                if (vehicule == null) {
                    mv.addItem("error", "Véhicule non trouvé pour modification");
                    return mv;
                }
            } else {
                vehicule = new Vehicule();
            }

            // Mise à jour des données
            vehicule.setMarque(marque);
            vehicule.setCapacite(Integer.parseInt(capacite));
            vehicule.setTypeCarburant(typeCarburant);
            vehicule.setVitesseMoyenne(java.math.BigDecimal.valueOf(Double.parseDouble(vitesseMoyenne)));
            vehicule.setTempsAttente(Integer.parseInt(tempsAttente));

            // Sauvegarde
            vehiculeDAO.save(vehicule);

            mv.addItem("success", "Véhicule enregistré avec succès ! (ID: " + vehicule.getId() + ")");

        } catch (Exception e) {
            mv.addItem("error", "Erreur lors de l'enregistrement: " + e.getMessage());
        }
        return mv;
    }

    /**
     * Supprime un véhicule
     */
    @GetMapping("/vehicule/delete")
    public ModelView deleteVehicule(@RequestParam("id") String id) {
        ModelView mv = new ModelView("/WEB-INF/views/vehicule-list.jsp");
        try {
            int vehiculeId = Integer.parseInt(id);
            vehiculeDAO.delete(vehiculeId);
            mv.addItem("success", "Véhicule supprimé avec succès !");

            // Recharger la liste
            List<Vehicule> vehicules = vehiculeDAO.findAll();
            mv.addItem("vehicules", vehicules);

        } catch (Exception e) {
            mv.addItem("error", "Erreur lors de la suppression: " + e.getMessage());
            try {
                mv.addItem("vehicules", vehiculeDAO.findAll());
            } catch (Exception ex) {
                // Ignorer
            }
        }
        return mv;
    }
}