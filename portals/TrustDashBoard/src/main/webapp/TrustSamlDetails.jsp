<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<link rel="stylesheet" type="text/css" href="/TrustDashBoard/CSS/JQueryHelperCSS/prettify.css" />

    <script type="text/javascript" src="/TrustDashBoard/Scripts/JQuery/jquery-1.7.2.js"></script>
    <script type="text/javascript" src="/TrustDashBoard/Scripts/JQuery/prettify.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<c:set var="hostName" scope="session" value="${hostName}"/>
<title>Trust Details <c:out value="${hostName}"></c:out></title>
</head>
<body>
    <span style="display: none" id="hostName"><c:out value="${hostName}"></c:out></span>
    <div id="showXMlData">
        
    </div>
    
<!--    <iframe id="showXMlData3">
        
    </iframe>
    <div id="showXMlData2" style="color: green;"> 
        
    </div>-->
    
    
<!--	<c:choose>
		<c:when test="${result == true}">
			<c:set var="saml" scope="session" value="${trustSamlDetails}"/>
		<c:out value="${saml}"/>			
		</c:when>
		<c:otherwise>
			<div class="errorMessage">
				<c:set var="message" scope="session" value="${message}"/>
				<c:out value="${message}"/>
			</div>
		</c:otherwise>
	</c:choose>-->
       <script type="text/javascript" src="/TrustDashBoard/Scripts/showXmlDetailsForHost.js"></script>
</body>
</html>