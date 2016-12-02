<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="taglibs.jspf"%>
<skin:page>
    <!--  comment -->
    <skin:head>
        <title><spring:message code="editProduct.pageTitle" text="Test"/></title>
        <%-- crazy comment --%>
        <pager:javascript/>

        <script type="text/javascript">
        
            function doSubmit( code ) {
                if( code == null ) {
                    code = ${submissionTypes.UPDATE.code};
                }
                submitCode( code );
            }
            
            function selectionChanged() {
                <c:if test="${ bundles.totalResults > 0 }">
                    if( Pager_selectionChanged( ${bundles.totalResults} ) > 0 ) {
                        Form.Element.enable( 'deleteBundles' );
                    } else {
                        Form.Element.disable( 'deleteBundles' );
                    }
                </c:if>
            }
            
            function confirmAndDelete() {
                var selectedRows = Pager_countSelectedRows( ${bundles.totalResults} );
                if( selectedRows > 0 ) {
                    if( selectedRows > 5 ) {
                        var message = '<spring:message code="bundle.confirmAndDelete.largeDeletePrompt" text="If you are sure that you want to DELETE {0} bundles, please enter that number below."/>';
                        var response = window.prompt(format(message, selectedRows), '');
                        if( ""+response  != ""+selectedRows ) return;
                    } else if( selectedRows > 1 ) {
                        var message = '<spring:message code="bundle.confirmAndDelete.smallDeletePrompt" text="Are you sure you want to delete the selected {0} bundles?"/>';
                        if( ! window.confirm(format(message, selectedRows))) {
                            return;
                        }
                    }
                    else {
                        var message = '<spring:message code="bundle.confirmAndDelete.singleDeletePrompt" text="Are you sure you want to delete the selected bundle?"/>';
                        if( ! window.confirm(message)) {
                            return;
                        }
                    }
                    doSubmit( ${submissionTypes.DELETE.code} );
                }
            }
            
            function onLoad() {
                setSubmitButton( $('search') );
                Form.Element.activate( 'criteria-text' );
                selectionChanged();
                filterTypeChanged();
            }

            function filterTypeChanged() {
                if( $('criteria-filterType').value == ${filterTypeCodes.ALL.code} ) {
                    $('criteria-text').hide();
                } else {
                    $('criteria-text').show();
                }
            }
        </script>
    </skin:head>
    <skin:body bodyId="bundle" bodyOnLoad="onLoad();">
        <skin:form>
        
            <spring:hasBindErrors name="form">
                <span class="errors">
                    <c:forEach items="${errors.globalErrors}" var="error">
                        <spring:message code="${error.code}" arguments="${error.arguments}" text="${error.defaultMessage}"/>
                    </c:forEach>
                </span>
            </spring:hasBindErrors>
        
            <div class="pullOut"><table class="stretched form">
                <tr>
                    <td class="first label" colspan="6">
                        <h2 class="first"><spring:message code="bundle.form.manageBundles" text="Manage Bundles"/></h2>
                    </td>
                    <td>
                        <button id="createBundle" class="submit" title="<spring:message code="bundle.form.createBundleButtonTitle" text="Create a new bundle."/>" onclick="doSubmit( ${submissionTypes.NEW.code} );" ><spring:message code="bundle.form.createBundleButtonText" text="Create&nbsp;&hellip;"/></button>
                    </td>
                    <td class="field">
                        <button id="deleteBundles" class="submit" title="<spring:message code="bundle.form.deleteBundlesButtonTitle" text="Delete selected bundle(s)."/>" onclick="confirmAndDelete(); return false;" disabled="disabled"><spring:message code="bundle.form.deleteBundlesButtonText" text="Delete&nbsp;&hellip;"/></button>
                    </td>
                </tr>
                <tr>
                    <td class="first label"><spring:message code="bundle.form.listBundles" text="List bundles"/></td>
                    <td class="field">
                        <spring:bind path="form.criteria.filterType" >
                            <select
                                id="${util:path2cssId(status.expression)}" name="${status.expression}"
                                ${status.error?'class="error"':''} 
                                onchange="filterTypeChanged()"
                                onkeyup="filterTypeChanged()"
                            >
                                <c:forEach items="${filterTypes}" var="filterType">
                                    <option
                                        value="${filterType.code}"
                                        ${filterType.code == status.value ? 'selected="selected"' : ''} >
                                        <spring:message code="${filterType.messageKey}" text="${filterType.defaultMessage}"/>                                    
                                    </option>
                                </c:forEach>
                            </select>
                            <c:if test="${status.error}"><span class="errors">
                                <c:forEach items="${status.errorMessages}" var="error">
                                    <c:out value="${error}" />
                                </c:forEach>
                            </span></c:if>
                        </spring:bind>
                    </td>
        
                    <td class="field">
                        <spring:bind path="form.criteria.text">
                            <input type="text"
                                maxlength="50"
                                id="${util:path2cssId(status.expression)}" name="${status.expression}"
                                value="${status.value}"
                                ${status.error?'class="error"':''} />
                            <c:if test="${status.error}"><span class="errors">
                                <c:forEach items="${status.errorMessages}" var="error">
                                    <c:out value="${error}" />
                                </c:forEach>
                            </span></c:if>
                        </spring:bind>
                    </td>
        
                    <td class="label"><spring:message code="bundle.form.availableTo" text="available to"/></td>
                    <td class="field">
                        <scope:select path="form.criteria.scopeId">
                            <option value="" ${status.value == null ? 'selected="selected"' : ''}>
                                <spring:message code="bundle.form.anyAffiliate" text="any affiliate"/>
                            </option>
                        </scope:select>
                    </td>
                    <td class="fill"></td>
                    <td class="field">
                        <button id="search" class="submit" title="<spring:message code="bundle.form.searchButtonTitle" text="Search for bundles that match the given criteria."/>" onclick="doSubmit( ${submissionTypes.SEARCH.code} );" ><spring:message code="bundle.form.searchButtonText" text="Search"/></button>
                    </td>
                    <td class="last field">
                        <button id="reset" class="submit" title="<spring:message code="bundle.form.resetButtonTitle" text="Reset this form to its initial state."/>" onclick="doSubmit( ${submissionTypes.RESET.code} );" ><spring:message code="bundle.form.resetButtonText" text="Reset"/></button>
                    </td>
                </tr>
            </table></div>
        
            <spring:bind path="form.submissionType">
                <input type="hidden"
                    id="${util:path2cssId(status.expression)}" name="${status.expression}"
                    value="${status.value}" />
            </spring:bind>
        
            <c:if test="${bundles.totalResults > 0}">
                    <pager:pager>
                        <tr>
                            <pager:selector-header/>
                            <pager:counts-header/>
                            <pager:header sortType="${sortTypes.TITLE.code}"><spring:message code="bundle.form.pager.title" text="Title"/></pager:header>
                            <pager:header sortType="${sortTypes.KEY.code}"><spring:message code="bundle.form.pager.bundleKey" text="Bundle Key"/></pager:header>
                            <pager:header><spring:message code="bundle.form.pager.startDate" text="Start Date"/></pager:header>
                            <pager:header><spring:message code="bundle.form.pager.endDate" text="End Date"/></pager:header>
                            <pager:header sortType="${sortTypes.CHANGED_AT.code}"><spring:message code="bundle.form.pager.lastChange" text="Last Change"/></pager:header>
                            <pager:header last="true" sortType="${sortTypes.CHANGED_BY.code}"><spring:message code="bundle.form.pager.changedBy" text="Changed By"/></pager:header>
                        </tr>
                        <c:forEach var="bundle" items="${bundles.page}" varStatus="loop">
                            <tr class="${ 0 == loop.index % 2 ? 'even' : 'odd' }">
                                <pager:selector key="${bundle.naturalKey}" />
                                <td>
                                    <pager:edit-link key="${bundle.naturalKey}" title="${util:truncate(bundle.description,400)}">
                                        <c:choose>
                                            <c:when test="${bundle.smallImage != null}">
                                                  <c:url value="/app/resource/download" var="smallImageUrl" scope="page">
                                                <c:param name="resource" value="${bundle.smallImage}" />
                                                  </c:url>
                                                <img src="${smallImageUrl}" border="0"/>
                                            </c:when>
                                            <c:otherwise>
                                                <img src="<c:url value="/images/bundle-s.png" />" border="0"/>
                                            </c:otherwise>
                                        </c:choose>
                                    </pager:edit-link>
                                </td>
                                <td>
                                    <pager:edit-link key="${bundle.naturalKey}" title="${util:truncate(bundle.description,400)}">
                                        ${util:truncate(bundle.title,32)}
                                    </pager:edit-link>
                                </td>
                                <td class="monospace">${util:truncate(bundle.key,36)}</td>
                                <td class="<c:if test="${!bundle.availabilityDates.startDatesSame}">inconsistentDates</c:if>">
		                            <c:choose>
		                    			<c:when test="${bundle.availabilityDates.datesDefined}">
		                    				<c:choose>
		                   						<c:when test="${bundle.availabilityDates.minStartDate == null}">&infin;</c:when>
		                   						<c:otherwise><fmt:formatDate pattern="MM/dd/yyyy" value="${bundle.availabilityDates.minStartDate}" /></c:otherwise>
		                   					</c:choose>
		                    			</c:when>
		                    			<c:otherwise>&mdash;</c:otherwise>
		                    		</c:choose>
		                    	</td>
		                    	<td class="<c:if test="${!bundle.availabilityDates.endDatesSame}">inconsistentDates</c:if>">
		                    		<c:choose>
		                    			<c:when test="${bundle.availabilityDates.datesDefined}">
		                    				<c:choose>
		                   						<c:when test="${bundle.availabilityDates.maxEndDate == null}">&infin;</c:when>
		                   						<c:otherwise><fmt:formatDate pattern="MM/dd/yyyy" value="${bundle.availabilityDates.maxEndDate}" /></c:otherwise>
		                   					</c:choose>
		                    			</c:when>
		                    			<c:otherwise>&mdash;</c:otherwise>
		                    		</c:choose>
                    			</td>
                                <td><fmt:formatDate pattern="MM/dd/yyyy" value="${bundle.changedAt}"/></td>
                                <td class="last"><c:out value="${bundle.changedBy}"/></td>
                            </tr>
                        </c:forEach>
                    </pager:pager>
            </c:if>
        
            <c:if test="${bundles.totalResults < 1}">
                <p><spring:message code="bundle.form.noResults" text="No bundles match the given criteria."/></p>
            </c:if>
        
            <pager:navigator/>
            
        </skin:form>
    </skin:body>
</skin:page>
