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
        :root {
            --primary-color: #4299e1;
            --primary-dark: #3182ce;
            --secondary-color: #e2e8f0;
            --accent-color: #48bb78;
            --error-color: #f56565;
            --text-primary: #2d3748;
            --text-secondary: #4a5568;
            --bg-gradient-start: #f7fafc;
            --bg-gradient-end: #edf2f7;
            --shadow-light: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
            --shadow-medium: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
            --shadow-hover: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
            --border-radius: 12px;
            --border-radius-large: 16px;
        }
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
        }
        body {
            background: linear-gradient(135deg, var(--bg-gradient-start) 0%, var(--bg-gradient-end) 100%);
            min-height: 100vh;
            padding: 24px;
            color: var(--text-primary);
            line-height: 1.6;
        }
        .container {
            max-width: 1280px;
            margin: 0 auto;
        }
        h1 {
            color: var(--text-primary);
            text-align: center;
            margin-bottom: 48px;
            font-size: 36px;
            font-weight: 800;
            letter-spacing: -0.025em;
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            animation: fadeInDown 0.6s ease-out;
        }
        @keyframes fadeInDown {
            from {
                opacity: 0;
                transform: translateY(-30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        .filter-card {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(20px);
            border-radius: var(--border-radius-large);
            padding: 40px;
            margin-bottom: 32px;
            box-shadow: var(--shadow-medium);
            border: 1px solid rgba(255, 255, 255, 0.2);
            animation: slideInLeft 0.6s ease-out 0.2s both;
        }
        @keyframes slideInLeft {
            from {
                opacity: 0;
                transform: translateX(-30px);
            }
            to {
                opacity: 1;
                transform: translateX(0);
            }
        }
        .filter-form {
            display: flex;
            gap: 20px;
            align-items: center;
            flex-wrap: wrap;
        }
        .filter-form label {
            font-weight: 600;
            color: var(--text-secondary);
            font-size: 14px;
            letter-spacing: 0.025em;
            text-transform: uppercase;
        }
        .filter-form input[type="date"] {
            padding: 14px 18px;
            border: 2px solid var(--secondary-color);
            border-radius: var(--border-radius);
            font-size: 14px;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            background: #ffffff;
            color: var(--text-primary);
        }
        .filter-form input[type="date"]:focus {
            outline: none;
            border-color: var(--primary-color);
            box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.15), var(--shadow-light);
            transform: translateY(-1px);
        }
        .btn {
            padding: 14px 28px;
            border: none;
            border-radius: var(--border-radius);
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            text-decoration: none;
            display: inline-block;
            position: relative;
            overflow: hidden;
        }
        .btn::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
            transition: left 0.5s;
        }
        .btn:hover::before {
            left: 100%;
        }
        .btn-primary {
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            color: white;
        }
        .btn-primary:hover {
            transform: translateY(-3px);
            box-shadow: var(--shadow-hover);
        }
        .btn-secondary {
            background: var(--secondary-color);
            color: var(--text-secondary);
        }
        .btn-secondary:hover {
            background: #cbd5e0;
            transform: translateY(-1px);
        }
        .table-card {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(20px);
            border-radius: var(--border-radius-large);
            padding: 40px;
            box-shadow: var(--shadow-medium);
            border: 1px solid rgba(255, 255, 255, 0.2);
            overflow-x: auto;
            animation: slideInRight 0.6s ease-out 0.4s both;
        }
        @keyframes slideInRight {
            from {
                opacity: 0;
                transform: translateX(30px);
            }
            to {
                opacity: 1;
                transform: translateX(0);
            }
        }
        table {
            width: 100%;
            border-collapse: collapse;
            border-radius: var(--border-radius);
            overflow: hidden;
            box-shadow: var(--shadow-light);
        }
        th, td {
            padding: 18px 16px;
            text-align: left;
            border-bottom: 1px solid var(--secondary-color);
        }
        th {
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            color: white;
            font-weight: 700;
            text-transform: uppercase;
            font-size: 12px;
            letter-spacing: 0.05em;
        }
        tr:hover {
            background-color: rgba(66, 153, 225, 0.05);
            transform: scale(1.01);
            transition: all 0.2s ease;
        }
        .no-data {
            text-align: center;
            padding: 80px 20px;
            color: var(--text-secondary);
            font-style: italic;
            font-size: 18px;
        }
        .alert {
            padding: 20px 24px;
            border-radius: var(--border-radius);
            margin-bottom: 28px;
            font-weight: 500;
            display: flex;
            align-items: center;
            gap: 12px;
        }
        .alert-danger {
            background: linear-gradient(135deg, #fed7d7 0%, #feb2b2 100%);
            color: #742a2a;
            border: 1px solid var(--error-color);
        }
        .alert-info {
            background: linear-gradient(135deg, #bee3f8 0%, #90cdf4 100%);
            color: #2a4365;
            border: 1px solid #4299e1;
        }
        .badge {
            padding: 8px 14px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
        }
        .badge-info {
            background: linear-gradient(135deg, #bee3f8 0%, #90cdf4 100%);
            color: #2a4365;
        }
        .nav-links {
            text-align: center;
            margin-bottom: 40px;
        }
        .nav-links a {
            color: var(--text-secondary);
            text-decoration: none;
            margin: 0 24px;
            font-weight: 500;
            transition: all 0.3s ease;
            padding: 10px 20px;
            border-radius: var(--border-radius);
            position: relative;
        }
        .nav-links a::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 50%;
            width: 0;
            height: 2px;
            background: var(--primary-color);
            transition: all 0.3s ease;
            transform: translateX(-50%);
        }
        .nav-links a:hover::after {
            width: 100%;
        }
        .nav-links a:hover {
            color: var(--primary-color);
            background: rgba(66, 153, 225, 0.1);
        }
        .stats {
            display: flex;
            gap: 16px;
            margin-bottom: 24px;
        }
        .stat-badge {
            background: linear-gradient(135deg, #bee3f8 0%, #90cdf4 100%);
            color: #2a4365;
            padding: 10px 20px;
            border-radius: 20px;
            font-size: 14px;
            font-weight: 500;
        }
        @media (max-width: 768px) {
            .container {
                padding: 16px;
            }
            .filter-card, .table-card {
                padding: 24px;
            }
            .filter-form {
                flex-direction: column;
                align-items: stretch;
            }
            .nav-links a {
                margin: 8px;
            }
            h1 {
                font-size: 28px;
            }
            table {
                font-size: 14px;
            }
            th, td {
                padding: 12px 8px;
            }
        }
    </style>
</head>
<body>
    <div class="app-layout">
        <%@ include file="includes/sidebar.jsp" %>
        <div class="main-content">
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
                <a href="<%= request.getContextPath() %>/reservations" class="btn btn-secondary" style="margin-left: 15px; padding: 5px 15px;">Effacer le filtre</a>
            </div>
        <% } %>

        <!-- Carte de filtre -->
        <div class="filter-card">
            <form action="reservations/filter" method="get" class="filter-form">
                <label for="dateArrivee">🗓️ Filtrer par date d'arrivée :</label>
                <input type="date" id="dateArrivee" name="dateArrivee" 
                       value="<%= request.getAttribute("filterDate") != null ? request.getAttribute("filterDate") : "" %>">
                <button type="submit" class="btn btn-primary">🔍 Filtrer</button>
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
                        <th>Réf. Client</th>
                        <th>Hôtel</th>
                        <th>Date d'arrivée</th>
                        <th>Heure</th>
                        <th>Personnes</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Reservation r : reservations) { %>
                    <tr>
                        <td><span class="badge badge-info">#<%= r.getId() %></span></td>
                        <td><strong><%= r.getRefClient() %></strong></td>
                        <td><%= r.getHotelNom() %></td>
                        <td><%= r.getDateArrivee() %></td>
                        <td><%= r.getHeureArrivee() %></td>
                        <td><%= r.getNombrePersonnes() %> 👤</td>
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
        </div>
    </div>
</body>
</html>
