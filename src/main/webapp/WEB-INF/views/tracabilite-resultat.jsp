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
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary-color: #4299e1;
            --primary-dark: #3182ce;
            --primary-light: #63b3ed;
            --secondary-color: #e2e8f0;
            --accent-color: #10b981;
            --accent-dark: #059669;
            --warning-color: #f59e0b;
            --warning-dark: #d97706;
            --error-color: #ef4444;
            --text-primary: #1e293b;
            --text-secondary: #64748b;
            --bg-gradient-start: #f8fafc;
            --bg-gradient-end: #e2e8f0;
            --card-bg: rgba(255, 255, 255, 0.95);
            --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
            --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
            --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
            --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
            --border-radius: 12px;
            --border-radius-lg: 16px;
            --border-radius-xl: 24px;
        }
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: linear-gradient(135deg, var(--bg-gradient-start) 0%, var(--bg-gradient-end) 100%);
            min-height: 100vh;
            margin: 0;
            padding: 24px;
            color: var(--text-primary);
            line-height: 1.6;
        }
        .app-layout { display: flex; gap: 28px; max-width: 1400px; margin: 0 auto; padding: 24px; min-height: 100vh; align-items: flex-start; }
        .main-content { flex: 1; min-width: 0; }
        .container { max-width: 1100px; margin: 0 auto; }
        
        @keyframes fadeInDown { from { opacity: 0; transform: translateY(-30px); } to { opacity: 1; transform: translateY(0); } }
        @keyframes fadeInUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }
        @keyframes scaleIn { from { opacity: 0; transform: scale(0.9); } to { opacity: 1; transform: scale(1); } }
        @keyframes pulse { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.02); } }
        
        .page-header {
            text-align: center;
            margin-bottom: 32px;
            animation: fadeInDown 0.6s ease-out;
        }
        .page-title {
            font-size: 36px;
            font-weight: 800;
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 8px;
        }
        .date-header {
            font-size: 18px;
            font-weight: 600;
            color: var(--text-secondary);
        }
        .date-header strong {
            color: var(--primary-dark);
        }
        
        /* Dashboard Section */
        .dashboard {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 40px;
            animation: fadeInUp 0.5s ease-out 0.1s both;
        }
        .dashboard-card {
            background: var(--card-bg);
            backdrop-filter: blur(20px);
            border-radius: var(--border-radius-lg);
            padding: 24px;
            box-shadow: var(--shadow-lg);
            border: 1px solid rgba(255, 255, 255, 0.5);
            text-align: center;
            position: relative;
            overflow: hidden;
            transition: all 0.3s ease;
        }
        .dashboard-card:hover {
            transform: translateY(-4px);
            box-shadow: var(--shadow-xl);
        }
        .dashboard-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
        }
        .dashboard-card.success::before {
            background: linear-gradient(90deg, var(--accent-color), var(--accent-dark));
        }
        .dashboard-card.warning::before {
            background: linear-gradient(90deg, var(--warning-color), var(--warning-dark));
        }
        .dashboard-card.primary::before {
            background: linear-gradient(90deg, var(--primary-color), var(--primary-dark));
        }
        .dashboard-card.info::before {
            background: linear-gradient(90deg, #0ea5e9, #0284c7);
        }
        .dashboard-icon {
            width: 56px;
            height: 56px;
            border-radius: var(--border-radius);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 28px;
            margin: 0 auto 16px;
        }
        .dashboard-card.success .dashboard-icon {
            background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(16, 185, 129, 0.2) 100%);
        }
        .dashboard-card.warning .dashboard-icon {
            background: linear-gradient(135deg, rgba(245, 158, 11, 0.1) 0%, rgba(245, 158, 11, 0.2) 100%);
        }
        .dashboard-card.primary .dashboard-icon {
            background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(99, 102, 241, 0.2) 100%);
        }
        .dashboard-card.info .dashboard-icon {
            background: linear-gradient(135deg, rgba(14, 165, 233, 0.1) 0%, rgba(14, 165, 233, 0.2) 100%);
        }
        .dashboard-value {
            font-size: 42px;
            font-weight: 800;
            line-height: 1;
            margin-bottom: 8px;
        }
        .dashboard-card.success .dashboard-value { color: var(--accent-color); }
        .dashboard-card.warning .dashboard-value { color: var(--warning-color); }
        .dashboard-card.primary .dashboard-value { color: var(--primary-color); }
        .dashboard-card.info .dashboard-value { color: #0ea5e9; }
        .dashboard-label {
            font-size: 13px;
            font-weight: 600;
            color: var(--text-secondary);
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }
        
        .alert-error {
            background: linear-gradient(135deg, rgba(239, 68, 68, 0.1) 0%, rgba(239, 68, 68, 0.05) 100%);
            border: 1px solid rgba(239, 68, 68, 0.2);
            color: #b91c1c;
            padding: 16px 24px;
            border-radius: var(--border-radius);
            margin-bottom: 24px;
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 12px;
        }
        
        .vehicule-card {
            background: var(--card-bg);
            backdrop-filter: blur(20px);
            border-radius: var(--border-radius-xl);
            box-shadow: var(--shadow-lg);
            padding: 32px;
            margin-bottom: 28px;
            border: 1px solid rgba(255, 255, 255, 0.5);
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
            background: rgba(99, 102, 241, 0.12);
            color: var(--primary-dark);
        }
        .tag-carburant {
            background: rgba(16, 185, 129, 0.12);
            color: #047857;
        }
        .tag-vitesse {
            background: rgba(245, 158, 11, 0.12);
            color: #b45309;
        }
        
        .info-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 24px;
        }
        .info-box {
            background: linear-gradient(135deg, rgba(99, 102, 241, 0.05) 0%, rgba(79, 70, 229, 0.1) 100%);
            padding: 20px;
            border-radius: var(--border-radius);
            border: 1px solid rgba(99, 102, 241, 0.15);
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
            font-size: 14px;
            font-weight: 700;
            color: var(--text-secondary);
            margin-bottom: 12px;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }
        
        .parcours-container {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: 10px 12px;
            margin-bottom: 24px;
        }
        .parcours-step {
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            color: white;
            padding: 8px 14px;
            border-radius: 14px;
            font-size: 14px;
            font-weight: 700;
            box-shadow: var(--shadow-sm);
            white-space: nowrap;
        }
        .parcours-arrow {
            color: var(--text-secondary);
            font-weight: 800;
            font-size: 18px;
            margin: 0 4px;
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
        tbody tr {
            transition: all 0.2s ease;
        }
        tbody tr:hover {
            background: rgba(99, 102, 241, 0.04);
        }
        tbody tr:last-child td { border-bottom: none; }
        
        .badge {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 8px;
            font-size: 12px;
            font-weight: 700;
        }
        .badge-id {
            background: rgba(99, 102, 241, 0.12);
            color: var(--primary-dark);
        }
        .badge-persons {
            background: rgba(16, 185, 129, 0.12);
            color: #047857;
        }
        
        .no-data {
            text-align: center;
            padding: 60px 20px;
            background: var(--card-bg);
            border-radius: var(--border-radius-xl);
            box-shadow: var(--shadow-lg);
            animation: fadeInUp 0.6s ease-out;
        }
        .no-data-icon { font-size: 64px; margin-bottom: 16px; opacity: 0.5; }
        .no-data p {
            font-size: 18px;
            color: var(--text-secondary);
            font-weight: 600;
        }
        
        .back-link {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            margin-top: 32px;
            padding: 16px 32px;
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            color: white;
            text-decoration: none;
            border-radius: var(--border-radius);
            font-size: 15px;
            font-weight: 700;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            box-shadow: var(--shadow-md);
        }
        .back-link:hover {
            transform: translateY(-3px);
            box-shadow: var(--shadow-xl);
        }
        
        .footer { text-align: center; margin-top: 16px; }
        
        @media (max-width: 768px) {
            .app-layout { flex-direction: column; padding: 16px; }
            .info-grid { grid-template-columns: 1fr; }
            .page-title { font-size: 28px; }
            .vehicule-header { flex-direction: column; gap: 12px; }
            .vehicule-card { padding: 20px; }
            th, td { padding: 10px 8px; font-size: 13px; }
            .dashboard { grid-template-columns: repeat(2, 1fr); }
            .dashboard-value { font-size: 32px; }
        }
    </style>
</head>
<body>
    <div class="app-layout">
        <%@ include file="includes/sidebar.jsp" %>
        <div class="main-content">
            <div class="container">
                <div class="page-header">
                    <h1 class="page-title">🚐 Traçabilité des Véhicules</h1>
                    <div class="date-header">📅 Traçabilité du <strong><%= request.getAttribute("date") %></strong></div>
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
                    Integer totalReservations = (Integer) request.getAttribute("totalReservations");
                    
                    if (countAssignees == null) countAssignees = 0;
                    if (countNonAssignees == null) countNonAssignees = 0;
                    if (countEnAttente == null) countEnAttente = 0;
                    if (totalReservations == null) totalReservations = 0;
                    if (totalVehicules == null) totalVehicules = 0;
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
                    <% int cardIndex = 0; for (VehiculeTracabilite vt : tracabilites) { cardIndex++; %>
                        <div class="vehicule-card" style="animation-delay: <%= (cardIndex * 0.1) %>s;">
                            <div class="vehicule-header">
                                <div class="vehicule-name">🚐 <%= vt.getVehicule().getMarque() %></div>
                                <div class="vehicule-tags">
                                    <span class="tag tag-capacite">👤 <%= vt.getVehicule().getCapacite() %> places</span>
                                    <span class="tag tag-carburant">⛽ <%= vt.getVehicule().getTypeCarburant() %></span>
                                    <span class="tag tag-vitesse">⚡ <%= vt.getVehicule().getVitesseMoyenne() %> km/h</span>
                                </div>
                            </div>

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

                            <div class="section-title">🏨 Parcours</div>
                            <div class="parcours-container">
                                <div class="parcours-step">Aéroport</div>
                                <% if (vt.getHotels() != null && !vt.getHotels().isEmpty()) { %>
                                    <% for (String hotel : vt.getHotels()) { %>
                                        <div class="parcours-arrow">→</div>
                                        <div class="parcours-step"><%= hotel %></div>
                                    <% } %>
                                <% } %>
                                <div class="parcours-arrow">→</div>
                                <div class="parcours-step">Aéroport</div>
                            </div>

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
                                        <td><span class="badge badge-persons">👤 <%= r.getNombrePersonnes() %></span></td>
                                    </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    <% } %>
                <% } else { %>
                    <div class="no-data">
                        <div class="no-data-icon">📭</div>
                        <p>Aucun véhicule avec des réservations pour cette date.</p>
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