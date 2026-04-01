<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%--
    Sprint 8 : Section pour afficher la distinction entre passagers prioritaires et nouveaux
    À inclure dans assignation-detail.jsp ou tracabilite-resultat.jsp
--%>

<!-- Sprint 8 : Séparation Prioritaires vs Nouvelles réservations -->
<c:if test="${not empty assignments || not empty result}">
    <div class="row mt-4">
        <!-- Colonne : Passagers Prioritaires (Non assignés des fenêtres précédentes) -->
        <div class="col-md-6">
            <div class="card border-danger h-100">
                <div class="card-header bg-danger text-white">
                    <i class="bi bi-star-fill"></i> 
                    <strong>PRIORITAIRES</strong>
                    <small class="ms-2">(Non assignés précédents)</small>
                </div>
                <div class="card-body">
                    <c:set var="hasPriority" value="false" />
                    <c:forEach var="a" items="${not empty assignments ? assignments : result.assignments}">
                        <c:if test="${a.reservation.isPriority()}">
                            <c:set var="hasPriority" value="true" />
                        </c:if>
                    </c:forEach>
                    
                    <c:choose>
                        <c:when test="${hasPriority}">
                            <ul class="list-group list-group-flush">
                                <c:forEach var="a" items="${not empty assignments ? assignments : result.assignments}">
                                    <c:if test="${a.reservation.isPriority()}">
                                        <li class="list-group-item d-flex justify-content-between align-items-center">
                                            <div>
                                                <strong>#${a.reservation.id}</strong> - ${a.reservation.nomClient}
                                                <br/>
                                                <small class="text-muted">
                                                    Depuis: ${a.reservation.firstWindowTime}
                                                </small>
                                            </div>
                                            <span class="badge bg-success rounded-pill">
                                                ${a.passengersAssigned} passagers
                                            </span>
                                        </li>
                                    </c:if>
                                </c:forEach>
                            </ul>
                        </c:when>
                        <c:otherwise>
                            <p class="text-muted text-center py-4">
                                <i class="bi bi-check-circle display-6 text-success"></i>
                                <br/>Aucun passager prioritaire
                            </p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <!-- Colonne : Nouvelles Réservations -->
        <div class="col-md-6">
            <div class="card border-primary h-100">
                <div class="card-header bg-primary text-white">
                    <i class="bi bi-plus-circle"></i> 
                    <strong>NOUVELLES RÉSERVATIONS</strong>
                    <small class="ms-2">(Fenêtre actuelle)</small>
                </div>
                <div class="card-body">
                    <c:set var="hasNew" value="false" />
                    <c:forEach var="a" items="${not empty assignments ? assignments : result.assignments}">
                        <c:if test="${!a.reservation.isPriority()}">
                            <c:set var="hasNew" value="true" />
                        </c:if>
                    </c:forEach>
                    
                    <c:choose>
                        <c:when test="${hasNew}">
                            <ul class="list-group list-group-flush">
                                <c:forEach var="a" items="${not empty assignments ? assignments : result.assignments}">
                                    <c:if test="${!a.reservation.isPriority()}">
                                        <li class="list-group-item d-flex justify-content-between align-items-center">
                                            <div>
                                                <strong>#${a.reservation.id}</strong> - ${a.reservation.nomClient}
                                                <br/>
                                                <small class="text-muted">
                                                    Arrivée: ${a.reservation.heureArrivee}
                                                </small>
                                            </div>
                                            <span class="badge bg-info rounded-pill">
                                                ${a.passengersAssigned} passagers
                                            </span>
                                        </li>
                                    </c:if>
                                </c:forEach>
                            </ul>
                        </c:when>
                        <c:otherwise>
                            <p class="text-muted text-center py-4">
                                <i class="bi bi-inbox display-6"></i>
                                <br/>Aucune nouvelle réservation
                            </p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
</c:if>

<!-- Sprint 8 : Bouton Simuler retour véhicule -->
<div class="mt-4 text-center">
    <a href="${pageContext.request.contextPath}/assignations/simuler-retour" 
       class="btn btn-outline-success btn-lg">
        <i class="bi bi-arrow-counterclockwise"></i> Simuler retour véhicule
    </a>
    <a href="${pageContext.request.contextPath}/assignations/non-assignes?date=${date}" 
       class="btn btn-outline-warning btn-lg ms-2">
        <i class="bi bi-people"></i> Voir non assignés
    </a>
</div>
