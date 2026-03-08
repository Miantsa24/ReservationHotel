<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="models.Hotel" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Nouvelle Réservation - Back-office Hôtel</title>
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
            max-width: 640px;
            margin: 0 auto;
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(20px);
            border-radius: var(--border-radius-large);
            box-shadow: var(--shadow-medium);
            padding: 48px;
            border: 1px solid rgba(255, 255, 255, 0.2);
            animation: fadeInUp 0.6s ease-out;
        }
        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        h1 {
            color: var(--text-primary);
            text-align: center;
            margin-bottom: 48px;
            font-size: 32px;
            font-weight: 800;
            letter-spacing: -0.025em;
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }
        .form-group {
            margin-bottom: 28px;
            position: relative;
        }
        label {
            display: block;
            margin-bottom: 8px;
            color: var(--text-secondary);
            font-weight: 600;
            font-size: 14px;
            letter-spacing: 0.025em;
            text-transform: uppercase;
        }
        input, select {
            width: 100%;
            padding: 16px 20px;
            border: 2px solid var(--secondary-color);
            border-radius: var(--border-radius);
            font-size: 16px;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            background: #ffffff;
            color: var(--text-primary);
        }
        input:focus, select:focus {
            outline: none;
            border-color: var(--primary-color);
            box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.15), var(--shadow-light);
            transform: translateY(-1px);
        }
        .row {
            display: flex;
            gap: 20px;
        }
        .row .form-group {
            flex: 1;
        }
        button {
            width: 100%;
            padding: 18px 24px;
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            color: white;
            border: none;
            border-radius: var(--border-radius);
            font-size: 16px;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            margin-top: 12px;
            position: relative;
            overflow: hidden;
        }
        button::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
            transition: left 0.5s;
        }
        button:hover::before {
            left: 100%;
        }
        button:hover {
            transform: translateY(-3px);
            box-shadow: var(--shadow-hover);
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
        .alert-success {
            background: linear-gradient(135deg, #f0fff4 0%, #c6f6d5 100%);
            color: #22543d;
            border: 1px solid var(--accent-color);
        }
        .alert-error {
            background: linear-gradient(135deg, #fed7d7 0%, #feb2b2 100%);
            color: #742a2a;
            border: 1px solid var(--error-color);
        }
        .hotel-info {
            font-size: 12px;
            color: var(--text-secondary);
            margin-top: 4px;
        }
        @media (max-width: 768px) {
            .container {
                padding: 32px 24px;
            }
            .row {
                flex-direction: column;
                gap: 16px;
            }
            h1 {
                font-size: 28px;
            }
        }
    </style>
</head>
<body>
    <div class="app-layout">
        <%@ include file="includes/sidebar.jsp" %>
        <div class="main-content">
            <div class="container">
        <h1>🏨 Nouvelle Réservation</h1>
        
        <% if (request.getAttribute("success") != null) { %>
            <div class="alert alert-success">
                ✅ <%= request.getAttribute("success") %>
            </div>
        <% } %>
        
        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-error">
                ❌ <%= request.getAttribute("error") %>
            </div>
        <% } %>
        
        <form action="${pageContext.request.contextPath}/reservation/save" method="POST">
            <div class="form-group">
                <label for="hotelId">Hôtel *</label>
                <select name="hotelId" id="hotelId" required>
                    <option value="">-- Sélectionnez un hôtel --</option>
                    <% 
                    List<Hotel> hotels = (List<Hotel>) request.getAttribute("hotels");
                    if (hotels != null) {
                        for (Hotel hotel : hotels) { 
                    %>
                        <option value="<%= hotel.getId() %>">
                            <%= hotel.getNom() %> (<%= hotel.getCode() %>)
                        </option>
                    <% 
                        }
                    } 
                    %>
                </select>
            </div>
            
            <div class="row">
                <div class="form-group">
                    <label for="dateArrivee">Date d'arrivée *</label>
                    <input type="date" name="dateArrivee" id="dateArrivee" required>
                </div>
                <div class="form-group">
                    <label for="heureArrivee">Heure d'arrivée *</label>
                    <input type="time" name="heureArrivee" id="heureArrivee" required>
                </div>
            </div>
            
            <div class="row">
                <div class="form-group">
                    <label for="nombrePersonnes">Nombre de personnes *</label>
                    <input type="number" name="nombrePersonnes" id="nombrePersonnes" min="1" required>
                </div>
                <div class="form-group">
                    <label for="refClient">Référence client *</label>
                    <input type="text" name="refClient" id="refClient" placeholder="REF123" required>
                </div>
            </div>
            
            <button type="submit">📝 Enregistrer la réservation</button>
        </form>
            </div>
        </div>
    </div>
</body>
</html>
