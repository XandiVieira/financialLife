package com.relyon.financiallife.repository.specification;

import com.relyon.financiallife.model.role.Role;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Builder
public class RoleSpecification implements Specification<Role> {

    private final String name;
    private final String createdBy;
    private final String lastModifiedBy;

    public static RoleSpecificationBuilder builder() {
        return new RoleSpecificationBuilder();
    }

    @Override
    public Predicate toPredicate(@NonNull Root<Role> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();
        if (name != null) {
            predicates.add(builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (createdBy != null) {
            predicates.add(builder.like(builder.lower(root.get("createdBy")), "%" + createdBy.toLowerCase() + "%"));
        }
        if (lastModifiedBy != null) {
            predicates.add(builder.like(builder.lower(root.get("lastModifiedBy")), "%" + lastModifiedBy.toLowerCase() + "%"));
        }
        return builder.and(predicates.toArray(new Predicate[0]));
    }

    public static class RoleSpecificationBuilder {

        private final List<Specification<Role>> specs = new ArrayList<>();

        public RoleSpecificationBuilder name(String name) {
            if (StringUtils.hasText(name)) {
                specs.add((root, query, builder) ->
                        builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            return this;
        }

        public RoleSpecificationBuilder createdBy(String createdBy) {
            if (StringUtils.hasText(createdBy)) {
                specs.add((root, query, builder) ->
                        builder.like(builder.lower(root.get("createdBy")), "%" + createdBy.toLowerCase() + "%"));
            }
            return this;
        }

        public RoleSpecificationBuilder lastModifiedBy(String lastModifiedBy) {
            if (StringUtils.hasText(lastModifiedBy)) {
                specs.add((root, query, builder) ->
                        builder.like(builder.lower(root.get("lastModifiedBy")), "%" + lastModifiedBy.toLowerCase() + "%"));
            }
            return this;
        }

        public Specification<Role> build() {
            if (specs.isEmpty()) {
                return null;
            }
            Specification<Role> result = Specification.where(specs.get(0));
            for (int i = 1; i < specs.size(); i++) {
                result = result.and(specs.get(i));
            }
            return result;
        }
    }
}