<%@ include file="taglibs.jspf"%>

<%@ tag 
    language="java" 
    pageEncoding="UTF-8" 
    body-content="scriptless"
    description="" %>
    
<%@ attribute 
    name="availability"
    description=""
    type="java.lang.Object"
    required="false" %>

<%@ attribute 
    name="clazz"
    description=""
    type="java.lang.String"
    required="false" %>

<%@ attribute 
    name="selectable"
    description=""
    type="java.lang.Boolean"
    required="false" %>

<%@ attribute 
    name="dateStyle"
    description=""
    type="java.lang.String"
    required="false" %>

<c:if test="${empty availability}">
    <c:set var="availability" value="${_availability}" />
</c:if>

<tr 
    class="${ 0 == _loop_index % 2 ? 'even' : 'odd' } ${clazz}" 
>
    <c:choose>
        <c:when test="${empty selectable || selectable}">
            <pager:selector key="${availability.naturalKey}" disabled="${availability == currentAvailability}" />
        </c:when>
        <c:otherwise>
            <td></td>
        </c:otherwise>
    </c:choose>
    <td></td>
    <td 
        <c:if test="${availability.scope.territory == null}" >class="territory"</c:if> 
    >
        <c:choose>
            <c:when test="${availability != currentAvailability && ( empty selectable || selectable ) }" >
                <pager:edit-link key="${availability.naturalKey}">
                    <c:out value="${availability.scope.name}" />
                </pager:edit-link>
            </c:when>
            <c:otherwise>
                <c:out value="${availability.scope.name}" />
            </c:otherwise>
        </c:choose>
    </td>
    <c:if test="${_wholesalePrice}">
        <td>
            <c:choose>
                <c:when test="${availability.condition.wholesalePrice != null}">
                    <c:out value="${availability.condition.wholesalePrice}" />
                    <c:out value="${availability.scope.currency}" />
                </c:when>
                <c:otherwise>
                    N/A
                </c:otherwise>
            </c:choose>
        </td>
    </c:if>
    <c:if test="${_retailPrice}">
        <td>
            <c:choose>
                <c:when test="${availability.condition.suggestedRetailPrice != null}">
                    <c:out value="${availability.condition.suggestedRetailPrice}" />
                    <c:out value="${availability.scope.currency}" />
                </c:when>
                <c:otherwise>
                    N/A
                </c:otherwise>
            </c:choose>
        </td>
    </c:if>
    <td>
        <c:choose>
            <c:when test="${availability.condition.startDate != null}">
                <fmt:formatDate value="${availability.condition.startDate}" dateStyle="${empty dateStyle ? 'full' : dateStyle}"/>
            </c:when>
            <c:otherwise>
                &infin;
            </c:otherwise>
        </c:choose>
    </td>
    <td>
        <c:choose>
            <c:when test="${availability.condition.endDate != null}">
                <fmt:formatDate value="${availability.condition.endDate}" dateStyle="${empty dateStyle ? 'full' : dateStyle}"/>
            </c:when>
            <c:otherwise>
                &infin;
            </c:otherwise>
        </c:choose>
    </td>
    <td class="last" align="right"><jsp:doBody/></td>
</tr>
