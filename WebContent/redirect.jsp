<% 
	String path=request.getParameter("path");
	System.out.println("JSP redirect path: " + path);
	response.sendRedirect(request.getParameter("path")); 
%>
