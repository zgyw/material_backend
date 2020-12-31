package com.zgyw.materiel.repository;

import com.zgyw.materiel.bean.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Integer> {

    User findByLoginName(String name);
}
