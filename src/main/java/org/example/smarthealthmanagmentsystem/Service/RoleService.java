package org.example.smarthealthmanagmentsystem.Service;

import org.example.smarthealthmanagmentsystem.Entity.Role;
import org.example.smarthealthmanagmentsystem.ExceptionHandler.ResourceNotFoundException;
import org.example.smarthealthmanagmentsystem.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    // Fetch role by name or throw exception if not found
    public Role findByName(String roleType) {
        return roleRepository.findByName(roleType)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + roleType));
    }

}
