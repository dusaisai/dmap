package ${package}.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wttech.data.service.GenericManagerImpl;
 import ${name};
${impl}
import ${package}.dao.${simpleName}Dao;
import ${package}.service.${simpleName}Manager;

@Service
public class ${simpleName}ManagerImpl extends GenericManagerImpl<${simpleName}, ${pkName}> implements ${simpleName}Manager {
	private ${simpleName}Dao dao;

	@Autowired
	public ${simpleName}ManagerImpl(${simpleName}Dao genericDao) {
		super(genericDao);
		dao = genericDao;
	}

 
}
