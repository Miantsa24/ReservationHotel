<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.sql.Time" %>
<%@ page import="models.AssignmentProposal" %>
<%@ page import="dao.ReservationDAO" %>
    <%@ include file="includes/assignation-styles.jsp" %>
        
        <!-- @media (max-width: 900px) {
            .app-layout { flex-direction: column; padding: 16px; }
            .hours-grid { grid-template-columns: 1fr; }
            .page-title { font-size: 26px; }
        } -->
    </style>
</head>
<body>
    <div class="app-layout">
        <%@ include file="includes/sidebar.jsp" %>
        <div class="main-content">
            <div class="breadcrumb">
                <a href="<%= request.getContextPath() %>/assignations">Assignations</a>
                <span>›</span>
                <span><%= request.getAttribute("date") %></span>
            </div>
            
            <div class="page-header">
                <h1 class="page-title">Assignations par date</h1>
                <p class="page-subtitle">Groupes formés selon le temps d'attente. Ouvrez une proposition et validez.</p>
            </div>

            <div class="date-badge">
                <span>📅</span>
                <span><%= request.getAttribute("date") %></span>
            </div>

            <% if (request.getAttribute("dates") != null) { %>
                <div style="margin:12px 0 20px 0; display:flex; gap:12px; flex-wrap:wrap;">
                    <% java.util.List<java.sql.Date> dates = (java.util.List<java.sql.Date>) request.getAttribute("dates");
                       for (java.sql.Date d : dates) { %>
                        <a href="<%= request.getContextPath() %>/assignations?date=<%= d.toString() %>" class="btn btn-primary" style="display:inline-block; padding:10px 14px; border-radius:12px; text-decoration:none;">📅 <%= d.toString() %></a>
                    <% } %>
                </div>
            <% } %>

            <div class="card">
                <% if (request.getAttribute("proposal") != null) { %>
                    <%
                        AssignmentProposal proposal = (AssignmentProposal) request.getAttribute("proposal");
                        List<AssignmentProposal.GroupProposal> groups = proposal.getGroups();
                        ReservationDAO rdao = new ReservationDAO();
                        if (groups != null && !groups.isEmpty()) {
                    %>
                        <div class="hours-grid">
                            <% for (int i = 0; i < groups.size(); i++) {
                                AssignmentProposal.GroupProposal g = groups.get(i);
                                int resCount = g.reservations != null ? g.reservations.size() : 0;
                                String depStr = g.departureTime != null ? g.departureTime.toString() : "";

                                // Get first reservation's arrival time
                                String firstStr = "—";
                                if (g.reservations != null && !g.reservations.isEmpty()) {
                                    try {
                                        int firstResId = g.reservations.get(0).reservationId;
                                        models.Reservation firstRes = rdao.findById(firstResId);
                                        if (firstRes != null && firstRes.getHeureArrivee() != null) {
                                            firstStr = firstRes.getHeureArrivee().toString();
                                        }
                                    } catch (Exception _e) { /* ignore */ }
                                }
                            %>
                                <a href="<%= request.getContextPath() %>/assignations/group?date=<%= request.getAttribute("date") %>&index=<%= i %>" class="hour-card" style="cursor:pointer; display:block; text-decoration:none;">
                                    <div class="hour-icon">🧩</div>
                                    <div>
                                        <div class="hour-value">Groupe #<%= i+1 %></div>
                                        <div style="font-size:13px;color:var(--text-secondary);margin-top:6px;">
                                            <span><%= resCount %> réservation(s)</span>
                                            &nbsp;•&nbsp;
                                            <span>
                                                <%= (firstStr != null && firstStr.length() >=5) ? firstStr.substring(0,5) : (firstStr != null ? firstStr : "—") %>
                                                -
                                                <%= (depStr != null && depStr.length() >=5) ? depStr.substring(0,5) : (depStr != null && depStr.length()>0 ? depStr : "—") %>
                                            </span>
                                        </div>
                                    </div>
                                </a>
                            <% } %>
                        </div>
                    <% } else { %>
                        <div class="empty-state">
                            <div class="empty-state-icon">🕐</div>
                            <p class="empty-state-text">Aucun groupe disponible pour cette date</p>
                        </div>
                    <% } %>
                <% } else { %>
                    <div class="empty-state">
                        <div class="empty-state-icon">📭</div>
                        <p class="empty-state-text">Aucune donnée disponible</p>
                    </div>
                <% } %>

                <a href="<%= request.getContextPath() %>/assignations" class="back-btn">
                    <span>←</span>
                    <span>Retour aux dates</span>
                </a>
            </div>

            <!-- Navigation now opens a dedicated detail page for the group -->
        </div>
    </div>
</body>
</html>
