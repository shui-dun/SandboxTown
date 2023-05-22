package com.shuidun.sandbox_town_backend.service;

import java.util.Set;

public interface PermService {
    public Set<String> getPermsByRoles(Set<String> roles);
}
