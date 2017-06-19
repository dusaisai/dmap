package ${package}.dao;

import org.springframework.stereotype.Repository;
import com.wttech.data.jpa.GenericDao;
import ${name};
${impl}

@Repository
public interface ${simpleName}Dao extends GenericDao<${simpleName}, ${pkName}> {

}
