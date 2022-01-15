package com.studylecture.event;

import com.studylecture.domain.Account;
import com.studylecture.domain.Enrollment;
import com.studylecture.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByEventAndAccount(Event event, Account account);

    Enrollment findByEventAndAccount(Event event, Account account);
}
