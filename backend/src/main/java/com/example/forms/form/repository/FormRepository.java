package com.example.forms.form.repository;

import com.example.forms.form.entity.Form;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormRepository extends JpaRepository<Form, Long> {
    List<Form> findByOwnerIdOrderByUpdatedAtDesc(Long ownerId);
}
