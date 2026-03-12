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
    <style>
        :root {
            --primary-color: #4299e1;
            --primary-dark: #3182ce;
            --primary-light: #818cf8;
            --secondary-color: #e2e8f0;
            --accent-color: #10b981;
            --accent-light: #34d399;
            --warning-color: #f59e0b;
            --error-color: #ef4444;
            --text-primary: #1e293b;
            --text-secondary: #64748b;
            --bg-gradient-start: #f8fafc;
            --bg-gradient-end: #e2e8f0;
            --card-bg: rgba(255, 255, 255, 0.9);
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
            color: var(--text-primary);
            line-height: 1.6;
        }
        .app-layout { display: flex; gap: 28px; max-width: 1400px; margin: 0 auto; padding: 24px; min-height: 100vh; align-items: flex-start; }
        .main-content { flex: 1; min-width: 0; }
        
        .page-header {
            margin-bottom: 32px;
            animation: slideDown 0.5s ease-out;
        }
        @keyframes slideDown {
            from { opacity: 0; transform: translateY(-20px); }
            to { opacity: 1; transform: translateY(0); }
        }
        @keyframes slideUp {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }
        @keyframes pulse {
            0%, 100% { transform: scale(1); }
            50% { transform: scale(1.05); }
        }
        
        .page-title {
            font-size: 32px;
            font-weight: 800;
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 8px;
        }
        .page-subtitle {
            color: var(--text-secondary);
            font-size: 16px;
            font-weight: 500;
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
        
        .alert-error {
            background: linear-gradient(135deg, rgba(239, 68, 68, 0.1) 0%, rgba(239, 68, 68, 0.05) 100%);
            border: 1px solid rgba(239, 68, 68, 0.2);
            color: #b91c1c;
            padding: 16px 20px;
            border-radius: var(--border-radius);
            margin-bottom: 24px;
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 12px;
        }
        
        .dates-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
            gap: 16px;
            margin-top: 24px;
        }
        
        .date-card {
            display: flex;
            align-items: center;
            gap: 16px;
            padding: 20px 24px;
            background: linear-gradient(135deg, rgba(99, 102, 241, 0.05) 0%, rgba(79, 70, 229, 0.08) 100%);
            border: 1px solid rgba(99, 102, 241, 0.15);
            border-radius: var(--border-radius-lg);
            text-decoration: none;
            color: var(--text-primary);
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
            overflow: hidden;
        }
        .date-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
            transition: left 0.5s;
        }
        .date-card:hover::before {
            left: 100%;
        }
        .date-card:hover {
            transform: translateY(-4px) scale(1.02);
            box-shadow: var(--shadow-xl);
            border-color: var(--primary-color);
            background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(79, 70, 229, 0.15) 100%);
        }
        
        .date-icon {
            width: 56px;
            height: 56px;
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            border-radius: var(--border-radius);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            box-shadow: var(--shadow-md);
        }
        
        .date-info {
            flex: 1;
        }
        .date-value {
            font-size: 18px;
            font-weight: 700;
            color: var(--text-primary);
        }
        .date-label {
            font-size: 13px;
            color: var(--text-secondary);
            font-weight: 500;
        }
        
        .date-arrow {
            width: 32px;
            height: 32px;
            background: rgba(99, 102, 241, 0.1);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--primary-color);
            font-size: 14px;
            transition: all 0.3s ease;
        }
        .date-card:hover .date-arrow {
            background: var(--primary-color);
            color: white;
            transform: translateX(4px);
        }
        
        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: var(--text-secondary);
        }
        .empty-state-icon {
            font-size: 64px;
            margin-bottom: 16px;
            opacity: 0.5;
        }
        .empty-state-text {
            font-size: 18px;
            font-weight: 600;
        }
        
        @media (max-width: 900px) {
            .app-layout { flex-direction: column; padding: 16px; }
            .dates-grid { grid-template-columns: 1fr; }
            .page-title { font-size: 26px; }
        }
    </style>
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
