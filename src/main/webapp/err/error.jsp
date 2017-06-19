<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@page isErrorPage="true"%>
<%@ page import="java.io.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>错误页面</title>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<script type="text/javascript" src="${ctx}/res/js/jquery/jquery.min.js"></script>
<script>
            function showErrorMessage(){
                $("#errorMessageDiv").toggle();
            }
           $(document).ready(showErrorMessage);
        </script>
</head>
<body>
	<table width="100%">
		<tr>
			<td style="border-bottom: dotted 1px Gray;" colspan="2"> &nbsp;&nbsp;错误提示</td>
			<td></td>
		</tr>
		<tr>
			<td style="width: 130px"> </td>
			<td>尊敬的用户：<br />系统出现了异常，请重试。 <br />如果问题重复出现，请向系统管理员反馈。<br />
			<br /> <a id="showErrorMessageButton" href="javascript:showErrorMessage();">详细错误信息</a>
			</td>
		</tr>
	</table>
	<div id="errorMessageDiv" style="border:1px solid blue" >
		<pre> 
                <%
			try {
				//全部内容先写到内存，然后分别从两个输出流再输出到页面和文件
				StringBuffer sb = new StringBuffer();
				sb.append("用户信息").append("\r\n");
				sb.append("账号：" + request.getSession().getAttribute("userName")).append("\r\n");
				sb.append("访问的路径: " + request.getAttribute("javax.servlet.forward.request_uri")).append("\r\n");

				sb.append("异常信息").append("\r\n");
				Enumeration<String>e=request. getAttributeNames();
				if (e.hasMoreElements()) {
					sb.append("请求属性：").append("\r\n");
					while (e.hasMoreElements()) {
						String key = e.nextElement();
						sb.append(key + "=" + request.getAttribute(key)).append("\r\n");
					}
				}
				sb.append("请求参数：").append("\r\n");
				e = request.getParameterNames();
				if (e.hasMoreElements()) {
					sb.append("请求中的Parameter包括：").append("\r\n");
					while (e.hasMoreElements()) {
						String key = e.nextElement();
						sb.append(key + "=" + request.getParameter(key)).append("\r\n");
					}
				}
// 				out.print(sb.toString()); //输出到网页
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		%>
            </pre>
	</div>
</body>
</html>