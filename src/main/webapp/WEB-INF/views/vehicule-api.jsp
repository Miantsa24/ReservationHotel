<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Liste des véhicules - Front Office</title>
    <style>
        body { font-family: Arial, sans-serif; padding: 24px; background: #f7fafc; color: #2d3748; }
        .container { max-width: 800px; margin: auto; }
        h1 { text-align: center; margin-bottom: 24px; }
        table { width: 100%; border-collapse: collapse; margin-top: 16px; }
        th, td { padding: 12px; border: 1px solid #ccc; text-align: left; }
        .error { color: #c53030; margin-top: 16px; }
    </style>
</head>
<body>
    <div class="app-layout">
        <%@ include file="includes/sidebar.jsp" %>
        <div class="main-content">
            <div class="container">
    <h1>Véhicules (API protégée)</h1>
    <div>
        <label for="token">Token :</label>
        <input type="text" id="token" size="40" />
        <button onclick="loadVehicules()">Charger</button>
    </div>
    <div id="message" class="error"></div>
    <table id="vehiculeTable" style="display:none;">
        <thead>
        <tr>
            <th>Marque</th>
            <th>Capacité</th>
            <th>Type carburant</th>
            <th>Vitesse moyenne</th>
            <th>Temps attente</th>
        </tr>
        </thead>
        <tbody></tbody>
    </table>
            </div>
        </div>
    </div>
<script>
    function loadVehicules() {
        document.getElementById('message').textContent = '';
        var token = document.getElementById('token').value;
        fetch('/ReservationHotel/api/vehicules?token=' + encodeURIComponent(token))
            .then(function(resp) { return resp.json(); })
            .then(function(data) {
                if (!data.success) {
                    document.getElementById('vehiculeTable').style.display = 'none';
                    document.getElementById('message').textContent = data.message || 'Erreur';
                } else {
                    var tbody = document.querySelector('#vehiculeTable tbody');
                    tbody.innerHTML = '';
                    data.vehicules.forEach(function(v) {
                        var row = '<tr>' +
                            '<td>' + v.marque + '</td>' +
                            '<td>' + v.capacite + '</td>' +
                            '<td>' + v.typeCarburant + '</td>' +
                            '<td>' + v.vitesseMoyenne + '</td>' +
                            '<td>' + v.tempsAttente + '</td>' +
                            '</tr>';
                        tbody.innerHTML += row;
                    });
                    document.getElementById('vehiculeTable').style.display = '';
                }
            })
            .catch(function(err) {
                document.getElementById('message').textContent = 'Erreur de communication';
            });
    }
</script>
</body>
</html>