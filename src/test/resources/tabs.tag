<%@ include file="taglibs.jspf"%>

<%@ tag 
    language="java"
    pageEncoding="UTF-8"
    body-content="scriptless"
    description="" %>

<spring:bind path="form.activeTab">
    <input type="hidden" 
        id="${util:path2cssId(status.expression)}" name="${status.expression}"
        value="${status.value}" />
</spring:bind>

<skin:sub-tabs>
    <jsp:doBody/>
</skin:sub-tabs>
