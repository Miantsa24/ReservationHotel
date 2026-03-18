<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="models.Reservation" %>
<%@ page import="models.AssignmentProposal" %>
<%@ page import="dao.ReservationDAO" %>
<%@ page import="dao.VehiculeDAO" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.sql.Date" %>
<%@ page import="java.sql.Time" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Détail du créneau</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <%@ include file="includes/assignation-styles.jsp" %>
    <!-- Fallback styles (harmonized with central variables) in case the main include is missing -->
    <style id="fallback-assignation-detail">
        :root{ --primary-color:#4299e1; --primary-dark:#3182ce; --text-primary:#1e293b; --text-secondary:#64748b; --card-bg:#ffffff; --border-radius:12px; }
        *{box-sizing:border-box;margin:0;padding:0}
        body{font-family:Inter,Segoe UI,Roboto,Arial,sans-serif;background:linear-gradient(135deg,#f8fafc 0%,#e2e8f0 100%);color:var(--text-primary);line-height:1.5}
        .app-layout{display:flex;gap:24px;max-width:1200px;margin:0 auto;padding:20px}
        .main-content{flex:1}
        .card{background:var(--card-bg);padding:20px;border-radius:var(--border-radius);border:1px solid rgba(0,0,0,0.04)}
        .page-title{font-size:28px;font-weight:800;color:var(--primary-dark);margin-bottom:8px}
        .page-subtitle{color:var(--text-secondary);font-size:14px}
        .breadcrumb{display:flex;gap:8px;color:var(--text-secondary);margin-bottom:16px}
        .table-container table{width:100%;border-collapse:collapse;background:transparent}
        .table-container th,.table-container td{padding:10px;border-bottom:1px solid rgba(0,0,0,0.04);text-align:left}
        .vehicle-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(260px,1fr));gap:12px;margin-top:12px}
        .vehicle-card{background:var(--card-bg);border-radius:10px;padding:12px;box-shadow:0 4px 10px rgba(0,0,0,0.04)}
        .reservation-chip{display:inline-flex;gap:6px;padding:6px 8px;border-radius:999px;background:rgba(0,0,0,0.03);margin-right:6px;margin-bottom:6px}
        .btn{display:inline-flex;align-items:center;gap:8px;padding:8px 12px;border-radius:10px;border:none;cursor:pointer}
        .btn-primary{background:linear-gradient(135deg,var(--primary-color) 0%,var(--primary-dark) 100%);color:#fff}
        .btn-secondary{background:rgba(66,153,225,0.08);color:var(--primary-dark)}
        @media (max-width:900px){.app-layout{flex-direction:column;padding:12px}.vehicle-grid{grid-template-columns:1fr}}
    </style>
</head>
<body>
    <div class="app-layout">
        <%@ include file="includes/sidebar.jsp" %>
        <div class="main-content">
            <div class="breadcrumb">
                <a href="<%= request.getContextPath() %>/assignations">Assignations</a>
                <span>›</span>
                <a href="<%= request.getContextPath() %>/assignations/hours?date=<%= request.getAttribute("date") %>"><%= request.getAttribute("date") %></a>
                <span>›</span>
                <span><%= request.getAttribute("heure") %></span>
            </div>
            
            <div class="page-header">
                <h1 class="page-title">Détail du créneau</h1>
                <p class="page-subtitle">Réservations en attente d'assignation pour ce créneau</p>
            </div>
            
            <div class="info-badges">
                <span class="info-badge date">📅 <%= request.getAttribute("date") %></span>
                <span class="info-badge time">⏱️ <%= request.getAttribute("heure") %></span>
            </div>

            <div class="card">
                <% if (request.getAttribute("proposal") != null) {
                    AssignmentProposal proposal = (AssignmentProposal) request.getAttribute("proposal");
                    // flatten reservations for display
                    List<AssignmentProposal.GroupProposal> groups = proposal.getGroups();
                %>
                    <div class="card-header">
                        <h2 class="card-title">Proposition d'assignation</h2>
                        <span class="card-count"><%= groups.stream().mapToInt(g -> g.reservations.size()).sum() %> réservation(s)</span>
                    </div>

                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Référence Client</th>
                                    <th>Hôtel</th>
                                    <th>Personnes</th>
                                    <th>Véhicule proposé</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                    ReservationDAO rdao = new ReservationDAO();
                                    VehiculeDAO vdao = new VehiculeDAO();
                                    for (AssignmentProposal.GroupProposal g : groups) {
                                        for (AssignmentProposal.ReservationProposal rp : g.reservations) {
                                            models.Reservation r = null;
                                            try { r = rdao.findById(rp.reservationId); } catch (SQLException _e) { r = null; }
                                %>
                                <tr>
                                    <td><span class="badge-id">#<%= rp.reservationId %></span></td>
                                    <td><strong><%= r != null && r.getRefClient() != null ? r.getRefClient() : "—" %></strong></td>
                                    <td><%= r != null && r.getHotelNom() != null ? r.getHotelNom() : "—" %></td>
                                    <td><span class="badge-persons">👤 <%= r != null ? r.getNombrePersonnes() : 0 %></span></td>
                                    <td>
                                        <% if (rp.proposedVehiculeId != null) {
                                            models.Vehicule v = null;
                                            try { v = vdao.findById(rp.proposedVehiculeId); } catch (SQLException _e) { v = null; }
                                        %>
                                            <% if (v != null) { %>
                                                <%= v.getMarque() %> (capacité: <%= v.getCapacite() %>)
                                            <% } else { %>
                                                Véhicule #<%= rp.proposedVehiculeId %>
                                            <% } %>
                                        <% } else { %>
                                            <span style="color: #b91c1c; font-weight:700;">NON_ASSIGNÉ</span>
                                        <% } %>
                                    </td>
                                </tr>
                                <%     }
                                    }
                                %>
                            </tbody>
                        </table>
                    </div>

                    <!-- Vehicle summaries (card layout) -->
                    <div style="margin-top:20px;">
                        <h3>Résumé par véhicule</h3>
                        <div class="vehicle-grid">
                            <% for (AssignmentProposal.VehicleSummary vs : proposal.getVehicleSummaries().values()) {
                                    models.Vehicule _v = null;
                                    try { _v = new VehiculeDAO().findById(vs.vehiculeId); } catch (SQLException _e) { _v = null; }
                            %>
                            <div class="vehicle-card">
                                <div>
                                    <div class="vehicle-title"><%= _v != null ? (_v.getMarque() + " (capacité: " + _v.getCapacite() + ")") : ("Véhicule #"+vs.vehiculeId) %></div>
                                    <div class="vehicle-meta">ID: <%= vs.vehiculeId %></div>
                                </div>

                                <div class="vehicle-reservations">
                                    <%-- show small chips for each reservation with client ref and persons --%>
                                    <% for (Integer rid : vs.reservationIds) {
                                            models.Reservation rr = null;
                                            try { rr = new ReservationDAO().findById(rid); } catch (SQLException _e) { rr = null; }
                                    %>
                                        <div class="reservation-chip">
                                            <span style="opacity:.8;">#<%= rid %></span>
                                            <span style="color:var(--text-primary);"> <%= rr != null && rr.getRefClient() != null ? rr.getRefClient() : "—" %></span>
                                            <span style="color:var(--text-secondary); font-weight:600;">• <%= rr != null ? rr.getNombrePersonnes() : 0 %> pers.</span>
                                        </div>
                                    <% } %>
                                </div>

                                <div class="vehicle-stats">
                                    <div class="stat">Départ: <strong><%= vs.heureDepart != null ? vs.heureDepart.toString().substring(11,19) : "—" %></strong></div>
                                    <div class="stat">Arrivée: <strong><%= vs.heureArrivee != null ? vs.heureArrivee.toString().substring(11,19) : "—" %></strong></div>
                                    <%
                                        double kmVal = vs.estimatedKilometrage;
                                        String kmDisplay;
                                        if (Double.isInfinite(kmVal) || Double.isNaN(kmVal)) {
                                            kmDisplay = "N/A";
                                        } else if (kmVal == 0.0) {
                                            kmDisplay = "—";
                                        } else {
                                            kmDisplay = String.format("%.2f", kmVal);
                                        }
                                    %>
                                    <div class="stat">Km estimé: <strong><%= kmDisplay %></strong></div>
                                </div>
                            </div>
                            <% } %>
                        </div>
                    </div>

                    <div class="action-bar">
                        <% if (request.getAttribute("groupIndex") != null) { %>
                        <form action="<%= request.getContextPath() %>/assignations/confirmGroup" method="post" style="margin:0;">
                            <input type="hidden" name="date" value="<%= request.getAttribute("date") %>">
                            <input type="hidden" name="index" value="<%= request.getAttribute("groupIndex") %>">
                            <button type="submit" class="btn btn-primary">
                                <span>✅</span>
                                <span>Confirmer et persister</span>
                            </button>
                        </form>
                        <% } else { %>
                        <form action="<%= request.getContextPath() %>/assignations/confirm" method="post" style="margin:0;">
                            <input type="hidden" name="date" value="<%= request.getAttribute("date") %>">
                            <input type="hidden" name="heure" value="<%= request.getAttribute("heure") %>">
                            <button type="submit" class="btn btn-primary">
                                <span>✅</span>
                                <span>Confirmer et persister</span>
                            </button>
                        </form>
                        <% } %>
                        <a href="<%= request.getContextPath() %>/assignations/hours?date=<%= request.getAttribute("date") %>" class="btn btn-secondary">
                            <span>←</span>
                            <span>Retour</span>
                        </a>
                    </div>

                <% } else if (request.getAttribute("reservations") != null) {
                    List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
                %>
                    <div class="card-header">
                        <h2 class="card-title">Réservations à assigner</h2>
                        <span class="card-count"><%= reservations.size() %> réservation(s)</span>
                    </div>
                    
                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Référence Client</th>
                                    <th>Hôtel</th>
                                    <th>Personnes</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (Reservation r : reservations) { %>
                                <tr>
                                    <td><span class="badge-id">#<%= r.getId() %></span></td>
                                    <td><strong><%= r.getRefClient() %></strong></td>
                                    <td><%= r.getHotelNom() %></td>
                                    <td><span class="badge-persons">👤 <%= r.getNombrePersonnes() %></span></td>
                                </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                    
                    <div class="action-bar">
                        <form action="<%= request.getContextPath() %>/assignations/assign" method="post" style="margin: 0;">
                            <input type="hidden" name="date" value="<%= request.getAttribute("date") %>">
                            <input type="hidden" name="heure" value="<%= request.getAttribute("heure") %>">
                            <button type="submit" class="btn btn-primary">
                                <span>🚀</span>
                                <span>Lancer l'assignation (Largest-First)</span>
                            </button>
                        </form>
                        <a href="<%= request.getContextPath() %>/assignations/hours?date=<%= request.getAttribute("date") %>" class="btn btn-secondary">
                            <span>←</span>
                            <span>Retour aux heures</span>
                        </a>
                    </div>
                <% } else { %>
                    <div class="empty-state">
                        <div class="empty-state-icon">📭</div>
                        <p class="empty-state-text">Aucune réservation trouvée pour ce créneau</p>
                    </div>
                <% } %>
            </div>
        </div>
    </div>
</body>
</html>
