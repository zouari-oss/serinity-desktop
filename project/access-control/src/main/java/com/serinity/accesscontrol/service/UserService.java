// `UserService` pckage name
package com.serinity.accesscontrol.service;

// `java` import(s)
import java.util.Optional;
import java.util.concurrent.TimeUnit;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.core.EntityManager;

// `serinity` import(s)
import com.serinity.accesscontrol.config.SkinnedRatOrmEntityManager;
import com.serinity.accesscontrol.dto.ServiceResult;
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
import com.serinity.accesscontrol.util.RandomPasswordGenerator;
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

  private static final org.apache.logging.log4j.Logger _LOGGER = org.apache.logging.log4j.LogManager
      .getLogger(MailSenderService.class);

  private static final com.github.benmanes.caffeine.cache.Cache<String, String> resetCodeCache = com.github.benmanes.caffeine.cache.Caffeine
      .newBuilder()
      .expireAfterWrite(10, TimeUnit.MINUTES) // Code valid for 10 min
      .maximumSize(10_000)
      .build();

  /**
   * Registers a new user with email, password, and role.
   *
   * <p>
   * This method performs:
   * <ul>
   * <li>Input validation for email, password, confirm password, and role.</li>
   * <li>User creation with hashed password.</li>
   * <li>Profile, session, and audit log creation.</li>
   * <li>Persisting all entities using the corresponding repositories.</li>
   * </ul>
   * </p>
   *
   * @param email           the user's email
   * @param password        the user's chosen password
   * @param confirmPassword confirmation of the password
   * @param role            the role assigned to the user
   * @return a {@link ServiceResult} indicating success or failure with messages
   */
  public static ServiceResult<User> signUp(
      final String email,
      final String password,
      final String confirmPassword,
      final UserRole role) {

    if (!RegexValidator.isValidEmail(email)) {
      return ServiceResult.failure("Invalid email format.");
    }

    if (!RegexValidator.isValidPassword(password)) {
      return ServiceResult.failure("Password does not meet complexity requirements.");
    }

    if (!password.equals(confirmPassword)) {
      return ServiceResult.failure("Passwords do not match.");
    }

    if (role == null) {
      return ServiceResult.failure("User role must be specified.");
    }

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

    try {
      final EntityManager em = SkinnedRatOrmEntityManager.getEntityManager();

      new UserRepository(em).save(user);
      new ProfileRepository(em).save(profile);
      new AuthSessionRepository(em).save(authSession);
      new AuditLogRepository(em).save(auditLog);

      _LOGGER.info("New user registered: {} with role {}", email, role);

      return ServiceResult.success(user, "User registered successfully. Welcome!");
    } catch (final Exception e) {
      _LOGGER.error("Error during user sign-up for email {}: {}", email, e.getMessage(), e);
      return ServiceResult.failure("Failed to register user. Please try again later.");
    }
  }

  /**
   * Signs in a user using their email (or username) and password.
   *
   * <p>
   * This method performs the following steps:
   * <ol>
   * <li>Validates the email/username format.</li>
   * <li>Retrieves the user from the database.</li>
   * <li>Verifies the password using {@link PasswordEncoder}.</li>
   * <li>Checks for an existing active session.</li>
   * <li>Returns a {@link ServiceResult} containing success or error
   * information.</li>
   * </ol>
   * </p>
   *
   * @param usernameOrEmail the user's email or username
   * @param password        the plain-text password provided by the user
   * @return a {@link ServiceResult} containing the authenticated {@link User} or
   *         an error
   */
  public static ServiceResult<User> signIn(final String usernameOrEmail, final String password) {
    final EntityManager em = SkinnedRatOrmEntityManager.getEntityManager();

    User user = null;

    if (!RegexValidator.isValidEmail(usernameOrEmail)) { // Will considered as username
      final ProfileRepository profileRepository = new ProfileRepository(em);
      Profile profile = profileRepository.findByUsername(usernameOrEmail);
      user = profile.getUser();
    } else { // It's an email
      final UserRepository userRepository = new UserRepository(em);
      user = userRepository.findUserByEmail(usernameOrEmail);
    }

    if (user == null) {
      return ServiceResult.failure("User not found");
    }

    if (!PasswordEncoder.isConfirmPassword(password, user.getPassword())) {
      return ServiceResult.failure("Incorrect password");
    }

    final AuthSessionRepository authSessionRepository = new AuthSessionRepository(em);
    final Optional<AuthSession> activeSession = authSessionRepository.findActiveSession(user);
    activeSession.ifPresent(session -> {
      session.setRevoked(true);
      authSessionRepository.update(session);
    });

    AuthSession newAuthSession = new AuthSession();
    newAuthSession.setUser(user);

    final AuditLog auditLog = new AuditLog();
    auditLog.setAction(AuditAction.USER_LOGIN.getValue());
    auditLog.setSession(newAuthSession);

    new AuditLogRepository(em).save(auditLog);
    authSessionRepository.save(newAuthSession);

    return ServiceResult.success(user, "User signed in successfully!");
  }

  public static ServiceResult<Void> sendResetMail(final String email) {
    if (!RegexValidator.isValidEmail(email)) {
      _LOGGER.warn("Invalid Email! - {}", email);
      return ServiceResult.failure("Invalid email format!");
    }

    try {
      final EntityManager em = SkinnedRatOrmEntityManager.getEntityManager();
      final UserRepository userRepository = new UserRepository(em);
      final User user = userRepository.findUserByEmail(email);

      if (user == null) {
        _LOGGER.info("User with email {} does not exist.", email);
        return ServiceResult.failure("User does not exist!");
      }

      final ProfileRepository profileRepository = new ProfileRepository(em);
      final Profile profile = profileRepository.findByUserId(user.getId());

      final String generatedCode = RandomPasswordGenerator.generate();

      // Store reset code in Caffeine
      resetCodeCache.put(user.getEmail(), generatedCode);

      MailSenderService.sendPasswordReset(
          profile.getUsername(),
          user.getEmail(),
          generatedCode);

      return ServiceResult.success(null,
          "Reset code sent successfully.");

    } catch (final Exception e) {
      _LOGGER.error("Password reset start failed", e);
      return ServiceResult.failure("Failed to process reset request.");
    }
  }

  public static ServiceResult<Void> confirmResetMail(
      final String email,
      final String inputCode,
      final String newPassword) {
    final String storedCode = resetCodeCache.getIfPresent(email);

    if (storedCode == null) {
      return ServiceResult.failure("Code expired or not found.");
    }

    if (!storedCode.equals(inputCode)) {
      return ServiceResult.failure("Incorrect code!");
    }

    if (!RegexValidator.isValidPassword(newPassword)) {
      return ServiceResult.failure("Invalid password format!");
    }

    try {
      final EntityManager em = SkinnedRatOrmEntityManager.getEntityManager();
      final UserRepository userRepository = new UserRepository(em);
      final User user = userRepository.findUserByEmail(email);

      user.setPasswordHash(PasswordEncoder.encode(newPassword));
      userRepository.update(user);

      // Remove code after successful reset
      resetCodeCache.invalidate(email);

      return ServiceResult.success(null,
          "Password updated successfully!");

    } catch (final Exception e) {
      _LOGGER.error("Password update failed", e);
      return ServiceResult.failure("Failed to update password.");
    }
  }
} // UserService final class
