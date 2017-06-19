<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<!DOCTYPE html>
<html>
<head>
	<title>${lname}</title>
</head>
<body>
	<div class="searchBox">
		<ul>
			<li><span>名称：</span><input type="text" class="easyui-textbox" name="name_lk"  value=""  ></li>
			<li><button onclick="cmd_search()">查询</button></li>
		</ul>
		<div class="clearfix"></div>
	</div>
	<div class="commondBox">
		<ul>
			<li onclick="cmd_insert()"><span class="btn_insert">添加</span></li>
			<li onclick="cmd_modify()"><span class="btn_modify">修改</span></li>
 			<li onclick="cmd_remove()"><span class="btn_remove">删除</span></li>
		</ul>
		<div class="clearfix"></div>
	</div>
	<div id="datagrid"></div>

	<div id="dialog" class="formPanel">
		<form id="ff" action="" method="POST" onsubmit="return false">
			<input type="hidden" name="id" value="">
			<ul>
				${form}					 
			</ul>
		</form>
		<div id="bb">
			<a class="easyui-linkbutton"  onclick="cmd_save()">保存</a>
			<a class="easyui-linkbutton"  onclick="$('#dialog').dialog('close');">关闭</a>
		</div>
	</div>
	<script type="text/javascript" src="${'$'}{ctx}/res/js/gen/${lname}.js"></script>
</body>
</html>