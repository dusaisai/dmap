package ${package}.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wttech.data.utils.QueryFilters;
import ${package}.controller.BaseController;
import ${package}.service.${simpleName}Manager;
import ${name};
${impl}



@Controller
@RequestMapping("${lname}")
public class ${simpleName}Controller extends BaseController {

	@Resource
	private ${simpleName}Manager manager;

	/**
	 * 按指定id的查询菜单信息
	 * 
	 * @param id
	 *            菜单主键
	 * @return
	 */
	@RequestMapping(value = "/get/{id}",method = {  RequestMethod.GET })
	@ResponseBody
	public ${simpleName} get(@PathVariable() ${pkName} id) {
		return manager.get(id);
	}

	/**
	 * 修改或新增
	 * 
	 * @param id
	 *            可选 需要修改的记录ID
	 * @return ExtjsResult EXTJS 结果的封闭
	 */
	@RequestMapping(value = "/save", method = { RequestMethod.PUT, RequestMethod.POST })
	@ResponseBody
	public ${simpleName} save(${pkName} id, HttpServletRequest request) {
		// 提交菜单信息
		${simpleName} vo = new ${simpleName}();
		if (!StringUtils.isEmpty(id)) { // 从数据库读取记录
			vo = manager.get(id);
		}
		bind(request, vo);
		manager.save(vo);
		return vo;
	}

	/**
	 * 删除记录 单条
	 */
	@RequestMapping(value = "/delete/{id}", method = { RequestMethod.DELETE })
	@ResponseBody
	public boolean delete(@PathVariable() ${pkName} id) {
		manager.remove(id);
		return true;
	}

	/** 默认动态查询 */
	@RequestMapping(value = "/list",method = {  RequestMethod.POST })
	@ResponseBody
	public Page<${simpleName}> list(HttpServletRequest request, int page, int rows) {
		Pageable pageable = new PageRequest(page - 1, rows, new Sort("name"));
		return manager.findAll(QueryFilters.from(request.getParameterMap()).toArray(), pageable);
	}

}
