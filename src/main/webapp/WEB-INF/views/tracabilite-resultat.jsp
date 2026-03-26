<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
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
                    <div class="date-header">
                        📅 Traçabilité du <strong><%= request.getAttribute("date") %></strong>
                    </div>
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

                    if (countAssignees == null) countAssignees = 0;
                    if (countNonAssignees == null) countNonAssignees = 0;
                    if (countEnAttente == null) countEnAttente = 0;
                    if (totalVehicules == null) totalVehicules = 0;

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
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

                    <% int cardIndex = 0; 
                       for (VehiculeTracabilite vt : tracabilites) { 
                           cardIndex++; %>

                        <div class="vehicule-card" style="animation-delay: <%= (cardIndex * 0.1) %>s;">

                            <div class="vehicule-header">
                                <div class="vehicule-name">
                                    🚐 <%= vt.getVehicule().getMarque() %>
                                </div>

                                <div class="vehicule-tags">
                                    <span class="tag tag-capacite">
                                        👤 <%= vt.getVehicule().getCapacite() %> places
                                    </span>
                                    <span class="tag tag-carburant">
                                        ⛽ <%= vt.getVehicule().getTypeCarburant() %>
                                    </span>
                                    <span class="tag tag-vitesse">
                                        ⚡ <%= vt.getVehicule().getVitesseMoyenne() %> km/h
                                    </span>
                                    <span class="tag tag-trajets">
                                        🚏 <%= vt.getVehicule().getTrajetsEffectues() %> trajets
                                    </span>
                                </div>
                            </div>

                            <% if (vt.getTrajets() != null && !vt.getTrajets().isEmpty()) { 
                                   int tIndex = 0;

                                   for (models.TrajetTracabilite tt : vt.getTrajets()) { 
                                       tIndex++; %>

                                <div style="margin-top:20px; padding:15px; border:1px solid #ddd; border-radius:10px;">

                                    <div class="section-title">
                                        🚏 Trajet <%= tIndex %>
                                        (<%= tt.getTrajet().getHeureDepart() != null ? sdf.format(tt.getTrajet().getHeureDepart()) : "—" %>)
                                    </div>

                                    <div class="info-grid">

                                        <div class="info-box">
                                            <div class="info-box-label">🛫 Départ</div>
                                            <div class="info-box-value">
                                                <%= tt.getTrajet().getHeureDepart() != null ? sdf.format(tt.getTrajet().getHeureDepart()) : "—" %>
                                            </div>
                                        </div>

                                        <div class="info-box">
                                            <div class="info-box-label">🛬 Retour</div>
                                            <div class="info-box-value">
                                                <%= tt.getTrajet().getHeureArrivee() != null ? sdf.format(tt.getTrajet().getHeureArrivee()) : "—" %>
                                            </div>
                                        </div>

                                        <div class="info-box">
                                            <div class="info-box-label">🧭 Distance</div>
                                            <div class="info-box-value">
                                                <%= String.format("%.2f km", tt.getTrajet().getKilometrageParcouru()) %>
                                            </div>
                                        </div>

                                    </div>

                                    <!-- Parcours -->
                                    <div class="section-title">🏨 Parcours</div>
                                    <div class="parcours-container">
                                        <div class="parcours-step">Aéroport</div>

                                        <% if (tt.getHotels() != null) {
                                               for (String h : tt.getHotels()) { %>
                                            <div class="parcours-arrow">→</div>
                                            <div class="parcours-step"><%= h %></div>
                                        <% }} %>

                                        <div class="parcours-arrow">→</div>
                                        <div class="parcours-step">Aéroport</div>
                                    </div>

                                    <!-- Réservations -->
                                    <div class="section-title">
                                        📋 Réservations (<%= tt.getReservations() != null ? tt.getReservations().size() : 0 %>)
                                    </div>

                                    <table>
                                        <thead>
                                            <tr>
                                                <th>ID</th>
                                                <th>Client</th>
                                                <th>Hôtel</th>
                                                <th>Heure</th>
                                                <th>Personnes</th>
                                            </tr>
                                        </thead>
                                        <tbody>

                                            <% for (Reservation r : tt.getReservations()) { %>
                                                <tr>
                                                    <td>#<%= r.getId() %></td>
                                                    <td><%= r.getRefClient() %></td>
                                                    <td><%= r.getHotelNom() %></td>
                                                    <td>
                                                        <%= r.getHeureArrivee() != null ? sdf.format(r.getHeureArrivee()) : "—" %>
                                                    </td>
                                                    <td><%= r.getNombrePersonnes() %></td>
                                                </tr>
                                            <% } %>

                                        </tbody>
                                    </table>

                                    <!-- Étapes -->
                                    <div class="section-title">⏱️ Étapes</div>

                                    <table>
                                        <tr>
                                            <th>Étape</th>
                                            <th>Heure</th>
                                        </tr>

                                        <tr>
                                            <td>Aéroport</td>
                                            <td>
                                                <%= tt.getTrajet().getHeureDepart() != null ? sdf.format(tt.getTrajet().getHeureDepart()) : "—" %>
                                            </td>
                                        </tr>

                                        <%
                                            List<java.sql.Time> etapes = tt.getEtapeHeures();

                                            if (tt.getHotels() != null && etapes != null) {
                                            for (int i = 0; i < tt.getHotels().size(); i++) {

                                                String hTime = "—";

                                                if (etapes.size() > i+1 && etapes.get(i+1) != null) {
                                                    hTime = sdf.format(etapes.get(i+1));
                                            }
                                        %>
                                            <tr>
                                                <td><%= tt.getHotels().get(i) %></td>
                                                <td><%= hTime %></td>
                                            </tr>
                                        <%      }
                                            } %>

                                        <tr>
                                            <td>Aéroport</td>
                                            <td>
                                                <%= tt.getTrajet().getHeureArrivee() != null ? sdf.format(tt.getTrajet().getHeureArrivee()) : "—" %>
                                            </td>
                                        </tr>
                                    </table>

                                </div> <!-- fin trajet -->

                            <% 
                                } // fin for trajets
                            } // fin if trajets 
                            %>

                        </div> <!-- fin véhicule -->

                    <% } %>

                <% } else { %>
                    <div class="no-data">
                        <div class="no-data-icon">📭</div>
                        <p>Aucun véhicule avec des trajets pour cette date.</p>
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