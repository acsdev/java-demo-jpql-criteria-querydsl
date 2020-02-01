package eti.query.demonstration.department.control;


import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import eti.query.demonstration.department.entities.DepartmentDomain;
import eti.query.demonstration.department.entities.DepartmentEntity;
import eti.query.demonstration.department.entities.dslmodel.QDepartmentEntity;
import eti.query.demonstration.department.entities.metamodel.DepartmentEntity_;
import eti.query.demonstration.employee.entities.EmployeeEntity;
import eti.query.demonstration.employee.entities.dslmodel.QEmployeeEntity;
import eti.query.demonstration.employee.entities.metamodel.EmployeeEntity_;
import eti.query.demonstration.location.entities.LocationEntity;
import eti.query.demonstration.location.entities.dslmodel.QLocationEntity;
import eti.query.demonstration.location.entities.metamodel.LocationEntity_;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class DepartmentStore {

    @PersistenceContext
    private EntityManager entityManager;

    void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<DepartmentDomain> jpqlFindDepartamentWithManagerLocation() {
        StringBuffer sql = new StringBuffer();
        sql.append("      SELECT new eti.query.demonstration.department.entities");
        sql.append(".DepartmentDomain(o.departmentName, e.firstName, l.streetAddress) ");
        sql.append("        FROM DepartmentEntity o ");
        sql.append("  INNER JOIN EmployeeEntity e ON e.employeeId = o.managerId ");
        sql.append("  INNER JOIN LocationEntity l ON l.locationId = o.locationId ");
        return entityManager.createQuery(sql.toString(), DepartmentDomain.class).getResultList();
    }

    public List<DepartmentDomain> criteriaFindDepartamentWithManagerLocation() {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<DepartmentDomain> criteria = builder.createQuery(DepartmentDomain.class);
        Root<DepartmentEntity> department = criteria.from(DepartmentEntity.class);
        Root<EmployeeEntity> employee = criteria.from(EmployeeEntity.class);
        Root<LocationEntity> location = criteria.from(LocationEntity.class);

        criteria.select(builder.construct(DepartmentDomain.class,
            department.get(DepartmentEntity_.departmentName),
            employee.get(EmployeeEntity_.firstName),
            location.get(LocationEntity_.streetAddress)));

        criteria.where(
                builder.equal(department.get(DepartmentEntity_.managerId), employee.get(EmployeeEntity_.employeeId)),
                builder.equal(department.get(DepartmentEntity_.locationId), location.get(LocationEntity_.locationId)));

        return entityManager.createQuery(criteria).getResultList();

    }

    public List<DepartmentDomain> queryDSLFindDepartamentWithManagerLocation() {

        QDepartmentEntity department = QDepartmentEntity.departmentEntity;
        QEmployeeEntity employee = QEmployeeEntity.employeeEntity;
        QLocationEntity location = QLocationEntity.locationEntity;

        return new JPAQueryFactory(this.entityManager)
                .select(Projections.bean(DepartmentDomain.class, department.departmentName, employee.firstName, location.streetAddress))
                .from(department)
                .innerJoin(employee)
                .on(employee.employeeId.eq(department.managerId))
                .innerJoin(location)
                .on(location.locationId.eq(department.locationId))
                .fetch();
    }
}
