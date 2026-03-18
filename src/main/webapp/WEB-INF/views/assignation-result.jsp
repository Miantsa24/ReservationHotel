<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.Map" %>
<%@ page import="models.AssignmentProposal" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Résultat d'assignation</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <%@ include file="includes/assignation-styles.jsp" %>
</head>
<body>
    <div class="app-layout">
        <%@ include file="includes/sidebar.jsp" %>
        <div class="main-content">
            <div class="page-header">
                <h1 class="page-title">Assignation terminée</h1>
                <p class="page-subtitle">Résultat de l'algorithme Largest-First</p>
            </div>

            <div class="card">
                <% if (request.getAttribute("error") != null) { %>
                    <div class="alert-error">
                        <span>⚠️</span>
                        <span><%= request.getAttribute("error") %></span>
                    </div>
                <% } %>
                
                <%
                    AssignmentProposal proposal = (AssignmentProposal) request.getAttribute("proposal");
                    String resultMessage = request.getAttribute("resultMessage") != null ? request.getAttribute("resultMessage").toString() : null;
                    Map<String,Object> res = (Map<String,Object>) request.getAttribute("result");
                    if (proposal != null) {
                        // show detailed proposal persisted
                %>
                    <div class="success-icon">✓</div>
                    <h2 style="margin-top:8px;">Assignations persistées</h2>
                    <% if (resultMessage != null) { %>
                        <p style="color: #047857; font-weight:700;"><%= resultMessage %></p>
                    <% } %>
                    <div style="margin-top:18px; text-align:left;">
                        <h3>Détails par véhicule</h3>
                        <div class="table-container">
                            <table class="vehicle-summary-table">
                                <thead>
                                    <tr>
                                        <th style="width:120px;">Véhicule</th>
                                        <th>Réservations</th>
                                        <th style="width:160px;">Heure départ</th>
                                        <th style="width:160px;">Heure arrivée</th>
                                        <th style="width:160px;">Kilométrage (km)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (AssignmentProposal.VehicleSummary vs : proposal.getVehicleSummaries().values()) { %>
                                    <tr>
                                        <td style="font-weight:800;">#<%= vs.vehiculeId %></td>
                                        <td><%= vs.reservationIds.toString() %></td>
                                        <td><%= vs.heureDepart != null ? vs.heureDepart.toString().substring(0,19) : "—" %></td>
                                        <td><%= vs.heureArrivee != null ? vs.heureArrivee.toString().substring(0,19) : "—" %></td>
                                        <td><%= vs.estimatedKilometrage > 0 ? String.format("%.2f", vs.estimatedKilometrage) : "—" %></td>
                                    </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                <%
                    } else if (res != null) {
                        int assigned = res.get("assigned") != null ? (Integer) res.get("assigned") : 0;
                        int notAssigned = res.get("notAssigned") != null ? (Integer) res.get("notAssigned") : 0;
                %>
                    <div class="success-icon">✓</div>
                    
                    <div class="stats-grid">
                        <div class="stat-card success">
                            <div class="stat-value"><%= assigned %></div>
                            <div class="stat-label">Réservations assignées</div>
                        </div>
                        <div class="stat-card warning">
                            <div class="stat-value"><%= notAssigned %></div>
                            <div class="stat-label">Non assignées</div>
                        </div>
                    </div>
                    
                    <% if (res.get("error") != null) { %>
                        <div class="alert-error">
                            <span>⚠️</span>
                            <span><%= res.get("error") %></span>
                        </div>
                    <% } %>
                <% } else { %>
                    <p class="no-result">Aucun résultat disponible</p>
                <% } %>
                
                <div class="action-bar">
                    <a href="<%= request.getContextPath() %>/assignations" class="btn btn-primary">
                        <span>📅</span>
                        <span>Retour aux dates</span>
                    </a>
                    <a href="<%= request.getContextPath() %>/tracabilite" class="btn btn-secondary">
                        <span>🚐</span>
                        <span>Voir la traçabilité</span>
                    </a>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
