<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Accès Refusé - Authentification Requise</title>
    <style>
        :root {
            --error-color: #e53e3e;
            --error-light: #fed7d7;
            --error-dark: #c53030;
            --text-primary: #2d3748;
            --text-secondary: #4a5568;
            --bg-gradient-start: #f7fafc;
            --bg-gradient-end: #edf2f7;
            --shadow-medium: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
            --border-radius: 12px;
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
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 24px;
            color: var(--text-primary);
        }
        .error-container {
            max-width: 500px;
            width: 100%;
            background: white;
            border-radius: var(--border-radius);
            box-shadow: var(--shadow-medium);
            overflow: hidden;
            animation: slideIn 0.5s ease-out;
        }
        @keyframes slideIn {
            from {
                opacity: 0;
                transform: translateY(-20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        .error-header {
            background: linear-gradient(135deg, var(--error-color) 0%, var(--error-dark) 100%);
            color: white;
            padding: 32px;
            text-align: center;
        }
        .error-icon {
            font-size: 64px;
            margin-bottom: 16px;
        }
        .error-header h1 {
            font-size: 24px;
            font-weight: 700;
            margin-bottom: 8px;
        }
        .error-header p {
            font-size: 14px;
            opacity: 0.9;
        }
        .error-body {
            padding: 32px;
        }
        .error-detail {
            background: var(--error-light);
            border-left: 4px solid var(--error-color);
            padding: 16px;
            border-radius: 0 8px 8px 0;
            margin-bottom: 24px;
        }
        .error-detail p {
            color: var(--error-dark);
            font-size: 14px;
            line-height: 1.6;
        }
        .token-info {
            background: #f7fafc;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            padding: 16px;
            margin-bottom: 24px;
        }
        .token-info label {
            display: block;
            font-size: 12px;
            color: var(--text-secondary);
            text-transform: uppercase;
            letter-spacing: 0.05em;
            margin-bottom: 8px;
        }
        .token-info code {
            display: block;
            background: #2d3748;
            color: #e2e8f0;
            padding: 12px;
            border-radius: 6px;
            font-family: 'Monaco', 'Consolas', monospace;
            font-size: 12px;
            word-break: break-all;
        }
        .instructions {
            margin-bottom: 24px;
        }
        .instructions h3 {
            font-size: 16px;
            color: var(--text-primary);
            margin-bottom: 12px;
        }
        .instructions ol {
            padding-left: 20px;
            color: var(--text-secondary);
            font-size: 14px;
            line-height: 1.8;
        }
        .instructions code {
            background: #edf2f7;
            padding: 2px 6px;
            border-radius: 4px;
            font-family: 'Monaco', 'Consolas', monospace;
            font-size: 12px;
        }
        .btn {
            display: inline-block;
            padding: 12px 24px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            text-decoration: none;
            cursor: pointer;
            transition: all 0.2s ease;
            border: none;
        }
        .btn-primary {
            background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);
            color: white;
        }
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(66, 153, 225, 0.4);
        }
        .btn-secondary {
            background: #edf2f7;
            color: var(--text-primary);
            margin-left: 12px;
        }
        .btn-secondary:hover {
            background: #e2e8f0;
        }
        .actions {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
        }
        .status-code {
            position: absolute;
            top: 16px;
            right: 16px;
            background: rgba(255,255,255,0.2);
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
        }
        .error-header {
            position: relative;
        }
    </style>
</head>
<body>
    <div class="app-layout" style="align-items:flex-start;">
        <%@ include file="includes/sidebar.jsp" %>
        <div class="main-content">
            <div class="error-container">
        <div class="error-header">
            <span class="status-code">401</span>
            <div class="error-icon">🔒</div>
            <h1><%= request.getAttribute("errorTitle") != null ? request.getAttribute("errorTitle") : "Accès Refusé" %></h1>
            <p><%= request.getAttribute("errorMessage") != null ? request.getAttribute("errorMessage") : "Authentification requise" %></p>
        </div>
        
        <div class="error-body">
            <div class="error-detail">
                <p><%= request.getAttribute("errorDetail") != null ? request.getAttribute("errorDetail") : "Vous devez fournir un token valide pour accéder à cette ressource." %></p>
            </div>
            
            <% if (request.getAttribute("providedToken") != null && !((String)request.getAttribute("providedToken")).isEmpty()) { %>
            <div class="token-info">
                <label>Token fourni</label>
                <code><%= request.getAttribute("providedToken") %></code>
            </div>
            <% } %>
            
            <div class="instructions">
                <h3>Comment obtenir un token valide ?</h3>
                <ol>
                    <li>Accédez à <code>/tokens/create</code> pour générer un nouveau token</li>
                    <li>Copiez le token retourné dans la réponse JSON</li>
                    <li>Ajoutez le token à l'URL : <code>/reservations?token=VOTRE_TOKEN</code></li>
                </ol>
            </div>
            
            <div class="actions">
                <a href="tokens/create" class="btn btn-primary">Générer un Token</a>
                <a href="vehicules" class="btn btn-secondary">Retour à l'accueil</a>
            </div>
</div>
        </div>
    </div>
</body>
</html>
