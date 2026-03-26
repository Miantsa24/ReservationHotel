<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="models.Reservation" %>
<%@ page import="models.AssignmentProposal" %>
<%@ page import="dao.ReservationDAO" %>
<%@ page import="dao.VehiculeDAO" %>
<%@ page import="dao.ReservationVehiculeDAO" %>
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
    <script>
        function toggleFrag(id) {
            var el = document.getElementById(id);
            if (!el) return;
            el.style.display = (el.style.display === 'none' || el.style.display === '') ? 'table-row' : 'none';
        }
    </script>
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
                                    <th>Assignés</th>
                                    <th>Restants</th>
                                    <th>Véhicule proposé</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                    ReservationDAO rdao = new ReservationDAO();
                                    VehiculeDAO vdao = new VehiculeDAO();

                                    // Determine which reservations to display: if a specific groupIndex is provided,
                                    // show only that group's reservations; otherwise show all groups' reservations.
                                    Integer requestedGroupIndex = null;
                                    try { requestedGroupIndex = (Integer) request.getAttribute("groupIndex"); } catch (Exception _gi) { requestedGroupIndex = null; }

                                    java.util.List<AssignmentProposal.ReservationProposal> displayReservations = new java.util.ArrayList<>();
                                    if (requestedGroupIndex != null) {
                                        if (requestedGroupIndex >= 0 && requestedGroupIndex < groups.size()) {
                                            AssignmentProposal.GroupProposal selected = groups.get(requestedGroupIndex);
                                            if (selected != null && selected.reservations != null) displayReservations.addAll(selected.reservations);
                                        }
                                    } else {
                                        for (AssignmentProposal.GroupProposal g : groups) {
                                            if (g != null && g.reservations != null) displayReservations.addAll(g.reservations);
                                        }
                                    }

                                    for (AssignmentProposal.ReservationProposal rp : displayReservations) {
                                        models.Reservation r = null;
                                        try { r = rdao.findById(rp.reservationId); } catch (SQLException _e) { r = null; }
                                %>
                                <tr>
                                    <td><span class="badge-id">#<%= rp.reservationId %></span></td>
                                    <td><strong><%= r != null && r.getRefClient() != null ? r.getRefClient() : "—" %></strong></td>
                                    <td><%= r != null && r.getHotelNom() != null ? r.getHotelNom() : "—" %></td>
                                    <td><span class="badge-persons">👤 <%= r != null ? r.getNombrePersonnes() : 0 %></span></td>
                                    <td>
                                        <%
                                            // Sprint 7: Utiliser rp.passengersAssigned pour la proposition (pas encore persistée)
                                            // Fallback sur la DB uniquement si la proposition n'a pas cette info
                                            int assignedSum = rp.passengersAssigned;
                                            if (assignedSum == 0 && r != null) {
                                                try {
                                                    java.sql.Connection _c = dao.DatabaseConnection.getConnection();
                                                    java.sql.PreparedStatement _ps = _c.prepareStatement("SELECT SUM(passengers_assigned) as s FROM reservation_vehicule WHERE id_reservation = ?");
                                                    _ps.setInt(1, r.getId());
                                                    java.sql.ResultSet _rs = _ps.executeQuery();
                                                    if (_rs.next()) assignedSum = _rs.getInt("s");
                                                    _rs.close(); _ps.close();
                                                } catch (Exception _e) { assignedSum = r.getAssignedCount(); }
                                            }
                                        %>
                                        <%= assignedSum %>
                                    </td>
                                    <td>
                                        <%= (r != null ? (r.getNombrePersonnes() - assignedSum) : 0) %>
                                    </td>
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

                                <tr id="frag-<%= rp.reservationId %>" style="display:none;">
                                    <td colspan="7">
                                        <div style="padding:10px; background:#fbfbfb; border-radius:6px;">
                                            <strong>Détails des affectations</strong>
                                            <%
                                                try {
                                                    dao.ReservationVehiculeDAO rvdao = new dao.ReservationVehiculeDAO();
                                                    java.util.List<models.ReservationVehicule> rvs = rvdao.findAllByReservationId(rp.reservationId);
                                                    if (rvs == null || rvs.isEmpty()) {
                                            %>
                                                        <div style="margin-top:6px;">Aucune affectation persistée pour cette réservation.</div>
                                            <%      } else { %>
                                                        <table style="margin-top:8px; width:100%; border-collapse:collapse;">
                                                            <thead>
                                                                <tr>
                                                                    <th>Véhicule</th>
                                                                    <th>Passagers assignés</th>
                                                                </tr>
                                                            </thead>
                                                            <tbody>
                                                            <% for (models.ReservationVehicule rv : rvs) {
                                                                    models.Vehicule _vv = null;
                                                                    try { _vv = new dao.VehiculeDAO().findById(rv.getIdVehicule()); } catch (SQLException _ee) { _vv = null; }
                                                            %>
                                                                <tr>
                                                                    <td><%= _vv != null ? _vv.getMarque() + " (#" + _vv.getId() + ")" : ("Véhicule #" + rv.getIdVehicule()) %></td>
                                                                    <td><%= rv.getPassengersAssigned() %></td>
                                                                </tr>
                                                            <% } %>
                                                            </tbody>
                                                        </table>
                                            <%      }
                                                } catch (Exception _ex) {
                                            %>
                                                    <div style="color:#b91c1c; margin-top:6px;">Erreur lors de la lecture des affectations.</div>
                                            <%  } %>
                                        </div>
                                    </td>
                                </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>

                    <!-- Vehicle summaries (card layout) -->
                    <div style="margin-top:20px;">
                        <h3>Résumé par véhicule</h3>
                        <div class="vehicle-grid">
                            <%
                                // Filter vehicle summaries to the selected group's reservations when applicable
                                java.util.Set<Integer> groupResIds = new java.util.HashSet<>();
                                try {
                                    Integer requestedGroupIndex2 = (Integer) request.getAttribute("groupIndex");
                                    if (requestedGroupIndex2 != null && requestedGroupIndex2 >= 0 && requestedGroupIndex2 < groups.size()) {
                                        AssignmentProposal.GroupProposal sel = groups.get(requestedGroupIndex2);
                                        for (AssignmentProposal.ReservationProposal rp : sel.reservations) groupResIds.add(rp.reservationId);
                                    } else {
                                        // if no groupIndex provided, include all reservations
                                        for (AssignmentProposal.GroupProposal g : groups) for (AssignmentProposal.ReservationProposal rp : g.reservations) groupResIds.add(rp.reservationId);
                                    }
                                } catch (Exception _gex) {
                                    // fallback: include all
                                    for (AssignmentProposal.GroupProposal g : groups) for (AssignmentProposal.ReservationProposal rp : g.reservations) groupResIds.add(rp.reservationId);
                                }

                                for (AssignmentProposal.VehicleSummary vs : proposal.getVehicleSummaries().values()) {
                                    // show only vehicle summaries that concern this group's reservations
                                    boolean relevant = false;
                                    for (Integer rid : vs.reservationIds) { if (groupResIds.contains(rid)) { relevant = true; break; } }
                                    if (!relevant) continue;
                                    models.Vehicule _v = null;
                                    try { _v = new VehiculeDAO().findById(vs.vehiculeId); } catch (SQLException _e) { _v = null; }
                            %>
                            <div class="vehicle-card">
                                <div>
                                    <div class="vehicle-title"><%= _v != null ? (_v.getMarque() + " (capacité: " + _v.getCapacite() + ")") : ("Véhicule #"+vs.vehiculeId) %></div>
                                    <div class="vehicle-meta">ID: <%= vs.vehiculeId %></div>
                                </div>

                                <div class="vehicle-reservations">
                                    <%-- show small chips for each reservation with client ref and passengers assigned to THIS vehicle --%>
                                    <% for (Integer rid : vs.reservationIds) {
                                            if (!groupResIds.contains(rid)) continue;
                                            models.Reservation rr = null;
                                            try { rr = new ReservationDAO().findById(rid); } catch (SQLException _e) { rr = null; }
                                            // Sprint 7: utiliser passengersPerReservation pour afficher le nombre assigné à ce véhicule
                                            int passengersInThisVehicle = vs.passengersPerReservation.getOrDefault(rid, 0);
                                    %>
                                        <div class="reservation-chip">
                                            <span style="opacity:.8;">#<%= rid %></span>
                                            <span style="color:var(--text-primary);"> <%= rr != null && rr.getRefClient() != null ? rr.getRefClient() : "—" %></span>
                                            <span style="color:var(--text-secondary); font-weight:600;">• <%= passengersInThisVehicle %> pers. assignées</span>
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
