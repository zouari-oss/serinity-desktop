// `UserService` pckage name
package com.serinity.accesscontrol.service;

import org.zouarioss.skinnedratorm.core.EntityManager;

// `serinity` import(s)
import com.serinity.accesscontrol.config.SkinnedRatOrmEntityManager;
import com.serinity.accesscontrol.flag.AuditAction;
import com.serinity.accesscontrol.flag.UserRole;
import com.serinity.accesscontrol.model.AuditLog;
import com.serinity.accesscontrol.model.AuthSession;
import com.serinity.accesscontrol.model.Profile;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.repository.AuditLogRepository;
import com.serinity.accesscontrol.repository.AuthSessionRepository;
import com.serinity.accesscontrol.repository.ProfileRepository;
import com.serinity.accesscontrol.repository.UserRepository;
import com.serinity.accesscontrol.util.PasswordEncoder;
import com.serinity.accesscontrol.util.RegexValidator;

/**
 * Service class for managing user registration and authentication.
 *
 * <p>
 * This class provides methods to register new users with a profile and role,
 * as well as to authenticate existing users using their username and password.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-03
 * @see User
 * @see Profile
 * @see UserRepository
 * @see PasswordEncoder
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/service/UserService.java">
 *      UserService.java
 *      </a>
 */
public final class UserService {
  public static void signUp(
      final String email,
      final String password,
      final String confirmPassword,
      final UserRole role) {
    // =====================
    // == Data Validation ==
    // =====================
    if (!RegexValidator.isValidEmail(email)) {
      return;
    }

    if (!RegexValidator.isValidPassword(password)) {
      return;
    }

    if (!password.equals(confirmPassword)) {
      return;
    }

    if (role == null) {
      return;
    }

    // ===========
    // == Setup ==
    // ===========
    final User user = new User();
    user.setEmail(email);
    user.setPasswordHash(PasswordEncoder.encode(password));
    user.setRole(role);

    final Profile profile = new Profile();
    profile.setUser(user);

    final AuthSession authSession = new AuthSession();
    authSession.setUser(user);

    final AuditLog auditLog = new AuditLog();
    auditLog.setAction(AuditAction.USER_SIGN_UP.getValue());
    auditLog.setSession(authSession);

    // ================
    // == Persisting ==
    // ================
    final EntityManager em = SkinnedRatOrmEntityManager.getEntityManager();
    final UserRepository userRepository = new UserRepository(em);
    userRepository.save(user);

    final ProfileRepository profileRepository = new ProfileRepository(em);
    profileRepository.save(profile);

    final AuthSessionRepository authSessionRepository = new AuthSessionRepository(em);
    authSessionRepository.save(authSession);

    final AuditLogRepository auditLogRepository = new AuditLogRepository(em);
    auditLogRepository.save(auditLog);

    System.out.println("DONE");
  }

  public static User signIn(final String usernameOrEmail, final String password) {
    final EntityManager em = SkinnedRatOrmEntityManager.getEntityManager();
    final AuthSessionRepository authSessionRepository = new AuthSessionRepository(em);
    final UserRepository userRepository = new UserRepository(em);

    // =====================
    // == Data Validation ==
    // =====================
    if (!RegexValidator.isValidEmail(usernameOrEmail)) {
      return null;
    }

    final User user = userRepository.findUserByEmail(usernameOrEmail);

    authSessionRepository.findActiveSession(user);
    return null;
  }
} // UserService final class
