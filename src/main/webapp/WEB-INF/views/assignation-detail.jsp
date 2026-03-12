<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="models.Reservation" %>
<%@ page import="java.sql.Date" %>
<%@ page import="java.sql.Time" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Détail du créneau</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary-color: #4299e1;
            --primary-dark: #3182ce;
            --secondary-color: #e2e8f0;
            --accent-color: #10b981;
            --accent-dark: #059669;
            --warning-color: #f59e0b;
            --text-primary: #1e293b;
            --text-secondary: #64748b;
            --bg-gradient-start: #f8fafc;
            --bg-gradient-end: #e2e8f0;
            --card-bg: rgba(255, 255, 255, 0.9);
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
            color: var(--text-primary);
            line-height: 1.6;
        }
        .app-layout { display: flex; gap: 28px; max-width: 1400px; margin: 0 auto; padding: 24px; min-height: 100vh; align-items: flex-start; }
        .main-content { flex: 1; min-width: 0; }
        
        @keyframes slideDown { from { opacity: 0; transform: translateY(-20px); } to { opacity: 1; transform: translateY(0); } }
        @keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
        @keyframes pulse { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.02); } }
        
        .breadcrumb {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 24px;
            font-size: 14px;
            animation: slideDown 0.4s ease-out;
        }
        .breadcrumb a { color: var(--primary-color); text-decoration: none; font-weight: 600; transition: color 0.2s; }
        .breadcrumb a:hover { color: var(--primary-dark); }
        .breadcrumb span { color: var(--text-secondary); }
        
        .page-header { margin-bottom: 32px; animation: slideDown 0.5s ease-out; }
        .page-title {
            font-size: 32px;
            font-weight: 800;
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 8px;
        }
        .page-subtitle { color: var(--text-secondary); font-size: 16px; font-weight: 500; }
        
        .info-badges {
            display: flex;
            gap: 12px;
            margin-bottom: 24px;
            flex-wrap: wrap;
        }
        .info-badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 10px 18px;
            border-radius: var(--border-radius);
            font-weight: 600;
            font-size: 14px;
            box-shadow: var(--shadow-sm);
        }
        .info-badge.date {
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            color: white;
        }
        .info-badge.time {
            background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-dark) 100%);
            color: white;
        }
        
        .card {
            background: var(--card-bg);
            backdrop-filter: blur(20px);
            border-radius: var(--border-radius-xl);
            padding: 32px;
            box-shadow: var(--shadow-lg);
            border: 1px solid rgba(255, 255, 255, 0.5);
            animation: slideUp 0.6s ease-out;
        }
        .card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 24px;
            padding-bottom: 16px;
            border-bottom: 2px solid var(--secondary-color);
        }
        .card-title {
            font-size: 20px;
            font-weight: 700;
            color: var(--text-primary);
        }
        .card-count {
            background: linear-gradient(135deg, var(--warning-color) 0%, #d97706 100%);
            color: white;
            padding: 6px 14px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: 700;
        }
        
        .table-container {
            overflow-x: auto;
            margin-bottom: 24px;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            border-radius: var(--border-radius);
            overflow: hidden;
        }
        thead { background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%); }
        th {
            color: white;
            padding: 16px 20px;
            text-align: left;
            font-size: 13px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }
        td {
            padding: 16px 20px;
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
        
        .badge-id {
            display: inline-block;
            background: rgba(99, 102, 241, 0.12);
            color: var(--primary-dark);
            padding: 4px 12px;
            border-radius: 8px;
            font-size: 13px;
            font-weight: 700;
        }
        .badge-persons {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            background: rgba(16, 185, 129, 0.12);
            color: #047857;
            padding: 4px 12px;
            border-radius: 8px;
            font-size: 13px;
            font-weight: 700;
        }
        
        .action-bar {
            display: flex;
            gap: 16px;
            align-items: center;
            flex-wrap: wrap;
        }
        
        .btn {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 14px 28px;
            border-radius: var(--border-radius);
            font-weight: 700;
            font-size: 15px;
            text-decoration: none;
            border: none;
            cursor: pointer;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            box-shadow: var(--shadow-md);
        }
        .btn-primary {
            background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-dark) 100%);
            color: white;
        }
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: var(--shadow-lg);
        }
        .btn-primary:active {
            transform: translateY(0);
        }
        .btn-secondary {
            background: rgba(66, 153, 225, 0.1);
            color: var(--primary-dark);
        }
        .btn-secondary:hover {
            background: rgba(66, 153, 225, 0.2);
        }
        
        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: var(--text-secondary);
        }
        .empty-state-icon { font-size: 64px; margin-bottom: 16px; opacity: 0.5; }
        .empty-state-text { font-size: 18px; font-weight: 600; }
        
        @media (max-width: 900px) {
            .app-layout { flex-direction: column; padding: 16px; }
            .page-title { font-size: 26px; }
            th, td { padding: 12px; font-size: 13px; }
            .card { padding: 20px; }
            .action-bar { flex-direction: column; }
            .btn { width: 100%; justify-content: center; }
        }
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
                <% if (request.getAttribute("reservations") != null) { 
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
