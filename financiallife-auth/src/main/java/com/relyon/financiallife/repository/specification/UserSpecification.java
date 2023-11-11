package com.relyon.financiallife.repository.specification;

import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.user.User;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Builder
public class UserSpecification implements Specification<User> {

    private static final String FIELD_ROLES = "roles";

    private final String firstName;
    private final String lastName;
    private final String username;
    private final LocalDate dateOfBirth;
    private final String cpf;
    private final String cellphoneNumber;
    private final String email;
    private final String createdBy;
    private final String lastModifiedBy;
    private final Boolean enabled;
    private final List<String> roles;

    public static UserSpecificationBuilder builder() {
        return new UserSpecificationBuilder();
    }

    @Override
    public Predicate toPredicate(@NonNull Root<User> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();
        if (firstName != null) {
            predicates.add(builder.like(builder.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
        }
        if (lastName != null) {
            predicates.add(builder.like(builder.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
        }
        if (username != null) {
            predicates.add(builder.like(builder.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
        }
        if (email != null) {
            predicates.add(builder.like(builder.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }
        if (createdBy != null) {
            predicates.add(builder.like(builder.lower(root.get("createdBy")), "%" + createdBy.toLowerCase() + "%"));
        }
        if (lastModifiedBy != null) {
            predicates.add(builder.like(builder.lower(root.get("lastModifiedBy")), "%" + lastModifiedBy.toLowerCase() + "%"));
        }
        if (dateOfBirth != null) {
            predicates.add(builder.equal(root.get("dateOfBirth"), dateOfBirth));
        }
        if (cpf != null) {
            predicates.add(builder.equal(root.get("cpf"), cpf));
        }
        if (cellphoneNumber != null) {
            predicates.add(builder.equal(root.get("cellphoneNumber"), cellphoneNumber));
        }
        if (enabled != null) {
            predicates.add(builder.equal(root.get("enabled"), enabled));
        }
        if (roles != null && !roles.isEmpty()) {
            Join<User, Role> join = root.join(FIELD_ROLES);
            predicates.add(join.get("name").in(roles));
        }

        return builder.and(predicates.toArray(new Predicate[0]));
    }

    public static class UserSpecificationBuilder {

        private final List<Specification<User>> specs = new ArrayList<>();

        public UserSpecificationBuilder firstName(String firstName) {
            if (StringUtils.hasText(firstName)) {
                specs.add((root, query, builder) ->
                        builder.like(builder.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
            }
            return this;
        }

        public UserSpecificationBuilder lastName(String lastName) {
            if (StringUtils.hasText(lastName)) {
                specs.add((root, query, builder) ->
                        builder.like(builder.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
            }
            return this;
        }

        public UserSpecificationBuilder username(String username) {
            if (StringUtils.hasText(username)) {
                specs.add((root, query, builder) ->
                        builder.like(builder.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
            }
            return this;
        }

        public UserSpecificationBuilder email(String email) {
            if (StringUtils.hasText(email)) {
                specs.add((root, query, builder) ->
                        builder.like(builder.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }
            return this;
        }

        public UserSpecificationBuilder createdBy(String createdBy) {
            if (StringUtils.hasText(createdBy)) {
                specs.add((root, query, builder) ->
                        builder.like(builder.lower(root.get("createdBy")), "%" + createdBy.toLowerCase() + "%"));
            }
            return this;
        }

        public UserSpecificationBuilder lastModifiedBy(String lastModifiedBy) {
            if (StringUtils.hasText(lastModifiedBy)) {
                specs.add((root, query, builder) ->
                        builder.like(builder.lower(root.get("lastModifiedBy")), "%" + lastModifiedBy.toLowerCase() + "%"));
            }
            return this;
        }

        public UserSpecificationBuilder dateOfBirth(LocalDate dateOfBirth) {
            if (dateOfBirth != null) {
                specs.add((root, query, builder) ->
                        builder.equal(root.get("dateOfBirth"), dateOfBirth));
            }
            return this;
        }

        public UserSpecificationBuilder cpf(String cpf) {
            if (StringUtils.hasText(cpf)) {
                specs.add((root, query, builder) ->
                        builder.like(builder.lower(root.get("cpf")), "%" + cpf.toLowerCase() + "%"));
            }
            return this;
        }

        public UserSpecificationBuilder cellphoneNumber(String cellphoneNumber) {
            if (StringUtils.hasText(cellphoneNumber)) {
                specs.add((root, query, builder) ->
                        builder.like(builder.lower(root.get("cellphoneNumber")), "%" + cellphoneNumber.toLowerCase() + "%"));
            }
            return this;
        }

        public UserSpecificationBuilder enabled(Boolean enabled) {
            if (enabled != null) {
                specs.add((root, query, builder) ->
                        builder.equal(root.get("enabled"), enabled));
            }
            return this;
        }

        public void rolesInclusive(List<String> roles) {
            if (roles != null && !roles.isEmpty()) {
                specs.add((root, query, builder) -> {
                    Join<User, Role> join = root.join(FIELD_ROLES);
                    return join.get("name").in(roles);
                });
            }
        }

        public void rolesExclusive(List<String> roles) {
            if (roles != null && !roles.isEmpty()) {
                specs.add((root, query, builder) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    for (String role : roles) {
                        Join<User, Role> join = root.join(FIELD_ROLES);
                        predicates.add(builder.equal(join.get("name"), role));
                    }
                    return builder.and(predicates.toArray(new Predicate[0]));
                });
            }
        }

        public Specification<User> build() {
            return specs.stream().reduce(Specification::and).orElse(null);
        }
    }
}