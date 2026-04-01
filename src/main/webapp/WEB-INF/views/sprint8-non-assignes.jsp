<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Passagers Non Assignés - Sprint 8</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        .priority-badge { background-color: #dc3545; color: white; }
        .waiting-card { border-left: 4px solid #ffc107; }
        .stat-card { border-radius: 10px; }
        .urgent { animation: pulse 2s infinite; }
        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.7; }
        }
    </style>
</head>
<body class="bg-light">
    <div class="container py-4">
        <!-- Header -->
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h1><i class="bi bi-people-fill text-warning"></i> Passagers Non Assignés</h1>
                <p class="text-muted mb-0">Sprint 8 - Priorisation automatique</p>
            </div>
            <a href="${pageContext.request.contextPath}/assignations" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left"></i> Retour
            </a>
        </div>

        <!-- Erreur -->
        <c:if test="${not empty error}">
            <div class="alert alert-danger">
                <i class="bi bi-exclamation-triangle"></i> ${error}
            </div>
        </c:if>

        <!-- Sélection de date -->
        <div class="card mb-4">
            <div class="card-body">
                <form method="get" action="${pageContext.request.contextPath}/assignations/non-assignes" class="row g-3">
                    <div class="col-md-4">
                        <label class="form-label">Date</label>
                        <input type="date" name="date" value="${date}" class="form-control" />
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <button type="submit" class="btn btn-primary w-100">
                            <i class="bi bi-search"></i> Rechercher
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- Statistiques -->
        <div class="row mb-4">
            <div class="col-md-4">
                <div class="card stat-card bg-warning text-dark">
                    <div class="card-body text-center">
                        <h2 class="mb-0">${totalReservations}</h2>
                        <small>Réservations en attente</small>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card stat-card bg-danger text-white">
                    <div class="card-body text-center">
                        <h2 class="mb-0">${totalPassengersWaiting}</h2>
                        <small>Passagers à placer</small>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card stat-card bg-success text-white">
                    <div class="card-body text-center">
                        <h2 class="mb-0">${availableVehicules.size()}</h2>
                        <small>Véhicules disponibles</small>
                    </div>
                </div>
            </div>
        </div>

        <!-- Action rapide -->
        <c:if test="${totalPassengersWaiting > 0 && not empty availableVehicules}">
            <div class="card mb-4 border-success">
                <div class="card-header bg-success text-white">
                    <i class="bi bi-lightning-charge"></i> Action Rapide - Traiter Retour Véhicule
                </div>
                <div class="card-body">
                    <form method="post" action="${pageContext.request.contextPath}/assignations/traiter-retour-persist" class="row g-3">
                        <input type="hidden" name="date" value="${date}" />
                        <div class="col-md-4">
                            <label class="form-label">Véhicule</label>
                            <select name="vehiculeId" class="form-select" required>
                                <option value="">-- Sélectionner --</option>
                                <c:forEach var="v" items="${availableVehicules}">
                                    <option value="${v.id}">
                                        ${v.nom} (${v.capacite} places)
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">Heure de retour</label>
                            <input type="time" name="returnTime" class="form-control" required 
                                   value="<%= new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date()) %>" />
                        </div>
                        <div class="col-md-3 d-flex align-items-end">
                            <button type="submit" class="btn btn-success w-100">
                                <i class="bi bi-play-fill"></i> Lancer Allocation
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </c:if>

        <!-- Liste des non assignés -->
        <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span><i class="bi bi-list-ul"></i> Détail des réservations non assignées</span>
                <span class="badge bg-warning">${totalReservations} réservations</span>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty unassigned}">
                        <div class="text-center py-5 text-muted">
                            <i class="bi bi-check-circle display-3 text-success"></i>
                            <p class="mt-3">Tous les passagers ont été assignés !</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead class="table-dark">
                                    <tr>
                                        <th>ID</th>
                                        <th>Client</th>
                                        <th>Heure Arrivée</th>
                                        <th>Total Passagers</th>
                                        <th>Assignés</th>
                                        <th>En Attente</th>
                                        <th>Priorité</th>
                                        <th>Fenêtre Origine</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="r" items="${unassigned}">
                                        <tr class="waiting-card">
                                            <td><strong>#${r.id}</strong></td>
                                            <td>${r.nomClient}</td>
                                            <td>
                                                <i class="bi bi-clock"></i> ${r.heureArrivee}
                                            </td>
                                            <td>${r.nombrePassager}</td>
                                            <td>
                                                <span class="badge bg-info">
                                                    ${r.assignedCount}
                                                </span>
                                            </td>
                                            <td>
                                                <span class="badge bg-danger urgent">
                                                    ${r.remaining}
                                                </span>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${r.priorityOrder > 0}">
                                                        <span class="badge priority-badge">
                                                            <i class="bi bi-star-fill"></i> P${r.priorityOrder}
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-muted">-</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:if test="${r.firstWindowTime != null}">
                                                    <small class="text-muted">
                                                        ${r.firstWindowTime}
                                                    </small>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- Lien vers simulation -->
        <div class="mt-4 text-center">
            <a href="${pageContext.request.contextPath}/assignations/simuler-retour" class="btn btn-outline-primary">
                <i class="bi bi-gear"></i> Simuler un retour de véhicule
            </a>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
