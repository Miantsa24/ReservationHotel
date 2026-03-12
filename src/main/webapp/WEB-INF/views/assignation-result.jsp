<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Résultat d'assignation</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary-color: #4299e1;
            --primary-dark: #3182ce;
            --secondary-color: #e2e8f0;
            --accent-color: #10b981;
            --accent-dark: #059669;
            --warning-color: #f59e0b;
            --error-color: #ef4444;
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
        @keyframes scaleIn { from { opacity: 0; transform: scale(0.9); } to { opacity: 1; transform: scale(1); } }
        @keyframes checkmark { 0% { stroke-dashoffset: 100; } 100% { stroke-dashoffset: 0; } }
        
        .page-header { margin-bottom: 32px; animation: slideDown 0.5s ease-out; text-align: center; }
        .page-title {
            font-size: 36px;
            font-weight: 800;
            background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-dark) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 8px;
        }
        .page-subtitle { color: var(--text-secondary); font-size: 16px; font-weight: 500; }
        
        .card {
            background: var(--card-bg);
            backdrop-filter: blur(20px);
            border-radius: var(--border-radius-xl);
            padding: 40px;
            box-shadow: var(--shadow-lg);
            border: 1px solid rgba(255, 255, 255, 0.5);
            animation: slideUp 0.6s ease-out;
            text-align: center;
        }
        
        .success-icon {
            width: 80px;
            height: 80px;
            background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-dark) 100%);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 24px;
            font-size: 40px;
            box-shadow: 0 8px 32px rgba(16, 185, 129, 0.3);
            animation: scaleIn 0.4s ease-out 0.2s both;
        }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: 20px;
            margin: 32px 0;
        }
        
        .stat-card {
            background: linear-gradient(135deg, rgba(99, 102, 241, 0.05) 0%, rgba(99, 102, 241, 0.1) 100%);
            border: 1px solid rgba(99, 102, 241, 0.15);
            border-radius: var(--border-radius-lg);
            padding: 24px;
            animation: scaleIn 0.4s ease-out both;
        }
        .stat-card:nth-child(1) { animation-delay: 0.1s; }
        .stat-card:nth-child(2) { animation-delay: 0.2s; }
        
        .stat-card.success {
            background: linear-gradient(135deg, rgba(16, 185, 129, 0.05) 0%, rgba(16, 185, 129, 0.12) 100%);
            border-color: rgba(16, 185, 129, 0.2);
        }
        .stat-card.warning {
            background: linear-gradient(135deg, rgba(245, 158, 11, 0.05) 0%, rgba(245, 158, 11, 0.12) 100%);
            border-color: rgba(245, 158, 11, 0.2);
        }
        
        .stat-value {
            font-size: 48px;
            font-weight: 800;
            line-height: 1;
            margin-bottom: 8px;
        }
        .stat-card.success .stat-value { color: var(--accent-color); }
        .stat-card.warning .stat-value { color: var(--warning-color); }
        
        .stat-label {
            font-size: 14px;
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
            text-align: left;
            display: flex;
            align-items: center;
            gap: 12px;
        }
        
        .no-result {
            color: var(--text-secondary);
            font-size: 16px;
            padding: 20px;
        }
        
        .action-bar {
            margin-top: 32px;
            display: flex;
            gap: 16px;
            justify-content: center;
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
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            color: white;
        }
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: var(--shadow-lg);
        }
        .btn-secondary {
            background: rgba(66, 153, 225, 0.1);
            color: var(--primary-dark);
        }
        .btn-secondary:hover {
            background: rgba(66, 153, 225, 0.2);
        }
        
        @media (max-width: 900px) {
            .app-layout { flex-direction: column; padding: 16px; }
            .page-title { font-size: 28px; }
            .card { padding: 24px; }
            .stats-grid { grid-template-columns: 1fr; }
            .stat-value { font-size: 36px; }
            .action-bar { flex-direction: column; }
            .btn { width: 100%; justify-content: center; }
        }
    </style>
</head>
<body>
    <div class="app-layout">
        <%@ include file="includes/sidebar.jsp" %>
        <div class="main-content">
            <div class="page-header">
                <h1 class="page-title">Assignation terminée</h1>
                <p class="page-subtitle">Résultat de l'algorithme Largest-First</p>
            </div>

            <div class="card">
                <% if (request.getAttribute("error") != null) { %>
                    <div class="alert-error">
                        <span>⚠️</span>
                        <span><%= request.getAttribute("error") %></span>
                    </div>
                <% } %>
                
                <%
                    Map<String,Object> res = (Map<String,Object>) request.getAttribute("result");
                    if (res != null) {
                        int assigned = res.get("assigned") != null ? (Integer) res.get("assigned") : 0;
                        int notAssigned = res.get("notAssigned") != null ? (Integer) res.get("notAssigned") : 0;
                %>
                    <div class="success-icon">✓</div>
                    
                    <div class="stats-grid">
                        <div class="stat-card success">
                            <div class="stat-value"><%= assigned %></div>
                            <div class="stat-label">Réservations assignées</div>
                        </div>
                        <div class="stat-card warning">
                            <div class="stat-value"><%= notAssigned %></div>
                            <div class="stat-label">Non assignées</div>
                        </div>
                    </div>
                    
                    <% if (res.get("error") != null) { %>
                        <div class="alert-error">
                            <span>⚠️</span>
                            <span><%= res.get("error") %></span>
                        </div>
                    <% } %>
                <% } else { %>
                    <p class="no-result">Aucun résultat disponible</p>
                <% } %>
                
                <div class="action-bar">
                    <a href="<%= request.getContextPath() %>/assignations" class="btn btn-primary">
                        <span>📅</span>
                        <span>Retour aux dates</span>
                    </a>
                    <a href="<%= request.getContextPath() %>/tracabilite" class="btn btn-secondary">
                        <span>🚐</span>
                        <span>Voir la traçabilité</span>
                    </a>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
