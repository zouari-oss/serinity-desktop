/**
 * UserService.java
 *
 * Service class for managing user registration and authentication.
 *
 * <p>This class provides methods to register new users with a profile and role,
 * as well as to authenticate existing users using their username and password.</p>
 *
 * @author  @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since   2026-02-03
 * @see     User
 * @see     Profile
 * @see     UserRepository
 * @see     PasswordEncoder
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/service/UserService.java"
 * target="_blank">
 * UserService.java
 * </a>
 */

// `UserService` pckage name
package com.serinity.accesscontrol.service;

// `serinity` import(s)
import com.serinity.accesscontrol.flag.UserRole;
import com.serinity.accesscontrol.model.Profile;
import com.serinity.accesscontrol.model.User;
import com.serinity.accesscontrol.repository.ProfileRepository;
import com.serinity.accesscontrol.repository.UserRepository;
import com.serinity.accesscontrol.util.PasswordEncoder;

public final class UserService {
  public void register(final String email, final String password, final UserRole role) {
    final User user = new User();
    user.setEmail(email);
    user.setPasswordHash(PasswordEncoder.encode(password));
    user.setRole(role); // default role

    final Profile profile = new Profile();
    profile.setUser(user); // bidirectional link
    user.setProfile(profile);

    UserRepository.save(user);
  }

  public User login(final String username, final String password) {
    final Profile profile = ProfileRepository.findByUsername(username);
    if (profile == null)
      return null;
    User user = profile.getUser();
    return user.getPasswordHash().equals(password) ? user : null;
  }
} // UserService final class
