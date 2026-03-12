<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.sql.Time" %>
<%@ page import="java.sql.Date" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Assignations - Heures</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary-color: #4299e1;
            --primary-dark: #3182ce;
            --primary-light: #818cf8;
            --secondary-color: #e2e8f0;
            --accent-color: #10b981;
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
        
        .breadcrumb {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 24px;
            font-size: 14px;
            animation: slideDown 0.4s ease-out;
        }
        .breadcrumb a {
            color: var(--primary-color);
            text-decoration: none;
            font-weight: 600;
            transition: color 0.2s;
        }
        .breadcrumb a:hover { color: var(--primary-dark); }
        .breadcrumb span { color: var(--text-secondary); }
        
        .date-badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            color: white;
            padding: 10px 20px;
            border-radius: var(--border-radius);
            font-weight: 700;
            font-size: 15px;
            margin-bottom: 24px;
            box-shadow: var(--shadow-md);
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
        
        .hours-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
            gap: 16px;
        }
        
        .hour-card {
            display: flex;
            align-items: center;
            gap: 16px;
            padding: 20px;
            background: linear-gradient(135deg, rgba(16, 185, 129, 0.05) 0%, rgba(16, 185, 129, 0.1) 100%);
            border: 1px solid rgba(16, 185, 129, 0.2);
            border-radius: var(--border-radius-lg);
            text-decoration: none;
            color: var(--text-primary);
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
            overflow: hidden;
        }
        .hour-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
            transition: left 0.5s;
        }
        .hour-card:hover::before { left: 100%; }
        .hour-card:hover {
            transform: translateY(-4px) scale(1.02);
            box-shadow: var(--shadow-xl);
            border-color: var(--accent-color);
            background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(16, 185, 129, 0.18) 100%);
        }
        
        .hour-icon {
            width: 48px;
            height: 48px;
            background: linear-gradient(135deg, var(--accent-color) 0%, #059669 100%);
            border-radius: var(--border-radius);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 20px;
            box-shadow: var(--shadow-md);
        }
        
        .hour-value {
            font-size: 20px;
            font-weight: 700;
            color: var(--text-primary);
        }
        
        .hour-arrow {
            margin-left: auto;
            width: 28px;
            height: 28px;
            background: rgba(16, 185, 129, 0.1);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--accent-color);
            font-size: 12px;
            transition: all 0.3s ease;
        }
        .hour-card:hover .hour-arrow {
            background: var(--accent-color);
            color: white;
            transform: translateX(4px);
        }
        
        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: var(--text-secondary);
        }
        .empty-state-icon { font-size: 64px; margin-bottom: 16px; opacity: 0.5; }
        .empty-state-text { font-size: 18px; font-weight: 600; }
        
        .back-btn {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            margin-top: 24px;
            padding: 12px 24px;
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            color: white;
            text-decoration: none;
            border-radius: var(--border-radius);
            font-weight: 600;
            transition: all 0.3s ease;
            box-shadow: var(--shadow-md);
        }
        .back-btn:hover {
            transform: translateY(-2px);
            box-shadow: var(--shadow-lg);
        }
        
        @media (max-width: 900px) {
            .app-layout { flex-direction: column; padding: 16px; }
            .hours-grid { grid-template-columns: 1fr; }
            .page-title { font-size: 26px; }
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
                <span><%= request.getAttribute("date") %></span>
            </div>
            
            <div class="page-header">
                <h1 class="page-title">Créneaux horaires</h1>
                <p class="page-subtitle">Sélectionnez un créneau pour voir les réservations à assigner</p>
            </div>
            
            <div class="date-badge">
                <span>📅</span>
                <span><%= request.getAttribute("date") %></span>
            </div>

            <div class="card">
                <% if (request.getAttribute("hours") != null) { %>
                    <%
                        List<Time> hours = (List<Time>) request.getAttribute("hours");
                        if (hours != null && !hours.isEmpty()) {
                    %>
                        <div class="hours-grid">
                            <% for (Time t : hours) { %>
                                <a href="<%= request.getContextPath() %>/assignations/detail?date=<%= request.getAttribute("date") %>&heure=<%= t.toString() %>" class="hour-card">
                                    <div class="hour-icon">⏱️</div>
                                    <div class="hour-value"><%= t.toString() %></div>
                                    <div class="hour-arrow">→</div>
                                </a>
                            <% } %>
                        </div>
                    <% } else { %>
                        <div class="empty-state">
                            <div class="empty-state-icon">🕐</div>
                            <p class="empty-state-text">Aucun créneau disponible pour cette date</p>
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
        </div>
    </div>
</body>
</html>
