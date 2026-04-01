<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%--
    Sprint 8 : Section à inclure dans tracabilite-resultat.jsp et assignation-detail.jsp
    Affiche les passagers non assignés avec indication de priorité
--%>

<!-- Sprint 8 : Section Non Assignés -->
<c:if test="${not empty nonAssignes || not empty remainingReservations}">
    <div class="card mt-4 border-warning">
        <div class="card-header bg-warning text-dark d-flex justify-content-between align-items-center">
            <span>
                <i class="bi bi-exclamation-triangle"></i> 
                <strong>Passagers Non Assignés</strong>
                <span class="badge bg-danger ms-2">
                    <c:choose>
                        <c:when test="${not empty nonAssignes}">${nonAssignes.size()}</c:when>
                        <c:otherwise>${remainingReservations.size()}</c:otherwise>
                    </c:choose>
                    réservations
                </span>
            </span>
            <a href="${pageContext.request.contextPath}/assignations/non-assignes?date=${date}" 
               class="btn btn-sm btn-outline-dark">
                <i class="bi bi-arrow-right"></i> Voir détails
            </a>
        </div>
        <div class="card-body">
            <p class="text-muted mb-3">
                <i class="bi bi-info-circle"></i>
                Ces passagers seront <strong>prioritaires</strong> lors du prochain retour de véhicule.
            </p>
            
            <div class="table-responsive">
                <table class="table table-sm table-hover">
                    <thead class="table-secondary">
                        <tr>
                            <th>Priorité</th>
                            <th>Réservation</th>
                            <th>Client</th>
                            <th>Passagers restants</th>
                            <th>Fenêtre d'origine</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="r" items="${not empty nonAssignes ? nonAssignes : remainingReservations}" varStatus="status">
                            <tr class="${r.isPriority() ? 'table-warning' : ''}">
                                <td>
                                    <c:choose>
                                        <c:when test="${r.priorityOrder > 0}">
                                            <span class="badge bg-danger">
                                                <i class="bi bi-star-fill"></i> P${r.priorityOrder}
                                            </span>
                                        </c:when>
                                        <c:when test="${r.firstWindowTime != null}">
                                            <span class="badge bg-warning text-dark">
                                                <i class="bi bi-clock-history"></i> En attente
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">Nouveau</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td><strong>#${r.id}</strong></td>
                                <td>${r.nomClient}</td>
                                <td>
                                    <span class="badge bg-danger">${r.remaining}</span>
                                    / ${r.nombrePassager}
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${r.firstWindowTime != null}">
                                            <small class="text-muted">${r.firstWindowTime}</small>
                                        </c:when>
                                        <c:otherwise>
                                            <small class="text-muted">-</small>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
            
            <!-- Bouton action rapide -->
            <div class="mt-3 text-end">
                <a href="${pageContext.request.contextPath}/assignations/simuler-retour" 
                   class="btn btn-success">
                    <i class="bi bi-truck"></i> Simuler retour véhicule
                </a>
            </div>
        </div>
    </div>
</c:if>
