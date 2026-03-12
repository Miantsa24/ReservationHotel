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

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Map;

@Controller
public class AssignationController {

    private ReservationDAO reservationDAO = new ReservationDAO();
    private TokenDAO tokenDAO = new TokenDAO();
    private AssignationService assignationService = new AssignationService();

    @GetMapping("/assignations")
    public ModelView listDates() {
        String token = ConfigReader.getCurrentToken();
        if (!isTokenValid(token)) return createErrorView(token);

        ModelView mv = new ModelView("/WEB-INF/views/assignation-list.jsp");
        try {
            List<Date> dates = reservationDAO.findDistinctDatesByStatus("EN_ATTENTE");
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
            List<Time> hours = reservationDAO.findDistinctHoursByDateAndStatus(date, "EN_ATTENTE");
            mv.addItem("date", dateStr);
            mv.addItem("hours", hours);
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
