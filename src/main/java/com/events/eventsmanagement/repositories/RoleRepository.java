package com.events.eventsmanagement.repositories;

import com.events.eventsmanagement.models.Role;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, Integer>
{
    Role findRoleByName(String name);
}
