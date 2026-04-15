package com.example.forms.form.repository;

import com.example.forms.form.entity.Form;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormRepository extends JpaRepository<Form, Long> {
    List<Form> findByOwnerIdOrderByUpdatedAtDesc(Long ownerId);

    Optional<Form> findBySlug(String slug);
}
