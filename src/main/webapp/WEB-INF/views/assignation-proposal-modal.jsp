<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="models.AssignmentProposal" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ include file="includes/assignation-styles.jsp" %>
<%
    AssignmentProposal proposal = (AssignmentProposal) request.getAttribute("proposal");
    Integer groupIndex = (Integer) request.getAttribute("groupIndex");
    String date = (String) request.getAttribute("date");
    if (proposal == null || groupIndex == null) {
%>
<div style="padding:20px; color:#b91c1c;">Aucune proposition disponible.</div>
<div style="text-align:right; margin-top:12px;"><button onclick="closeModal()" class="btn btn-secondary">Fermer</button></div>
<%
    } else {
        AssignmentProposal.GroupProposal gp = proposal.getGroups().get(groupIndex);
%>
<div style="padding:12px;">
    <h2 style="margin-top:0;">Proposition pour le groupe #<%= groupIndex+1 %></h2>
    <p style="color:var(--text-secondary);">Date: <%= date %> — Départ groupe: <%= gp.departureTime != null ? gp.departureTime.toString() : "—" %></p>

    <h3>Réservations ehhhhh</h3>
    <table style="width:100%; border-collapse:collapse; margin-top:8px;">
        <thead>
            <tr style="text-align:left; border-bottom:1px solid #eee;">
                <th>ID</th>
                <th>Véhicule proposé</th>
                <th>Raison</th>
            </tr>
        </thead>
        <tbody>
            <% for (AssignmentProposal.ReservationProposal rp : gp.reservations) { %>
            <tr>
                <td style="padding:8px 6px;">#<%= rp.reservationId %></td>
                <td style="padding:8px 6px;"><%= rp.proposedVehiculeId != null ? "#"+rp.proposedVehiculeId : "—" %></td>
                <td style="padding:8px 6px;"><%= rp.reason != null ? rp.reason : "" %></td>
            </tr>
            <% } %>
        </tbody>
    </table>

    <h3 style="margin-top:14px;">Résumé par véhicule</h3>
    <div style="margin-top:8px;">
        <table style="width:100%; border-collapse:collapse;">
            <thead>
                <tr style="text-align:left; border-bottom:1px solid #eee;">
                    <th>Véhicule</th>
                    <th>Réservations</th>
                    <th>Kilométrage (est.)</th>
                    <th>Heure départ</th>
                    <th>Heure arrivée</th>
                </tr>
            </thead>
            <tbody>
                <%
                    // show only vehicle summaries that concern this group's reservations
                    java.util.Set<Integer> groupResIds = new java.util.HashSet<>();
                    for (AssignmentProposal.ReservationProposal rp : gp.reservations) groupResIds.add(rp.reservationId);
                    for (AssignmentProposal.VehicleSummary vs : proposal.getVehicleSummaries().values()) {
                        boolean relevant = false;
                        for (Integer rid : vs.reservationIds) { if (groupResIds.contains(rid)) { relevant = true; break; } }
                        if (!relevant) continue;
                %>
                <tr>
                    <td style="padding:8px 6px;">#<%= vs.vehiculeId %></td>
                    <td style="padding:8px 6px;"><%= vs.reservationIds.toString() %></td>
                    <td style="padding:8px 6px;"><%= String.format("%.2f", vs.estimatedKilometrage) %></td>
                    <td style="padding:8px 6px;"><%= vs.heureDepart != null ? vs.heureDepart.toString() : "—" %></td>
                    <td style="padding:8px 6px;"><%= vs.heureArrivee != null ? vs.heureArrivee.toString() : "—" %></td>
                </tr>
                <% } %>
            </tbody>
        </table>
    </div>

    <div style="margin-top:16px; display:flex; gap:8px; justify-content:flex-end; align-items:center;">
        <form method="post" action="<%= request.getContextPath() %>/assignations/confirmGroup" style="display:inline;">
            <input type="hidden" name="date" value="<%= date %>" />
            <input type="hidden" name="index" value="<%= groupIndex %>" />
            <button type="submit" class="btn btn-primary">Confirmer et persister</button>
        </form>
        <button onclick="closeModal()" class="btn btn-secondary">Annuler</button>
    </div>
</div>
<% } %>
