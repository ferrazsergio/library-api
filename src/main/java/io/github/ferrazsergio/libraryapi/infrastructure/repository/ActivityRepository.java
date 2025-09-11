package io.github.ferrazsergio.libraryapi.infrastructure.repository;

import io.github.ferrazsergio.libraryapi.domain.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
}
