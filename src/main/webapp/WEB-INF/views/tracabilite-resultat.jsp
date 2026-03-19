<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="models.VehiculeTracabilite" %>
<%@ page import="models.Reservation" %>
<%@ page import="models.Vehicule" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Résultat Traçabilité - Back-office Hôtel</title>
    <%@ include file="includes/assignation-styles.jsp" %>
</head>
<body>
    <div class="app-layout">
        <%@ include file="includes/sidebar.jsp" %>
        <div class="main-content">
            <div class="container">
                <div class="page-header">
                    <h1 class="page-title">🚐 Traçabilité des Véhicules</h1>
                    <div class="date-header">📅 Traçabilité du <strong><%= request.getAttribute("date") %></strong></div>
                </div>

                <% if (request.getAttribute("error") != null) { %>
                    <div class="alert-error">
                        <span>⚠️</span>
                        <span><%= request.getAttribute("error") %></span>
                    </div>
                <% } %>

                <%
                    List<VehiculeTracabilite> tracabilites = (List<VehiculeTracabilite>) request.getAttribute("tracabilites");
                    Integer totalVehicules = (Integer) request.getAttribute("totalVehicules");
                    Integer countAssignees = (Integer) request.getAttribute("countAssignees");
                    Integer countNonAssignees = (Integer) request.getAttribute("countNonAssignees");
                    Integer countEnAttente = (Integer) request.getAttribute("countEnAttente");
                    Integer totalReservations = (Integer) request.getAttribute("totalReservations");
                    
                    if (countAssignees == null) countAssignees = 0;
                    if (countNonAssignees == null) countNonAssignees = 0;
                    if (countEnAttente == null) countEnAttente = 0;
                    if (totalReservations == null) totalReservations = 0;
                    if (totalVehicules == null) totalVehicules = 0;
                %>

                <!-- Dashboard -->
                <div class="dashboard">
                    <div class="dashboard-card success">
                        <div class="dashboard-icon">✅</div>
                        <div class="dashboard-value"><%= countAssignees %></div>
                        <div class="dashboard-label">Réservations assignées</div>
                    </div>
                    <div class="dashboard-card warning">
                        <div class="dashboard-icon">❌</div>
                        <div class="dashboard-value"><%= countNonAssignees %></div>
                        <div class="dashboard-label">Non assignées</div>
                    </div>
                    <div class="dashboard-card info">
                        <div class="dashboard-icon">⏳</div>
                        <div class="dashboard-value"><%= countEnAttente %></div>
                        <div class="dashboard-label">En attente</div>
                    </div>
                    <div class="dashboard-card primary">
                        <div class="dashboard-icon">🚐</div>
                        <div class="dashboard-value"><%= totalVehicules %></div>
                        <div class="dashboard-label">Véhicules actifs</div>
                    </div>
                </div>

                <% if (tracabilites != null && !tracabilites.isEmpty()) { %>
                    <% int cardIndex = 0; for (VehiculeTracabilite vt : tracabilites) { cardIndex++; %>
                        <div class="vehicule-card" style="animation-delay: <%= (cardIndex * 0.1) %>s;">
                            <div class="vehicule-header">
                                <div class="vehicule-name">🚐 <%= vt.getVehicule().getMarque() %></div>
                                <div class="vehicule-tags">
                                    <span class="tag tag-capacite">👤 <%= vt.getVehicule().getCapacite() %> places</span>
                                    <span class="tag tag-carburant">⛽ <%= vt.getVehicule().getTypeCarburant() %></span>
                                    <span class="tag tag-vitesse">⚡ <%= vt.getVehicule().getVitesseMoyenne() %> km/h</span>
                                    <span class="tag tag-trajets">🚏 <%= vt.getVehicule().getTrajetsEffectues() %> trajets</span>
                                </div>
                            </div>

                            <div class="info-grid">
                                <div class="info-box">
                                    <div class="info-box-label">🛫 Départ Aéroport</div>
                                    <div class="info-box-value">
                                        <%= vt.getHeureDepart() != null ? vt.getHeureDepart() : "—" %>
                                    </div>
                                </div>
                                <div class="info-box">
                                    <div class="info-box-label">🛬 Retour Aéroport</div>
                                    <div class="info-box-value">
                                        <%= vt.getHeureRetour() != null ? vt.getHeureRetour() : "—" %>
                                    </div>
                                </div>
                                <div class="info-box">
                                    <div class="info-box-label">🧭 Kilométrage parcouru</div>
                                    <div class="info-box-value">
                                        <%= String.format("%.2f km", vt.getDistanceTotale()) %>
                                    </div>
                                </div>
                            </div>

                            <div class="section-title">🏨 Parcours</div>
                            <div class="parcours-container">
                                <div class="parcours-step">Aéroport</div>
                                <% if (vt.getHotels() != null && !vt.getHotels().isEmpty()) { %>
                                    <% for (String hotel : vt.getHotels()) { %>
                                        <div class="parcours-arrow">→</div>
                                        <div class="parcours-step"><%= hotel %></div>
                                    <% } %>
                                <% } %>
                                <div class="parcours-arrow">→</div>
                                <div class="parcours-step">Aéroport</div>
                            </div>

                            <div class="section-title">📋 Réservations assignées (<%= vt.getReservations().size() %>)</div>
                            <table>
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Réf. Client</th>
                                        <th>Hôtel</th>
                                        <th>Heure</th>
                                        <th>Personnes</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (Reservation r : vt.getReservations()) { 
                                            // compute estimated arrival time for this reservation from vt.etapeHeures
                                            java.sql.Time est = null;
                                            java.util.List<java.sql.Time> et = vt.getEtapeHeures();
                                            if (et != null && vt.getHotels() != null) {
                                                int idx = vt.getHotels().indexOf(r.getHotelNom());
                                                if (idx >= 0 && et.size() > idx+1) est = et.get(idx+1);
                                            }
                                    %>
                                    <tr>
                                        <td><span class="badge badge-id">#<%= r.getId() %></span></td>
                                        <td><strong><%= r.getRefClient() %></strong></td>
                                        <td><%= r.getHotelNom() %></td>
                                        <td><%= est != null ? est.toString().substring(0,8) : (r.getHeureArrivee()!=null? r.getHeureArrivee().toString().substring(0,8): "—") %></td>
                                        <td><span class="badge badge-persons">👤 <%= r.getNombrePersonnes() %></span></td>
                                    </tr>
                                    <% } %>
                                </tbody>
                            </table>

                            <%-- Removed JSON list: informations already shown in the reservations table above --%>

                            <div style="margin-top:12px">
                                <div class="section-title">⏱️ Détail des étapes (heures)</div>
                                <table>
                                    <thead>
                                        <tr><th>Étape</th><th>Horaire</th></tr>
                                    </thead>
                                    <tbody>
                                        <tr><td>Aéroport (départ)</td><td><%= vt.getHeureDepart() != null ? vt.getHeureDepart() : "—" %></td></tr>
                                        <% if (vt.getHotels() != null && !vt.getHotels().isEmpty()) {
                                            java.util.List<java.sql.Time> etapes = vt.getEtapeHeures();
                                            for (int hidx = 0; hidx < vt.getHotels().size(); hidx++) {
                                                String hotelName = vt.getHotels().get(hidx);
                                                String displayTime = "—";
                                                // prefer computed etape time (etapes list: [Aéroport, h1, h2, ..., Aéroport])
                                                if (etapes != null && etapes.size() > hidx+1 && etapes.get(hidx+1) != null) {
                                                    try { displayTime = etapes.get(hidx+1).toString().substring(0,8); } catch (Exception _e) { displayTime = etapes.get(hidx+1).toString(); }
                                                } else {
                                                    // fallback: concatenate reservation declared times for this hotel
                                                    StringBuilder times = new StringBuilder();
                                                    for (Reservation rr : vt.getReservations()) {
                                                        if (hotelName != null && hotelName.equals(rr.getHotelNom())) {
                                                            if (times.length() > 0) times.append(", ");
                                                            if (rr.getHeureArrivee() != null) times.append(rr.getHeureArrivee().toString().substring(0,8)); else times.append("—");
                                                        }
                                                    }
                                                    if (times.length() > 0) displayTime = times.toString();
                                                }
                                        %>
                                            <tr>
                                                <td><%= hotelName %></td>
                                                <td><%= displayTime %></td>
                                            </tr>
                                        <%  }
                                        } %>
                                        <tr><td>Aéroport (retour)</td><td><%= vt.getHeureRetour() != null ? vt.getHeureRetour() : "—" %></td></tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    <% } %>
                <% } else { %>
                    <div class="no-data">
                        <div class="no-data-icon">📭</div>
                        <p>Aucun véhicule avec des réservations pour cette date.</p>
                    </div>
                <% } %>

                <div class="footer">
                    <a href="<%= request.getContextPath() %>/tracabilite" class="back-link">
                        <span>←</span>
                        <span>Retour</span>
                    </a>
                </div>
            </div>
        </div>
    </div>
</body>
</html>