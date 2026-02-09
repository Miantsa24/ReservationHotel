<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="models.Reservation" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Liste des Réservations - Front Office</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
        }
        h1 {
            color: white;
            text-align: center;
            margin-bottom: 30px;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
        }
        .filter-card {
            background: white;
            border-radius: 15px;
            padding: 25px;
            margin-bottom: 25px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
        }
        .filter-form {
            display: flex;
            gap: 15px;
            align-items: center;
            flex-wrap: wrap;
        }
        .filter-form label {
            font-weight: 600;
            color: #333;
        }
        .filter-form input[type="date"] {
            padding: 12px 15px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-size: 14px;
            transition: border-color 0.3s;
        }
        .filter-form input[type="date"]:focus {
            outline: none;
            border-color: #667eea;
        }
        .btn {
            padding: 12px 25px;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
            text-decoration: none;
            display: inline-block;
        }
        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
        }
        .btn-secondary {
            background: #6c757d;
            color: white;
        }
        .btn-secondary:hover {
            background: #5a6268;
        }
        .table-card {
            background: white;
            border-radius: 15px;
            padding: 25px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            overflow-x: auto;
        }
        table {
            width: 100%;
            border-collapse: collapse;
        }
        th, td {
            padding: 15px 12px;
            text-align: left;
            border-bottom: 1px solid #e0e0e0;
        }
        th {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            font-weight: 600;
            text-transform: uppercase;
            font-size: 12px;
            letter-spacing: 0.5px;
        }
        tr:hover {
            background-color: #f8f9fa;
        }
        .no-data {
            text-align: center;
            padding: 40px;
            color: #6c757d;
            font-style: italic;
        }
        .alert {
            padding: 15px 20px;
            border-radius: 8px;
            margin-bottom: 20px;
        }
        .alert-danger {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        .alert-info {
            background: #d1ecf1;
            color: #0c5460;
            border: 1px solid #bee5eb;
        }
        .badge {
            padding: 5px 10px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
        }
        .badge-info {
            background: #e7f3ff;
            color: #0066cc;
        }
        .nav-links {
            text-align: center;
            margin-bottom: 20px;
        }
        .nav-links a {
            color: white;
            text-decoration: none;
            margin: 0 15px;
            font-weight: 500;
            transition: opacity 0.3s;
        }
        .nav-links a:hover {
            opacity: 0.8;
            text-decoration: underline;
        }
        .stats {
            display: flex;
            gap: 10px;
            margin-bottom: 15px;
        }
        .stat-badge {
            background: #e7f3ff;
            color: #0066cc;
            padding: 8px 15px;
            border-radius: 20px;
            font-size: 13px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🏨 Liste des Réservations</h1>
        
        <div class="nav-links">
            <a href="reservations">📋 Toutes les réservations</a>
            <a href="reservation/form">➕ Nouvelle réservation</a>
        </div>

        <!-- Messages d'erreur -->
        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-danger">
                ⚠️ <%= request.getAttribute("error") %>
            </div>
        <% } %>

        <!-- Filtre actif -->
        <% if (request.getAttribute("filterDate") != null) { %>
            <div class="alert alert-info">
                🔍 Filtre actif : Date d'arrivée = <strong><%= request.getAttribute("filterDate") %></strong>
                <a href="reservations" class="btn btn-secondary" style="margin-left: 15px; padding: 5px 15px;">Effacer le filtre</a>
            </div>
        <% } %>

        <!-- Carte de filtre -->
        <div class="filter-card">
            <form action="reservations/filter" method="get" class="filter-form">
                <label for="dateArrivee">🗓️ Filtrer par date d'arrivée :</label>
                <input type="date" id="dateArrivee" name="dateArrivee" 
                       value="<%= request.getAttribute("filterDate") != null ? request.getAttribute("filterDate") : "" %>">
                <button type="submit" class="btn btn-primary">🔍 Filtrer</button>
                <a href="reservations" class="btn btn-secondary">🔄 Réinitialiser</a>
            </form>
        </div>

        <!-- Tableau des réservations -->
        <div class="table-card">
            <%
                List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
                if (reservations != null && !reservations.isEmpty()) {
            %>
            <div class="stats">
                <span class="stat-badge">📊 Total : <%= reservations.size() %> réservation(s)</span>
            </div>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Client</th>
                        <th>Hôtel</th>
                        <th>Date d'arrivée</th>
                        <th>Heure</th>
                        <th>Date de départ</th>
                        <th>Personnes</th>
                        <th>Contact</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Reservation r : reservations) { %>
                    <tr>
                        <td><span class="badge badge-info">#<%= r.getId() %></span></td>
                        <td><strong><%= r.getNomClient() %></strong></td>
                        <td><%= r.getHotelNom() %></td>
                        <td><%= r.getDateArrivee() %></td>
                        <td><%= r.getHeureArrivee() %></td>
                        <td><%= r.getDateDepart() %></td>
                        <td><%= r.getNombrePersonnes() %> 👤</td>
                        <td>
                            📧 <%= r.getEmailClient() %><br>
                            📞 <%= r.getTelephoneClient() %>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
            <% } else { %>
            <div class="no-data">
                <p>📭 Aucune réservation trouvée.</p>
                <p style="margin-top: 10px;">
                    <a href="reservation/form" class="btn btn-primary">➕ Créer une réservation</a>
                </p>
            </div>
            <% } %>
        </div>
    </div>
</body>
</html>
