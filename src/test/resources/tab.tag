<%@ include file="taglibs.jspf" %>

<%@ tag 
    language="java"
    pageEncoding="UTF-8"
    body-content="scriptless"
    description="Renders a link that looks like a tab." %>

<%@ attribute 
    name="url"
    description="The url the link points to. If this attribute is not specified the body will be rendered without a tab-like visual around it."
    required="false" %>

<%@ attribute 
    name="active"
    type="java.lang.Boolean"
    description="Whether the tab is active. Active tabs are rendered inverse."
    required="false" %>
    
<%@ attribute 
    name="auxillary"
    type="java.lang.Boolean"
    description="Whether the tab is auxillary. Auxillary tabs float right. If there are multiple auxillary tabs, the source should list them in reverse order."
    required="false" %>

<%@ attribute 
    name="last"
    type="java.lang.Boolean"
    description="If this is the last tab, i.e. the right-most tab in the tab bar."
    required="false" %>

<c:choose>
    <c:when test="${empty url}">
        <div class="${auxillary ? 'auxillary ' : ''}${last ? 'last ' : ''}${active ? 'active ' : ''}tab"><div class="tab-content">
            <jsp:doBody/>
        </div></div>
    </c:when>
    <c:otherwise>
        <a 
            href="<c:url value='${url}'/>" 
            class="${auxillary ? 'auxillary ' : ''}${last ? 'last ' : ''}${active ? 'active ' : ''}tab">
            <div class="n"><div class="s"><div class="w"><div class="nw"><div class="sw"><div class="e"><div class="ne"><div class="se tab-content">
                <jsp:doBody/>
            </div></div></div></div></div></div></div></div>
        </a>
    </c:otherwise>
</c:choose>
