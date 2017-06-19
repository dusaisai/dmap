var _base = _ctx + "/rest/${lname}/",
	cmd = {
		get: _base + "get/",
		save: _base + "save",
		delete: _base + "delete/",
		list: _base + "list"
	}

$(function() {
	//初始化表格
	$('#datagrid').datagrid({
		url: cmd.list,
		rownumbers: true,
		queryParams: formToObject('.searchBox'),
		loadFilter: WT.pageLoadFilter,
		columns: [
			[
			{field: 'id',hidden: true}
		 	${column}
			]
		]
	});
	//form
	$('#dialog').dialog({
		title: 'message',
		width: 600,
		closed: true,
		buttons: '#bb'
	});
	$(window).resize((function() {
		$('#datagrid').datagrid('resize', {
			'height': $(window).height() - 15 - $('.searchBox').height() - $('.commondBox').height()
		});
		return arguments.callee;
	})());
});


//查询
function cmd_search() {
	$('#datagrid').datagrid("load", formToObject('.searchBox'));
}


// 添加
function cmd_insert() {
 	$('#ff').form('clear');
	$('#dialog').dialog("open");
}
//修改
function cmd_modify() {
	var row = $('#datagrid').datagrid("getSelected");
	if (row) {
		$('#dialog').dialog("open");
		$('#ff').form('clear').form("load", cmd.get + row.id);
	} else
		$.messager.alert('提示', '请选择一条记录');

}
//删除
function cmd_remove() {
	var row = $('#datagrid').datagrid("getSelected");
	if (row) {
		$.messager.confirm('询问', '确定要删除[' + row.name + ']数据吗？', function(r) {
			if (r) {
				$.ajax({
					type: "delete",
					url: cmd.delete + row.id,
					dataType: 'json',
					success: function(data) {
						if (err(data)) return;
						$.messager.show('提示', '删除成功！');
						$('#datagrid').datagrid("reload");
					}
				});
			}
		});
	} else $.messager.alert('提示', '请选择一条记录');

}
//保存
function cmd_save() {
	$('#ff').form('submit', {
		url: cmd.save,
		success: function(data) {
			if (err(data)) return;
			$('#datagrid').datagrid("reload");
			$('#dialog').dialog("close");
			info('保存成功！');
		}
	});
}