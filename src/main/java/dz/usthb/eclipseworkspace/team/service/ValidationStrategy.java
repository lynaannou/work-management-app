package dz.usthb.eclipseworkspace.team.service;

import dz.usthb.eclipseworkspace.workspace.model.AppUser;
import dz.usthb.eclipseworkspace.team.exceptions.ValidationException;

public interface ValidationStrategy {
    boolean validate(AppUser user, Long teamId) throws ValidationException;
}