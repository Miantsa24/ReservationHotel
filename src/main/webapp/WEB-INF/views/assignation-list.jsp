<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.sql.Date" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Assignations - Dates en attente</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <%@ include file="includes/assignation-styles.jsp" %>
</head>
<body>
    <div class="app-layout">
        <%@ include file="includes/sidebar.jsp" %>
        <div class="main-content">
            <div class="page-header">
                <h1 class="page-title">Assignations</h1>
                <p class="page-subtitle">Sélectionnez une date pour gérer les assignations de véhicules</p>
            </div>

            <div class="card">
                <% if (request.getAttribute("error") != null) { %>
                    <div class="alert-error">
                        <span>⚠️</span>
                        <span><%= request.getAttribute("error") %></span>
                    </div>
                <% } %>

                <%
                    List<Date> dates = (List<Date>) request.getAttribute("dates");
                    if (dates != null && !dates.isEmpty()) {
                %>
                    <div class="dates-grid">
                        <% for (Date d : dates) { %>
                            <a href="<%= request.getContextPath() %>/assignations/hours?date=<%= d.toString() %>" class="date-card">
                                <div class="date-icon">📅</div>
                                <div class="date-info">
                                    <div class="date-value"><%= d.toString() %></div>
                                    <div class="date-label">Réservations en attente</div>
                                </div>
                                <div class="date-arrow">→</div>
                            </a>
                        <% } %>
                    </div>
                <% } else { %>
                    <div class="empty-state">
                        <div class="empty-state-icon">📭</div>
                        <p class="empty-state-text">Aucune date avec des réservations en attente</p>
                    </div>
                <% } %>
            </div>
        </div>
    </div>
</body>
</html>
