<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="models.Hotel" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Nouvelle Réservation - Back-office Hôtel</title>
    <style>
        * {
            box-sizing: border-box;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            margin: 0;
            padding: 20px;
        }
        .container {
            max-width: 600px;
            margin: 0 auto;
            background: white;
            border-radius: 10px;
            box-shadow: 0 15px 35px rgba(0,0,0,0.2);
            padding: 30px;
        }
        h1 {
            color: #333;
            text-align: center;
            margin-bottom: 30px;
        }
        .form-group {
            margin-bottom: 20px;
        }
        label {
            display: block;
            margin-bottom: 5px;
            color: #555;
            font-weight: 600;
        }
        input, select {
            width: 100%;
            padding: 12px;
            border: 2px solid #e1e1e1;
            border-radius: 5px;
            font-size: 14px;
            transition: border-color 0.3s;
        }
        input:focus, select:focus {
            outline: none;
            border-color: #667eea;
        }
        .row {
            display: flex;
            gap: 15px;
        }
        .row .form-group {
            flex: 1;
        }
        button {
            width: 100%;
            padding: 15px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 5px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s, box-shadow 0.2s;
        }
        button:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
        }
        .alert {
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
        .alert-success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .alert-error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        .hotel-info {
            font-size: 12px;
            color: #888;
        }
    </style>
</head>
<body>
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
                            <%= hotel.getNom() %> - <%= hotel.getEtoiles() %>⭐ - <%= hotel.getPrixParNuit() %>€/nuit
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
                    <label for="dateDepart">Date de départ *</label>
                    <input type="date" name="dateDepart" id="dateDepart" required>
                </div>
                <div class="form-group">
                    <label for="nombrePersonnes">Nombre de personnes *</label>
                    <input type="number" name="nombrePersonnes" id="nombrePersonnes" min="1" max="10" required>
                </div>
            </div>
            
            <div class="form-group">
                <label for="nomClient">Nom du client *</label>
                <input type="text" name="nomClient" id="nomClient" placeholder="Jean Dupont" required>
            </div>
            
            <div class="row">
                <div class="form-group">
                    <label for="emailClient">Email</label>
                    <input type="email" name="emailClient" id="emailClient" placeholder="jean@email.com">
                </div>
                <div class="form-group">
                    <label for="telephoneClient">Téléphone</label>
                    <input type="tel" name="telephoneClient" id="telephoneClient" placeholder="+33 6 12 34 56 78">
                </div>
            </div>
            
            <button type="submit">📝 Enregistrer la réservation</button>
        </form>
    </div>
</body>
</html>
