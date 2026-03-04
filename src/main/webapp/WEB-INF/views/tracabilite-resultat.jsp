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
            margin: 0;
            padding: 24px;
            color: var(--text-primary);
            line-height: 1.6;
        }
        .container {
            max-width: 1100px;
            margin: 0 auto;
        }
        h1 {
            color: var(--text-primary);
            text-align: center;
            margin-bottom: 12px;
            font-size: 32px;
            font-weight: 800;
            letter-spacing: -0.025em;
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            animation: fadeInDown 0.6s ease-out;
        }
        @keyframes fadeInDown {
            from { opacity: 0; transform: translateY(-30px); }
            to { opacity: 1; transform: translateY(0); }
        }
        @keyframes fadeInUp {
            from { opacity: 0; transform: translateY(30px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .date-header {
            text-align: center;
            font-size: 20px;
            font-weight: 600;
            color: var(--text-secondary);
            margin-bottom: 32px;
            animation: fadeInDown 0.6s ease-out 0.1s both;
        }
        .stats-bar {
            display: flex;
            justify-content: center;
            gap: 16px;
            margin-bottom: 32px;
            animation: fadeInUp 0.5s ease-out 0.2s both;
        }
        .stat-badge {
            background: rgba(255, 255, 255, 0.95);
            padding: 10px 20px;
            border-radius: var(--border-radius);
            font-weight: 600;
            font-size: 14px;
            box-shadow: var(--shadow-light);
            color: var(--text-secondary);
        }
        .vehicule-card {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(20px);
            border-radius: var(--border-radius-large);
            box-shadow: var(--shadow-medium);
            padding: 32px;
            margin-bottom: 28px;
            border: 1px solid rgba(255, 255, 255, 0.2);
            animation: fadeInUp 0.6s ease-out both;
        }
        .vehicule-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 24px;
            padding-bottom: 16px;
            border-bottom: 2px solid var(--secondary-color);
        }
        .vehicule-name {
            font-size: 22px;
            font-weight: 800;
            color: var(--text-primary);
        }
        .vehicule-tags {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
        }
        .tag {
            padding: 6px 14px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }
        .tag-capacite {
            background: rgba(66, 153, 225, 0.12);
            color: var(--primary-dark);
        }
        .tag-carburant {
            background: rgba(72, 187, 120, 0.12);
            color: #276749;
        }
        .tag-vitesse {
            background: rgba(237, 137, 54, 0.12);
            color: #c05621;
        }
        .info-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 24px;
        }
        .info-box {
            background: linear-gradient(135deg, rgba(66, 153, 225, 0.05) 0%, rgba(49, 130, 206, 0.1) 100%);
            padding: 20px;
            border-radius: var(--border-radius);
            border: 1px solid rgba(66, 153, 225, 0.15);
        }
        .info-box-label {
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            color: var(--text-secondary);
            margin-bottom: 6px;
        }
        .info-box-value {
            font-size: 24px;
            font-weight: 800;
            color: var(--primary-dark);
        }
        .section-title {
            font-size: 16px;
            font-weight: 700;
            color: var(--text-secondary);
            margin-bottom: 12px;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }
        .hotels-list {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            margin-bottom: 24px;
        }
        .hotel-badge {
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            color: white;
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: 600;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            border-radius: var(--border-radius);
            overflow: hidden;
        }
        thead {
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
        }
        th {
            color: white;
            padding: 14px 16px;
            text-align: left;
            font-size: 13px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }
        td {
            padding: 14px 16px;
            border-bottom: 1px solid var(--secondary-color);
            font-size: 14px;
        }
        tbody tr:hover {
            background: rgba(66, 153, 225, 0.04);
        }
        .badge {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 8px;
            font-size: 12px;
            font-weight: 700;
        }
        .badge-id {
            background: rgba(66, 153, 225, 0.12);
            color: var(--primary-dark);
        }
        .no-data {
            text-align: center;
            padding: 60px 20px;
            background: rgba(255, 255, 255, 0.95);
            border-radius: var(--border-radius-large);
            box-shadow: var(--shadow-medium);
            animation: fadeInUp 0.6s ease-out;
        }
        .no-data p {
            font-size: 18px;
            color: var(--text-secondary);
        }
        .alert-error {
            background: rgba(245, 101, 101, 0.1);
            border: 1px solid rgba(245, 101, 101, 0.3);
            color: #c53030;
            padding: 16px 24px;
            border-radius: var(--border-radius);
            margin-bottom: 24px;
            font-weight: 600;
        }
        .back-link {
            display: inline-block;
            margin-top: 32px;
            padding: 16px 32px;
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            color: white;
            text-decoration: none;
            border-radius: var(--border-radius);
            font-size: 15px;
            font-weight: 700;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
            overflow: hidden;
        }
        .back-link::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
            transition: left 0.5s;
        }
        .back-link:hover::before { left: 100%; }
        .back-link:hover {
            transform: translateY(-3px);
            box-shadow: var(--shadow-hover);
        }
        .footer {
            text-align: center;
            margin-top: 16px;
        }
        @media (max-width: 768px) {
            .info-grid { grid-template-columns: 1fr; }
            h1 { font-size: 26px; }
            .vehicule-header { flex-direction: column; gap: 12px; }
            .vehicule-card { padding: 20px; }
            th, td { padding: 10px 8px; font-size: 13px; }
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🚐 Traçabilité des Véhicules</h1>
        <div class="date-header">📅 Traçabilité du <strong><%= request.getAttribute("date") %></strong></div>

        <% if (request.getAttribute("error") != null) { %>
            <div class="alert-error">⚠️ <%= request.getAttribute("error") %></div>
        <% } %>

        <%
            List<VehiculeTracabilite> tracabilites = (List<VehiculeTracabilite>) request.getAttribute("tracabilites");
            Integer totalVehicules = (Integer) request.getAttribute("totalVehicules");
        %>

        <% if (tracabilites != null && !tracabilites.isEmpty()) { %>

            <div class="stats-bar">
                <span class="stat-badge">🚐 <%= totalVehicules %> véhicule(s) actif(s)</span>
            </div>

            <% int cardIndex = 0; for (VehiculeTracabilite vt : tracabilites) { cardIndex++; %>
                <div class="vehicule-card" style="animation-delay: <%= (cardIndex * 0.1) %>s;">

                    <!-- En-tête véhicule -->
                    <div class="vehicule-header">
                        <div class="vehicule-name">🚐 <%= vt.getVehicule().getMarque() %></div>
                        <div class="vehicule-tags">
                            <span class="tag tag-capacite">👤 <%= vt.getVehicule().getCapacite() %> places</span>
                            <span class="tag tag-carburant">⛽ <%= vt.getVehicule().getTypeCarburant() %></span>
                            <span class="tag tag-vitesse">⚡ <%= vt.getVehicule().getVitesseMoyenne() %> km/h</span>
                        </div>
                    </div>

                    <!-- Heures départ / retour -->
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
                    </div>

                    <!-- Hôtels parcourus -->
                    <div class="section-title">🏨 Hôtels parcourus</div>
                    <div class="hotels-list">
                        <% for (String hotel : vt.getHotels()) { %>
                            <span class="hotel-badge"><%= hotel %></span>
                        <% } %>
                    </div>

                    <!-- Tableau des réservations -->
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
                            <% for (Reservation r : vt.getReservations()) { %>
                            <tr>
                                <td><span class="badge badge-id">#<%= r.getId() %></span></td>
                                <td><strong><%= r.getRefClient() %></strong></td>
                                <td><%= r.getHotelNom() %></td>
                                <td><%= r.getHeureArrivee() %></td>
                                <td><%= r.getNombrePersonnes() %> 👤</td>
                            </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } %>

        <% } else { %>
            <div class="no-data">
                <p>📭 Aucun véhicule avec des réservations pour cette date.</p>
            </div>
        <% } %>

        <div class="footer">
            <a href="<%= request.getContextPath() %>/tracabilite" class="back-link">⬅ Retour</a>
        </div>
    </div>
</body>
</html>
