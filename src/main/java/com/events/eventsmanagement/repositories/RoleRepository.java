package com.events.eventsmanagement.repositories;

import org.springframework.data.repository.CrudRepository;

import com.events.eventsmanagement.models.Role;

public interface RoleRepository extends CrudRepository<Role, Integer>
{
    Role findRoleByName(String name);
}
