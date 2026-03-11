<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Accueil - Hotel App</title>
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
            color: var(--text-primary);
            line-height: 1.6;
        }

        /* ===== Welcome hero ===== */
        .welcome-hero {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(20px);
            border-radius: var(--border-radius-large);
            box-shadow: var(--shadow-medium);
            border: 1px solid rgba(255, 255, 255, 0.2);
            padding: 56px 48px;
            text-align: center;
            margin-bottom: 32px;
            animation: fadeInUp 0.6s ease-out;
        }
        @keyframes fadeInUp {
            from { opacity: 0; transform: translateY(30px); }
            to   { opacity: 1; transform: translateY(0); }
        }
        .welcome-icon {
            font-size: 64px;
            margin-bottom: 16px;
        }
        .welcome-hero h1 {
            font-size: 36px;
            font-weight: 800;
            letter-spacing: -0.025em;
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 12px;
        }
        .welcome-hero p {
            font-size: 18px;
            color: var(--text-secondary);
            max-width: 600px;
            margin: 0 auto;
        }

        /* ===== Quick-access cards ===== */
        .cards-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
            gap: 24px;
            animation: fadeInUp 0.6s ease-out 0.2s both;
        }
        .card {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(20px);
            border-radius: var(--border-radius-large);
            box-shadow: var(--shadow-medium);
            border: 1px solid rgba(255, 255, 255, 0.2);
            padding: 36px 28px;
            text-align: center;
            text-decoration: none;
            color: var(--text-primary);
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
            overflow: hidden;
        }
        .card::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(66, 153, 225, 0.06), transparent);
            transition: left 0.5s;
        }
        .card:hover::before { left: 100%; }
        .card:hover {
            transform: translateY(-6px);
            box-shadow: var(--shadow-hover);
        }
        .card-icon {
            font-size: 48px;
            margin-bottom: 16px;
            display: block;
        }
        .card h2 {
            font-size: 20px;
            font-weight: 700;
            margin-bottom: 8px;
        }
        .card p {
            font-size: 14px;
            color: var(--text-secondary);
        }
        .card-arrow {
            display: inline-block;
            margin-top: 16px;
            padding: 8px 20px;
            border-radius: var(--border-radius);
            font-size: 13px;
            font-weight: 700;
            color: white;
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            transition: box-shadow 0.3s ease;
        }
        .card:hover .card-arrow {
            box-shadow: 0 4px 12px rgba(66, 153, 225, 0.4);
        }

        @media (max-width: 768px) {
            .welcome-hero { padding: 36px 24px; }
            .welcome-hero h1 { font-size: 28px; }
            .cards-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
    <div class="app-layout">
        <%@ include file="includes/sidebar.jsp" %>
        <div class="main-content">

            <div class="welcome-hero">
                <div class="welcome-icon">🏨</div>
                <h1>Bienvenue sur Hotel App</h1>
                <p>Gérez vos véhicules, réservations et suivez la traçabilité de votre flotte depuis un seul endroit.</p>
            </div>

            <div class="cards-grid">
                <a href="<%= request.getContextPath() %>/vehicules" class="card">
                    <span class="card-icon">🚗</span>
                    <h2>Véhicules</h2>
                    <p>Consultez et gérez la flotte de véhicules disponibles.</p>
                    <span class="card-arrow">Voir la liste →</span>
                </a>

                <a href="<%= request.getContextPath() %>/reservations" class="card">
                    <span class="card-icon">📋</span>
                    <h2>Réservations</h2>
                    <p>Visualisez toutes les réservations et créez-en de nouvelles.</p>
                    <span class="card-arrow">Voir la liste →</span>
                </a>

                <a href="<%= request.getContextPath() %>/tracabilite" class="card">
                    <span class="card-icon">🧭</span>
                    <h2>Traçabilité</h2>
                    <p>Suivez les trajets et horaires de retour des véhicules.</p>
                    <span class="card-arrow">Consulter →</span>
                </a>
            </div>

        </div>
    </div>
</body>
</html>
