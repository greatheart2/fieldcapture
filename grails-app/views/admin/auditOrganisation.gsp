<%@ page import="au.org.ala.merit.DateUtils" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="adminLayout"/>
		<title>Admin - Audit Organisation | MERIT | Atlas of Living Australia</title>
        <asset:stylesheet src="audit.css"/>
	</head>
	<body>
        <g:set var="searchTerm" value="${params.searchTerm}"/>


        <h3>Organisation Audit - ${organisation.name}</h3>


        <div class="row">
            <div class="span12 text-right">
                <a href="${createLink(action:'auditOrganisationSearch',params:[searchTerm: searchTerm])}" class="btn btn-default btn-small"><i class="icon-backward"></i> Back</a>
            </div>
        </div>

        <g:set var="returnTo" value="${createLink(action:'auditOrganisation', id:organisation.organisationId, params:[searchTerm:params.searchTerm])}"/>
        <g:render template="auditMessageList"></g:render>

        <asset:javascript src="audit.js"/>
    </body>
</html>

