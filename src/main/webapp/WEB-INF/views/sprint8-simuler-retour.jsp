<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Simuler Retour Véhicule - Sprint 8</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container py-4">
        <!-- Header -->
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h1><i class="bi bi-arrow-counterclockwise text-primary"></i> Simuler Retour Véhicule</h1>
                <p class="text-muted mb-0">Sprint 8 - Test de l'allocation prioritaire</p>
            </div>
            <a href="${pageContext.request.contextPath}/assignations/non-assignes" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left"></i> Retour
            </a>
        </div>

        <!-- Erreur -->
        <c:if test="${not empty error}">
            <div class="alert alert-danger">
                <i class="bi bi-exclamation-triangle"></i> ${error}
            </div>
        </c:if>

        <!-- Explication -->
        <div class="card mb-4 border-info">
            <div class="card-header bg-info text-white">
                <i class="bi bi-info-circle"></i> Comment ça fonctionne
            </div>
            <div class="card-body">
                <ol class="mb-0">
                    <li>Un véhicule revient à l'aéroport à une heure donnée</li>
                    <li>Le système récupère les passagers <strong>non assignés</strong> (priorité absolue)</li>
                    <li>Puis les nouvelles réservations dans la fenêtre de temps</li>
                    <li>L'allocation est calculée avec <strong>priorité aux non assignés</strong></li>
                    <li>Si le véhicule est plein → départ immédiat</li>
                </ol>
            </div>
        </div>

        <!-- Formulaire -->
        <div class="row">
            <div class="col-md-6">
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <i class="bi bi-truck"></i> Paramètres de simulation
                    </div>
                    <div class="card-body">
                        <form method="post" action="${pageContext.request.contextPath}/assignations/traiter-retour">
                            <div class="mb-3">
                                <label class="form-label">Véhicule</label>
                                <select name="vehiculeId" class="form-select" required>
                                    <option value="">-- Sélectionner un véhicule --</option>
                                    <c:forEach var="v" items="${vehicules}">
                                        <option value="${v.id}">
                                            ${v.nom} - ${v.capacite} places 
                                            (Attente: ${v.tempsAttente} min)
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            
                            <div class="mb-3">
                                <label class="form-label">Date</label>
                                <input type="date" name="date" class="form-control" 
                                       value="${defaultDate}" required />
                            </div>
                            
                            <div class="mb-3">
                                <label class="form-label">Heure de retour à l'aéroport</label>
                                <input type="time" name="returnTime" class="form-control" 
                                       value="${defaultTime}" required />
                                <small class="text-muted">
                                    Heure à laquelle le véhicule est de retour et disponible
                                </small>
                            </div>

                            <div class="d-grid gap-2">
                                <button type="submit" class="btn btn-primary btn-lg">
                                    <i class="bi bi-play-fill"></i> Simuler (sans persister)
                                </button>
                            </div>
                        </form>
                        
                        <hr />
                        
                        <form method="post" action="${pageContext.request.contextPath}/assignations/traiter-retour-persist">
                            <input type="hidden" name="vehiculeId" id="vehiculeId2" />
                            <input type="hidden" name="date" id="date2" />
                            <input type="hidden" name="returnTime" id="returnTime2" />
                            <button type="submit" class="btn btn-success w-100" id="btnPersist" disabled>
                                <i class="bi bi-check-circle"></i> Simuler ET Persister
                            </button>
                            <small class="text-muted">
                                Cette option enregistre les assignations en base de données
                            </small>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="card">
                    <div class="card-header bg-secondary text-white">
                        <i class="bi bi-diagram-3"></i> Algorithme Sprint 8
                    </div>
                    <div class="card-body">
                        <h6>1. Récupération prioritaire</h6>
                        <p class="small text-muted">
                            Les passagers non assignés des fenêtres précédentes sont récupérés 
                            en premier (ordre FIFO basé sur first_window_time).
                        </p>

                        <h6>2. Nouvelles réservations</h6>
                        <p class="small text-muted">
                            Les réservations dans la nouvelle fenêtre temporelle 
                            [returnTime, returnTime + temps_attente].
                        </p>

                        <h6>3. Allocation</h6>
                        <p class="small text-muted">
                            D'abord les prioritaires, puis les nouvelles avec 
                            l'algorithme de scoring Sprint 7.
                        </p>

                        <h6>4. Départ immédiat si plein</h6>
                        <p class="small text-muted">
                            Si le véhicule est rempli uniquement avec les prioritaires,
                            il part immédiatement sans attendre.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Synchroniser les champs pour le bouton "Persister"
        document.querySelector('select[name="vehiculeId"]').addEventListener('change', syncFields);
        document.querySelector('input[name="date"]').addEventListener('change', syncFields);
        document.querySelector('input[name="returnTime"]').addEventListener('change', syncFields);
        
        function syncFields() {
            const v = document.querySelector('select[name="vehiculeId"]').value;
            const d = document.querySelector('input[name="date"]').value;
            const t = document.querySelector('input[name="returnTime"]').value;
            
            document.getElementById('vehiculeId2').value = v;
            document.getElementById('date2').value = d;
            document.getElementById('returnTime2').value = t;
            
            document.getElementById('btnPersist').disabled = !(v && d && t);
        }
        
        // Init
        syncFields();
    </script>
</body>
</html>
