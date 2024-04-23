package com.ime.lockmanager.user.adapter.out;

import com.ime.lockmanager.auth.application.port.out.AuthToUserCommandPort;
import com.ime.lockmanager.user.application.port.out.UserCommandPort;
import com.ime.lockmanager.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class UserCommandRepository implements UserCommandPort, AuthToUserCommandPort {
    private final UserJpaRepository userJpaRepository;

    @Override
    public List<User> saveAll(List<User> users) {
        return userJpaRepository.saveAll(users);
    }

    @Override
    public void deleteAll() {
        userJpaRepository.deleteAll();
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }
}
