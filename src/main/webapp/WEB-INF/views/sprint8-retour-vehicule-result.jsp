<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Résultat Allocation - Sprint 8</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        .priority-row { background-color: #fff3cd !important; }
        .full-vehicle { background-color: #d4edda; }
        .immediate-departure { animation: blink 1s infinite; }
        @keyframes blink {
            0%, 100% { background-color: #28a745; }
            50% { background-color: #218838; }
        }
    </style>
</head>
<body class="bg-light">
    <div class="container py-4">
        <!-- Header -->
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h1><i class="bi bi-check-circle text-success"></i> Résultat de l'Allocation</h1>
                <p class="text-muted mb-0">Sprint 8 - Traitement retour véhicule</p>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/assignations/non-assignes?date=${date}" class="btn btn-outline-primary me-2">
                    <i class="bi bi-people"></i> Non Assignés
                </a>
                <a href="${pageContext.request.contextPath}/assignations/simuler-retour" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-counterclockwise"></i> Nouvelle simulation
                </a>
            </div>
        </div>

        <!-- Erreur -->
        <c:if test="${not empty error}">
            <div class="alert alert-danger">
                <i class="bi bi-exclamation-triangle"></i> ${error}
            </div>
        </c:if>

        <!-- Warning -->
        <c:if test="${not empty warning}">
            <div class="alert alert-warning">
                <i class="bi bi-exclamation-circle"></i> ${warning}
                <br/>Véhicule ID: ${vehiculeId}
            </div>
        </c:if>

        <!-- Succès -->
        <c:if test="${not empty result}">
            <!-- Message principal -->
            <c:choose>
                <c:when test="${vehicleFull}">
                    <div class="alert alert-success immediate-departure text-white text-center">
                        <h3><i class="bi bi-truck"></i> ${message}</h3>
                        <p class="mb-0">Le véhicule est plein avec les passagers prioritaires. Pas besoin d'attendre la fin de la fenêtre.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="alert alert-info">
                        <h4><i class="bi bi-info-circle"></i> ${message}</h4>
                        <p class="mb-0">Des places restent disponibles. Continuer à collecter jusqu'à la fin de la fenêtre.</p>
                    </div>
                </c:otherwise>
            </c:choose>

            <!-- Persisté ? -->
            <c:if test="${persisted}">
                <div class="alert alert-success">
                    <i class="bi bi-database-check"></i> Les assignations ont été enregistrées en base de données.
                </div>
            </c:if>

            <!-- Infos véhicule -->
            <div class="card mb-4">
                <div class="card-header bg-primary text-white">
                    <i class="bi bi-truck"></i> Véhicule: ${vehicule.nom}
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-3">
                            <strong>Capacité:</strong> ${vehicule.capacite} places
                        </div>
                        <div class="col-md-3">
                            <strong>Date:</strong> ${date}
                        </div>
                        <div class="col-md-3">
                            <strong>Heure retour:</strong> ${returnTime}
                        </div>
                        <div class="col-md-3">
                            <strong>Temps attente:</strong> ${vehicule.tempsAttente} min
                        </div>
                    </div>
                </div>
            </div>

            <!-- Statistiques -->
            <div class="row mb-4">
                <div class="col-md-4">
                    <div class="card text-center bg-success text-white">
                        <div class="card-body">
                            <h2>${totalAssigned}</h2>
                            <small>Réservations assignées</small>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card text-center bg-warning text-dark">
                        <div class="card-body">
                            <h2>${totalRemaining}</h2>
                            <small>Restent non assignées</small>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card text-center ${vehicleFull ? 'bg-success' : 'bg-info'} text-white">
                        <div class="card-body">
                            <h2>${vehicleFull ? 'PLEIN' : 'PARTIEL'}</h2>
                            <small>État du véhicule</small>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Assignations -->
            <div class="card mb-4">
                <div class="card-header bg-success text-white">
                    <i class="bi bi-check-all"></i> Passagers Assignés (${result.assignments.size()})
                </div>
                <div class="card-body">
                    <c:choose>
                        <c:when test="${empty result.assignments}">
                            <p class="text-muted text-center">Aucune assignation effectuée.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead class="table-dark">
                                        <tr>
                                            <th>Type</th>
                                            <th>Réservation</th>
                                            <th>Client</th>
                                            <th>Passagers Assignés</th>
                                            <th>Heure Arrivée</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="a" items="${result.assignments}">
                                            <tr class="${a.reservation.isPriority() ? 'priority-row' : ''}">
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${a.reservation.isPriority()}">
                                                            <span class="badge bg-danger">
                                                                <i class="bi bi-star-fill"></i> PRIORITÉ
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-secondary">Nouveau</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td><strong>#${a.reservation.id}</strong></td>
                                                <td>${a.reservation.nomClient}</td>
                                                <td>
                                                    <span class="badge bg-success">${a.passengersAssigned}</span>
                                                    / ${a.reservation.nombrePassager}
                                                </td>
                                                <td>${a.reservation.heureArrivee}</td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- Restants (prochaine priorité) -->
            <c:if test="${not empty result.remainingReservations}">
                <div class="card border-warning">
                    <div class="card-header bg-warning text-dark">
                        <i class="bi bi-hourglass-split"></i> 
                        Passagers reportés (priorité pour prochain véhicule): ${result.remainingReservations.size()}
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-sm">
                                <thead>
                                    <tr>
                                        <th>Réservation</th>
                                        <th>Client</th>
                                        <th>Passagers restants</th>
                                        <th>Nouvelle priorité</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="r" items="${result.remainingReservations}">
                                        <tr>
                                            <td>#${r.id}</td>
                                            <td>${r.nomClient}</td>
                                            <td>
                                                <span class="badge bg-danger">${r.remaining}</span>
                                            </td>
                                            <td>
                                                <i class="bi bi-star text-warning"></i> 
                                                Sera prioritaire
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                        <div class="alert alert-info mt-3 mb-0">
                            <i class="bi bi-info-circle"></i>
                            Ces passagers seront automatiquement prioritaires lors du prochain retour de véhicule.
                        </div>
                    </div>
                </div>
            </c:if>
        </c:if>

        <!-- Navigation -->
        <div class="mt-4 text-center">
            <a href="${pageContext.request.contextPath}/tracabilite" class="btn btn-primary">
                <i class="bi bi-list-check"></i> Voir Traçabilité
            </a>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
