package vn.sun.public_service_manager.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import vn.sun.public_service_manager.entity.ActivityLog;
import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.service.ActivityLogService;
import vn.sun.public_service_manager.service.CitizenService;
import vn.sun.public_service_manager.service.UserManagementService;
import vn.sun.public_service_manager.utils.SecurityUtil;
import vn.sun.public_service_manager.utils.annotation.LogActivity;
import vn.sun.public_service_manager.utils.constant.ActorType;
import vn.sun.public_service_manager.exception.ResourceNotFoundException;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityLogAspect {

    private final ActivityLogService activityLogService;
    private final CitizenService citizenService;
    private final UserManagementService userService;

    @AfterReturning(value = "@annotation(vn.sun.public_service_manager.utils.annotation.LogActivity)", returning = "result")
    public void logActivity(JoinPoint joinPoint, Object result) {
        LogActivity logActivity = getLogActivityAnnotation(joinPoint);

        String actorName = SecurityUtil.getCurrentUserName();
        ActivityLog activityLog = new ActivityLog();

        // resolve actor safely
        if (actorName != null && !actorName.equals("anonymousUser")) {
            try {
                var user = userService.getByUsername(actorName);
                if (user != null) {
                    activityLog.setActorId(user.getId());
                    activityLog.setActorType(ActorType.ADMIN);
                } else {
                    Citizen citizen = citizenService.getByNationalId(actorName);
                    if (citizen != null) {
                        activityLog.setActorId(citizen.getId());
                        activityLog.setActorType(ActorType.CITIZEN);
                    }
                }
            } catch (ResourceNotFoundException rnfe) {
                // actor not found - skip setting actor id so we don't throw
                log.debug("Actor not found for '{}', skipping actor resolution", actorName);
            } catch (Exception ex) {
                log.warn("Error resolving actor '{}': {}", actorName, ex.getMessage());
            }
        }

        Object[] args = joinPoint.getArgs();
        Long targetId = null;
        for (Object arg : args) {
            if (arg instanceof Long) {
                targetId = (Long) arg; // assume targetId is Long
                break;
            }
        }
        activityLog.setTargetId(targetId);
        // if actorType wasn't set above default to CITIZEN to preserve existing
        // behavior
        if (activityLog.getActorType() == null) {
            activityLog.setActorType(ActorType.CITIZEN);
        }
        activityLog.setTargetType(logActivity.targetType());
        activityLog.setDescription(logActivity.description());
        activityLog.setAction(logActivity.action());

        try {
            activityLogService.log(activityLog);
            log.debug("Activity logged: {}", activityLog);
        } catch (Exception ex) {
            log.warn("Failed to persist activity log: {}", ex.getMessage());
        }
    }

    @AfterThrowing(value = "@annotation(vn.sun.public_service_manager.utils.annotation.LogActivity)", throwing = "ex")
    public void logActivityFailure(JoinPoint joinPoint, Throwable ex) {
        LogActivity logActivity = getLogActivityAnnotation(joinPoint);
        ActivityLog activityLog = new ActivityLog();

        // resolve actor safely from security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String principalName = auth.getName();
            try {
                var user = userService.getByUsername(principalName);
                if (user != null) {
                    activityLog.setActorId(user.getId());
                    activityLog.setActorType(ActorType.ADMIN);
                } else {
                    Citizen citizen = citizenService.getByNationalId(principalName);
                    if (citizen != null) {
                        activityLog.setActorId(citizen.getId());
                        activityLog.setActorType(ActorType.CITIZEN);
                    }
                }
            } catch (ResourceNotFoundException rnfe) {
                log.debug("Actor not found for failure log: {}", principalName);
            } catch (Exception e) {
                log.warn("Error resolving actor for failure log: {}", e.getMessage());
            }
        } else {
            log.debug("No authenticated principal for failure log, proceeding without actor info");
        }

        if (activityLog.getActorType() == null) {
            activityLog.setActorType(ActorType.CITIZEN);
        }
        activityLog.setTargetType(logActivity.targetType());
        activityLog.setDescription(
                "Failed to perform action: " + logActivity.description() + ". Reason: " + ex.getMessage());
        activityLog.setAction(logActivity.action());

        try {
            activityLogService.log(activityLog);
            log.warn("Activity failed and logged: {}", activityLog);
        } catch (Exception logEx) {
            log.warn("Failed to persist failure activity log: {}", logEx.getMessage());
        }
    }

    @Around("@annotation(vn.sun.public_service_manager.utils.annotation.LogActivity)")
    public Object aroundLog(ProceedingJoinPoint pjp) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return pjp.proceed();
        }

        String principalName = auth.getName();
        try {
            // just attempt to resolve, but do not throw if missing
            citizenService.getByNationalId(principalName);
        } catch (Exception ex) {
            log.debug("AroundLog: cannot resolve citizen '{}': {}", principalName, ex.getMessage());
        }

        return pjp.proceed();
    }

    private LogActivity getLogActivityAnnotation(JoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature())
                .getMethod()
                .getAnnotation(LogActivity.class);
    }
}
