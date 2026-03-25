package controllers;

import src.Controller;
import src.GetMapping;
import src.PostMapping;
import src.ModelView;
import src.RequestParam;

import dao.ReservationDAO;
import dao.TokenDAO;
import dao.ConfigReader;
import models.Reservation;
import service.AssignationService;
import service.GroupingService;
import models.AssignmentProposal;

import java.sql.SQLException;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Map;

@Controller
public class AssignationController {

    private ReservationDAO reservationDAO = new ReservationDAO();
    private TokenDAO tokenDAO = new TokenDAO();
    private AssignationService assignationService = new AssignationService();
    private GroupingService groupingService = new GroupingService();
    private dao.VehiculeDAO vehiculeDAO = new dao.VehiculeDAO();

    @GetMapping("/assignations")
    public ModelView listDates() {
        String token = ConfigReader.getCurrentToken();
        if (!isTokenValid(token)) return createErrorView(token);

        ModelView mv = new ModelView("/WEB-INF/views/assignation-list.jsp");
        try {
            java.util.List<java.sql.Date> dates = reservationDAO.findDistinctDatesByStatus("EN_ATTENTE");
            mv.addItem("dates", dates);
        } catch (Exception e) {
            mv.addItem("error", "Erreur: " + e.getMessage());
        }
        return mv;
    }

    @GetMapping("/assignations/hours")
    public ModelView listHours(@RequestParam("date") String dateStr) {
        String token = ConfigReader.getCurrentToken();
        if (!isTokenValid(token)) return createErrorView(token);

        ModelView mv = new ModelView("/WEB-INF/views/assignation-hours.jsp");
        try {
            Date date = Date.valueOf(dateStr);
            // Use computeAssignmentsForDate to get the correct departure time (based on last assigned reservation)
            models.AssignmentProposal proposal = groupingService.computeAssignmentsForDate(date);
            // DEBUG: log brief proposal overview for troubleshooting
            try {
                System.out.println("DEBUG-CONTROLLER: computeAssignmentsForDate date=" + date + " groups=" + (proposal != null ? proposal.getGroups().size() : "null"));
            } catch (Exception __dbg) { }
            mv.addItem("date", dateStr);
            mv.addItem("proposal", proposal);
        } catch (Exception e) {
            mv.addItem("error", "Erreur: " + e.getMessage());
        }
        return mv;
    }

    @GetMapping("/assignations/computeGroup")
    public ModelView computeGroup(@RequestParam("date") String dateStr, @RequestParam("index") int groupIndex) {
        String token = ConfigReader.getCurrentToken();
        if (!isTokenValid(token)) return createErrorView(token);

        ModelView mv = new ModelView("/WEB-INF/views/assignation-proposal-modal.jsp");
        try {
            java.sql.Date date = java.sql.Date.valueOf(dateStr);
            models.AssignmentProposal proposal = groupingService.computeAssignmentsForDate(date);
            mv.addItem("proposal", proposal);
            mv.addItem("groupIndex", groupIndex);
            mv.addItem("date", dateStr);
        } catch (Exception e) {
            mv.addItem("error", "Erreur lors du calcul de la proposition: " + e.getMessage());
        }
        return mv;
    }

    @GetMapping("/assignations/group")
    public ModelView groupDetail(@RequestParam("date") String dateStr, @RequestParam("index") int groupIndex) {
        String token = ConfigReader.getCurrentToken();
        if (!isTokenValid(token)) return createErrorView(token);

        ModelView mv = new ModelView("/WEB-INF/views/assignation-detail.jsp");
        try {
            java.sql.Date date = java.sql.Date.valueOf(dateStr);
            AssignmentProposal proposal = groupingService.computeAssignmentsForDate(date);
            if (proposal == null) {
                mv.addItem("error", "Aucune proposition disponible pour cette date");
                return mv;
            }
            if (groupIndex < 0 || groupIndex >= proposal.getGroups().size()) {
                mv.addItem("error", "Groupe invalide");
                return mv;
            }
            AssignmentProposal.GroupProposal gp = proposal.getGroups().get(groupIndex);

            // compute human-readable time range: first arrival - departureTime
            String heureRange = "—";
            try {
                if (gp != null && gp.reservations != null && !gp.reservations.isEmpty()) {
                    java.sql.Time firstTime = null;
                    for (models.AssignmentProposal.ReservationProposal rrp : gp.reservations) {
                        Reservation rtemp = reservationDAO.findById(rrp.reservationId);
                        if (rtemp != null && rtemp.getHeureArrivee() != null) {
                            if (firstTime == null || rtemp.getHeureArrivee().before(firstTime)) firstTime = rtemp.getHeureArrivee();
                        }
                    }
                    if (firstTime != null) {
                        String first = firstTime.toString();
                        if (first.length() >= 5) first = first.substring(0,5);
                        heureRange = first;
                    }
                }
                if (gp != null && gp.departureTime != null) {
                    String dep = gp.departureTime.toString();
                    if (dep.length() >=5) dep = dep.substring(0,5);
                    heureRange = (heureRange.equals("—") ? dep : heureRange + " - " + dep);
                }
            } catch (Exception ex) { /* ignore formatting errors */ }

            mv.addItem("date", dateStr);
            mv.addItem("heure", heureRange);
            mv.addItem("proposal", proposal);
            mv.addItem("groupIndex", groupIndex);
        } catch (Exception e) {
            mv.addItem("error", "Erreur: " + e.getMessage());
        }
        return mv;
    }

    @PostMapping("/assignations/confirmGroup")
    public ModelView confirmGroup(@RequestParam("date") String dateStr, @RequestParam("index") int groupIndex) {
        String token = ConfigReader.getCurrentToken();
        if (!isTokenValid(token)) return createErrorView(token);

        ModelView mv = new ModelView("/WEB-INF/views/assignation-result.jsp");
        try {
            java.sql.Date date = java.sql.Date.valueOf(dateStr);
            models.AssignmentProposal full = groupingService.computeAssignmentsForDate(date);

            // validate group index
            if (groupIndex < 0 || groupIndex >= full.getGroups().size()) {
                mv.addItem("error", "Groupe invalide");
                return mv;
            }

            models.AssignmentProposal.GroupProposal gp = full.getGroups().get(groupIndex);

            // Build sub-proposal containing only vehicle summaries for this group
            models.AssignmentProposal sub = new models.AssignmentProposal();
            sub.setDate(full.getDate());

            // collect vehicle ids used in this group from reservation proposals
            java.util.Set<Integer> vehicleIds = new java.util.HashSet<>();
            for (models.AssignmentProposal.ReservationProposal rp : gp.reservations) {
                if (rp.proposedVehiculeId != null) vehicleIds.add(rp.proposedVehiculeId);
            }

            // copy relevant vehicle summaries
            for (Integer vid : vehicleIds) {
                models.AssignmentProposal.VehicleSummary vs = full.getVehicleSummaries().get(vid);
                if (vs != null) sub.getVehicleSummaries().put(vid, vs);
            }

            // copy the group so that non-assigned reservations can be updated
            sub.getGroups().add(gp);

            // Optimistic checks: reservations still EN_ATTENTE and vehicles available
            for (models.AssignmentProposal.VehicleSummary vs : sub.getVehicleSummaries().values()) {
                // check reservations
                for (Integer rid : vs.reservationIds) {
                    models.Reservation r = reservationDAO.findById(rid);
                    if (r == null || r.getStatus() == null || !"EN_ATTENTE".equals(r.getStatus())) {
                        mv.addItem("error", "Échec: la réservation #" + rid + " n'est plus en statut EN_ATTENTE.");
                        return mv;
                    }
                }
                // check vehicle availability
                models.Vehicule v = vehiculeDAO.findById(vs.vehiculeId);
                if (v != null && v.getAvailableFrom() != null && vs.heureDepart != null) {
                    if (v.getAvailableFrom().after(vs.heureDepart)) {
                        mv.addItem("error", "Échec: le véhicule #" + vs.vehiculeId + " n'est pas disponible pour ce créneau.");
                        return mv;
                    }
                }
            }

            // Persist sub-proposal atomically
            groupingService.persistAssignments(sub);
            mv.addItem("resultMessage", "Assignations du groupe persistées avec succès.");
            mv.addItem("proposal", sub);
            return mv;
        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors de la persistance: " + e.getMessage());
        } catch (Exception e) {
            mv.addItem("error", "Erreur: " + e.getMessage());
        }
        return mv;
    }

    @GetMapping("/assignations/detail")
    public ModelView detailCreneau(@RequestParam("date") String dateStr, @RequestParam("heure") String heureStr) {
        String token = ConfigReader.getCurrentToken();
        if (!isTokenValid(token)) return createErrorView(token);

        ModelView mv = new ModelView("/WEB-INF/views/assignation-detail.jsp");
        try {
            Date date = Date.valueOf(dateStr);
            Time time = Time.valueOf(heureStr);
            List<Reservation> reservations = reservationDAO.findByDateAndTimeAndStatus(date, time, "EN_ATTENTE");
            mv.addItem("date", dateStr);
            mv.addItem("heure", heureStr);
            mv.addItem("reservations", reservations);
        } catch (Exception e) {
            mv.addItem("error", "Erreur: " + e.getMessage());
        }
        return mv;
    }

    @PostMapping("/assignations/assign")
    public ModelView assignCreneau(@RequestParam("date") String dateStr, @RequestParam("heure") String heureStr) {
        String token = ConfigReader.getCurrentToken();
        if (!isTokenValid(token)) return createErrorView(token);

        ModelView mv = new ModelView("/WEB-INF/views/assignation-result.jsp");
        try {
            Date date = Date.valueOf(dateStr);
            Time time = Time.valueOf(heureStr);
            Map<String, Object> res = assignationService.assignerCreneau(date, time);
            mv.addItem("result", res);
        } catch (Exception e) {
            mv.addItem("error", "Erreur lors de l'assignation: " + e.getMessage());
        }
        return mv;
    }

    @GetMapping("/assignations/compute")
    public ModelView computeCreneau(@RequestParam("date") String dateStr, @RequestParam("heure") String heureStr) {
        String token = ConfigReader.getCurrentToken();
        if (!isTokenValid(token)) return createErrorView(token);

        ModelView mv = new ModelView("/WEB-INF/views/assignation-detail.jsp");
        try {
            java.sql.Date date = java.sql.Date.valueOf(dateStr);
            java.sql.Time time = java.sql.Time.valueOf(heureStr);
            // compute proposal server-side (no DB writes)
            AssignmentProposal proposal = groupingService.computeAssignmentsForDate(date);
            mv.addItem("date", dateStr);
            mv.addItem("heure", heureStr);
            mv.addItem("proposal", proposal);
        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors du calcul de la proposition: " + e.getMessage());
        } catch (Exception e) {
            mv.addItem("error", "Erreur: " + e.getMessage());
        }
        return mv;
    }

    @PostMapping("/assignations/confirm")
    public ModelView confirmCreneau(@RequestParam("date") String dateStr, @RequestParam("heure") String heureStr) {
        String token = ConfigReader.getCurrentToken();
        if (!isTokenValid(token)) return createErrorView(token);

        ModelView mv = new ModelView("/WEB-INF/views/assignation-result.jsp");
        try {
            java.sql.Date date = java.sql.Date.valueOf(dateStr);
            java.sql.Time time = java.sql.Time.valueOf(heureStr);
            // Recompute proposal to avoid trusting client-sent payload and to ensure freshness
            AssignmentProposal proposal = groupingService.computeAssignmentsForDate(date);
            // Persist computed proposal in an atomic transaction
            groupingService.persistAssignments(proposal);
            mv.addItem("resultMessage", "Assignations persistées avec succès.");
            mv.addItem("proposal", proposal);
        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors de la persistance: " + e.getMessage());
        } catch (Exception e) {
            mv.addItem("error", "Erreur: " + e.getMessage());
        }
        return mv;
    }

    private boolean isTokenValid(String token) {
        if (token == null || token.trim().isEmpty()) return false;
        try { return tokenDAO.isValidToken(token); } catch (Exception e) { return false; }
    }

    private ModelView createErrorView(String token) {
        ModelView mv = new ModelView("/WEB-INF/views/auth-error.jsp");
        if (token == null || token.trim().isEmpty()) {
            mv.addItem("errorTitle", "Token non configuré");
            mv.addItem("errorMessage", "Aucun token n'est configuré dans application.properties.");
            mv.addItem("errorDetail", "Exécutez TokenGenerator pour générer un token, ou ajoutez manuellement api.token=VOTRE_TOKEN dans application.properties");
        } else {
            mv.addItem("errorTitle", "Token invalide ou expiré");
            mv.addItem("errorMessage", "Le token configuré dans application.properties n'est pas valide ou a expiré.");
            mv.addItem("errorDetail", "Exécutez TokenGenerator pour générer un nouveau token valide, ou modifiez api.token dans application.properties");
        }
        mv.addItem("providedToken", token);
        return mv;
    }
}
